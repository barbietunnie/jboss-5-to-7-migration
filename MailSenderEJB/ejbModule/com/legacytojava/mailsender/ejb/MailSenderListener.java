package com.legacytojava.mailsender.ejb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import javax.mail.internet.AddressException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.jbatch.queue.PassThroughMessageConverter;
import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.ServiceLocator;

/**
 * Message-Driven Bean implementation class for: MailSenderListener
 *
 */
@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/mailSenderInput"),
		@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "4")
		},
	mappedName = "mailSenderInput")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Resource(mappedName = "java:jboss/MessageDS",
	name = "jdbc/msgdb_pool",
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@EJB(name="mailSender",beanInterface=MailSenderLocal.class)
public class MailSenderListener implements MessageListener {
	static final Logger logger = Logger.getLogger(MailSenderListener.class);
	private JmsProcessor jmsProcessor = null;
	final String LF = System.getProperty("line.separator", "\n");
	
    @Resource
    private MessageDrivenContext messageContext;

    private static final int MAX_DELIVERY_COUNT = 2;

    /**
     * Default constructor. 
     */
    public MailSenderListener() {
    	jmsProcessor = (JmsProcessor) SpringUtil.getAppContext().getBean("jmsProcessor");
    }
	
    private static Context ctx = null;
    static Context getContext() {
    	if (ctx == null) {
    		try {
    			ctx = ServiceLocator.getInitialContext();
    		}
    		catch (NamingException e) {
    			 throw new RuntimeException("Unable to get initial context", e);
    		}
    	}
    	return ctx;
    }

    /**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		logger.debug("Message Driven Bean got a message " + LF + message);

		try {
			if (message.getJMSRedelivered()) {
				Enumeration<?> enu = message.getPropertyNames();
				while (enu.hasMoreElements()) {
					String propName = (String) enu.nextElement();
					logger.info("JMS Property name: " + propName);
					if (propName == null) {
						continue;
					}
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
		
		MessageBean msgBean = null;
		try {
			 // JNID name "java:comp/env/mailSender" still works in JBoss 7.1
			//MailSenderLocal mailSender = (MailSenderLocal) getContext().lookup("java:comp/env/mailSender");
			MailSenderLocal mailSender = (MailSenderLocal) getContext().lookup("java:module/MailSender!com.legacytojava.mailsender.ejb.MailSenderLocal");
			
			if (message instanceof ObjectMessage) {
				Object msgObj = ((ObjectMessage) message).getObject();
				if (msgObj == null) {
					throw new MessageFormatException("Object Message is Null.");
				}
				if (msgObj instanceof MessageBean) {
					msgBean = (MessageBean) msgObj;
					logger.info("A MessageBean object received.");
					
					mailSender.send(msgBean);
				}
				else {
					logger.error("message was not a MessageBean as expected" + LF + message);
					jmsProcessor.writeJmsMsg(message, true);
				}
			}
			else if (message instanceof BytesMessage) {
				// SMTP raw stream
				BytesMessage msg = (BytesMessage) message;
				logger.info("A BytesMessage received.");
				byte[] buffer = new byte[1024];
				int len = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ( (len = msg.readBytes(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				byte[] mailStream = baos.toByteArray();
				
				mailSender.send(mailStream);
			}
		}
		catch (AddressException ae) {
			logger.error("AddressException caught", ae);
			sendToErrorQueue(message, ae);
		}
		catch (DataValidationException mex) {
			// failed to send the message
			logger.error("DataValidationException caught", mex);
			sendToErrorQueue(message, mex);
		}
		catch (MessagingException mex) {
			// failed to send the message
			logger.error("MessagingException caught", mex);
			sendToErrorQueue(message, mex);
		}
		catch (MessageFormatException em) {
			logger.error("MessageFormatException caught", em);
			sendToErrorQueue(message, em);
		}
		catch (NullPointerException en) {
			logger.error("NullPointerException caught", en);
			sendToErrorQueue(message, en);
		}
		catch (IndexOutOfBoundsException eb) {
			// AddressException from InternetAddress.parse() caused this
			// Exception to be thrown
			// write the original message to error queue
			logger.error("IndexOutOfBoundsException caught", eb);
			sendToErrorQueue(message, eb);
		}
		catch (NumberFormatException ef) {
			logger.error("NumberFormatException caught", ef);
			sendToErrorQueue(message, ef);
		}
		catch (InterruptedException e) {
			logger.error("MailSenderProcessor thread was interrupted. Process exiting...");
			// message will be re-delivered
			messageContext.setRollbackOnly();
			throw new EJBException(e.toString());
		}
		catch (IOException ie) {
			logger.error("IOException caught", ie);
			messageContext.setRollbackOnly();
			throw new EJBException(ie.toString());
		}
		catch (NamingException e) {
			logger.error("A NamingException occurred", e);
			messageContext.setRollbackOnly();
			throw new EJBException(e.toString());
		}
		catch (JMSException je) {
			logger.error("JMSException caught", je);
			// other JMS error, exiting MailSender
			logger.error("JMSException caught", je);
			Exception e = je.getLinkedException();
			if (e != null) {
				logger.error("linked errortion", e);
			}
			messageContext.setRollbackOnly();
			throw new EJBException(je.getMessage());
		}
		catch (SmtpException se) {
			logger.error("SmtpException caught", se);
			messageContext.setRollbackOnly();
			// SMTP error, exiting MailSender
			throw new EJBException(se.getMessage());
		}
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
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
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
