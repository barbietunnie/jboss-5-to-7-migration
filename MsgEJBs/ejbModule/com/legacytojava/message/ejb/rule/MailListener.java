package com.legacytojava.message.ejb.rule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.queue.PassThroughMessageConverter;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.exception.DataValidationException;

/**
 * Message-Driven Bean implementation class for: MailListener
 *
 */
@MessageDriven(
		activationConfig = {
			@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
			@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/mailReaderOutput")
			}, 
		mappedName = "mailReaderOutput")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Resource(mappedName = "java:jboss/MessageDS",
	name = "jdbc/msgdb_pool",
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
public class MailListener implements MessageListener {
	static final Logger logger = Logger.getLogger(MailListener.class);
	@Resource
	private MessageDrivenContext messageContext;
	private final AbstractApplicationContext factory;
	private static final int MAX_DELIVERY_COUNT = 2;
    /**
     * Default constructor. 
     */
    public MailListener() {
    	factory = SpringUtil.getAppContext();
    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    @EJB(name="msgInbox", beanInterface=MsgInboxLocal.class)
    private MsgInboxLocal msgInbox;
    
    public void onMessage(Message message) {
		logger.debug("Message Driven Bean got message " + message);
		try {
			if (message.getJMSRedelivered()) {
				Enumeration<?> enu = message.getPropertyNames();
				while (enu.hasMoreElements()) {
					String propName = (String) enu.nextElement();
					logger.info("JMS Property name: " + propName);
					if (propName == null)
						continue;
					String _propName = propName.toLowerCase();
					if (_propName.indexOf("jms") >= 0
							&& _propName.indexOf("delivery") > _propName.indexOf("jms")
							&& _propName.indexOf("count") > _propName.indexOf("delivery")) {
						String _dlvrCount = message.getStringProperty(propName);
						logger.info(propName + ": " + _dlvrCount);
						if (_dlvrCount != null) {
							try {
								int dlvrCount = Integer.parseInt(_dlvrCount);
								if (MAX_DELIVERY_COUNT > 0 && dlvrCount > MAX_DELIVERY_COUNT) {
									sendToErrorQueue(message, new JMSException(
											"DeliveryCount exceeded Listener's maximum: "
													+ MAX_DELIVERY_COUNT));
									return;
								}
							}
							catch (NumberFormatException e) {
								logger.error("NumberFormatException caught", e);
							}
						}
					}
				}
			}
		}
		catch (JMSException e) {
			logger.error("JMSException caught", e);
		}
		
		try {
			MessageBean msgBean = getMessageBean(message);

			msgInbox.parseMessage(msgBean);

			TaskScheduler taskScheduler = new TaskScheduler(factory);
			taskScheduler.scheduleTasks(msgBean);

			// clear the object for garbage collection (is this necessary?)
			msgBean.destroy();
		}
		catch (MessageFormatException e) {
			logger.error("A MessageFormatException occurred", e);
			sendToErrorQueue(message, e);
		}
		catch (DataValidationException e) {
			logger.error("A DataValidationException occurred", e);
			sendToErrorQueue(message, e);
		}
		catch (NumberFormatException e) {
			logger.error("A NumberFormatException occurred", e);
			sendToErrorQueue(message, e);
		}
		catch (MessagingException e) {
			logger.error("A MessagingException occurred", e);
			sendToErrorQueue(message, e);
		}
		catch (RemoteException e) {
			logger.error("A RemoteException occurred", e);
			messageContext.setRollbackOnly();
			throw new EJBException(e.toString());
		}
		catch (JMSException e) {
			logger.error("A JMSException occurred", e);
			messageContext.setRollbackOnly();
			throw new EJBException(e.toString());
		}
		catch (IOException e) {
			logger.error("An IOException occurred", e);
			messageContext.setRollbackOnly();
			throw new EJBException(e.toString());
		}
    }

	private MessageBean getMessageBean(Message req) throws JMSException, MessagingException {
		Message msg = (Message) req;
		MessageBean msgBean = null;
		if (msg instanceof ObjectMessage) {
			msgBean = (MessageBean) ((ObjectMessage) msg).getObject();
			if (msgBean == null) { // should never happen
				throw new MessageFormatException("Object message contained no content");
			}
		}
		else if (msg instanceof BytesMessage) {
			BytesMessage bytesMsg = (BytesMessage) msg;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			while ((len = bytesMsg.readBytes(buffer))>0) {
				baos.write(buffer, 0, len);
			}
			msgBean = MessageBeanUtil.createBeanFromStream(baos.toByteArray());
		}
		else {
			// not from MailReader
			throw new MessageFormatException(
					"message was not an ObjectMessage or BytesMessage as expected");
		}
		return msgBean;
	}

	private void sendToErrorQueue(javax.jms.Message message, Exception exception) {
		logger.info("Entering sendToErrorQueue()..., " + exception);
		
		try {
			message.setJMSCorrelationID(message.getJMSMessageID());

			message.clearProperties();
			message.setStringProperty("UnhandledError", exception.toString());
		}
		catch (JMSException e) {
			logger.error("Failled to set message properties - ", e);
		}

		try {
			//jmsProcessor.writeMsg(message, true);
			writeMsg(message);
			logger.info("Message written to Error Queue");
		}
		catch (JMSException e) {
			logger.error("Failed to write to Error Queue", e);
		}
	}

	private String writeMsg(javax.jms.Message msg) throws JMSException {
		String rtnMessageId = null;
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getBean(factory,
				"unHandledOutputJmsTemplate");
		MessageConverter converter = jmsTemplate.getMessageConverter();
		try {
			jmsTemplate.setMessageConverter(new PassThroughMessageConverter());
			// Ask the QueueSender to send the message we have created
			logger.info("Sending the message to " + jmsTemplate.getDefaultDestination());
			jmsTemplate.convertAndSend(jmsTemplate.getDefaultDestination(), msg);

			rtnMessageId = msg.getJMSMessageID();
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}
		finally {
			jmsTemplate.setMessageConverter(converter);
		}
		return rtnMessageId;
	}
}
