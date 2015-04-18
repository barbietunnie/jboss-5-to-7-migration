package com.es.mailsender.ejb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import jpa.message.MessageBean;
import jpa.service.msgout.SmtpException;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.es.tomee.util.TomeeCtxUtil;

/**
 * Message-Driven Bean implementation class for: MailSenderMDB
 *
 */
@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") 
		,@ActivationConfigProperty(propertyName = "destination", propertyValue = "mailOutboxQueue")
		,@ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "4")
		},
	mappedName = "mailOutboxQueue")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Resource(name = "msgdb_pool",mappedName = "jdbc/MessageDS",
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@EJB(name="MailSender",beanInterface=MailSenderLocal.class)
public class MailSenderMDB implements MessageListener {
	static final Logger logger = Logger.getLogger(MailSenderMDB.class);
	final String LF = System.getProperty("line.separator", "\n");
	
    @Resource
    private MessageDrivenContext messageContext;

	@Resource
    private ConnectionFactory connectionFactory;

    @Resource(name = "mailErrorQueue")
    private Queue errorQueue;

    private static final int MAX_DELIVERY_COUNT = 4;

    /**
     * Default constructor. 
     */
    public MailSenderMDB() {
    	logger.info("In MailSenderMDB.constructor().");
    }

    /**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		logger.debug("Message Driven Bean got a message: " + LF + message);

		try {
			int deliveryCount = 0;
			Enumeration<?> properties_enu;
			if (message instanceof ActiveMQMessage) {
				ActiveMQMessage msg = (ActiveMQMessage) message;
				deliveryCount = msg.getRedeliveryCounter() + 1;
				logger.info("ActiveMQ RedeliveryCounter: " + msg.getRedeliveryCounter());
				properties_enu = msg.getAllPropertyNames();
			}
			else {
				properties_enu = message.getPropertyNames();
			}
			//if (message.getJMSRedelivered()) {
				// find vendor specific JMS delivery count property
				while (properties_enu.hasMoreElements()) {
					String propName = (String) properties_enu.nextElement();
					if (StringUtils.isBlank(propName)) {
						continue;
					}
					logger.info("JMS Property name: " + propName + " = " + message.getObjectProperty(propName));
					Pattern p = Pattern.compile("jms\\w{0,}delivery\\w{0,}count", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher m = p.matcher(propName);
					if (m.find()) {
						deliveryCount = message.getIntProperty(propName);
						logger.info(propName + ": " + deliveryCount);
					}
				}
			//}
			if (deliveryCount > 1) {
				if (MAX_DELIVERY_COUNT > 0 && deliveryCount > MAX_DELIVERY_COUNT) {
					sendToErrorQueue(message, new JMSException(
							"DeliveryCount exceeded Listener's maximum: "
									+ MAX_DELIVERY_COUNT));
					return;
				}
			}
		}
		catch (JMSException e) {
			logger.error("JMSException caught", e);
		}
		
		MessageBean msgBean = null;
		try {
			MailSenderLocal mailSender = (MailSenderLocal) TomeeCtxUtil.getInitialContext().lookup(
					"java:global/WebContent/MailSender!com.es.mailsender.ejb.MailSenderLocal");
			logger.info("MailSender instance: " + mailSender);
			
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
			else if (message instanceof TextMessage) {
				logger.warn("A TextMessage received: " + ((TextMessage)message).getText());
				sendToErrorQueue(message, new Exception("Not expected message type"));
			}
		}
		catch (NamingException ne) {
			logger.error("NamingException caught", ne);
			messageContext.setRollbackOnly();
			throw new EJBException(ne.getMessage());
		}
		catch (IOException ie) {
			logger.error("IOException caught", ie);
			messageContext.setRollbackOnly();
			throw new EJBException(ie.toString());
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
			
			final Connection connection = connectionFactory.createConnection();
			
			connection.start();
			logger.info("In MailSenderMDBTest.testMDB() - JMS Connection started");
			
	        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	        final MessageProducer producer = session.createProducer(errorQueue);
	        
	        producer.send(message);

			logger.info("Message written to Error Queue");
		}
		catch (JMSException e) {
			logger.error("Failled to send message to error queue - ", e);
		}
	}

}
