package com.legacytojava.message.ejb.queuereader;

import java.io.ByteArrayOutputStream;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.ActivationConfigProperty;
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
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;

/**
 * Message-Driven Bean implementation class for: RmaRequestInput
 *
 */
@MessageDriven(
		activationConfig = {
			@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
			@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/rmaRequestInput"),
			@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "2")
			}, 
		mappedName = "rmaRequestInput")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Resource(mappedName = "java:jboss/MessageDS",
	name = "jdbc/msgdb_pool",
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
public class RmaRequestInput implements MessageListener {
	static final Logger logger = Logger.getLogger(RmaRequestInput.class);
	@Resource
	private MessageDrivenContext messageContext;
    /**
     * Default constructor. 
     */
    public RmaRequestInput() {
    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		logger.debug("Message Driven Bean got message " + message);
		try {
			Message msg = (Message) message;
			if (msg instanceof ObjectMessage) {
				MessageBean msgBean = (MessageBean) ((ObjectMessage) msg).getObject();
				logger.info("Received a MessageBean object: " + msgBean);
			}
			else if (msg instanceof BytesMessage) {
				BytesMessage bytesMsg = (BytesMessage) msg;
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int len;
				while ((len = bytesMsg.readBytes(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				logger.info("Received a BytesMessage: " + new String(baos.toByteArray()));
			}
			else if (msg instanceof TextMessage) {
				logger.info("Received a TextMessage: " + ((TextMessage) msg).getText());
			}
			else {
				logger.warn("Received a unknown message type: " + msg.getClass().getName());
			}
		}
		catch (JMSException e) {
			logger.error("A JMSException occurred", e);
			messageContext.setRollbackOnly();
			throw new EJBException(e.toString());
		}
    }

}
