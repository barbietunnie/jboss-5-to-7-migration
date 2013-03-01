package com.legacytojava.message.bo.mailsender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.legacytojava.jbatch.JbMain;
import com.legacytojava.jbatch.Processor;
import com.legacytojava.jbatch.pool.NamedPools;
import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.jbatch.smtp.SmtpConnection;
import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;

/**
 * process queue messages handed over by QueueListener
 * 
 * @author Administrator
 */
public class MailSenderProcessor extends MailSenderBase implements Processor {
	static final Logger logger = Logger.getLogger(MailSenderProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private JmsProcessor jmsProcessor;
	private JmsTransactionManager jmsTransactionManager;
	private Queue errorQueue;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * must use constructor without any parameters
	 */
	public MailSenderProcessor() {
		logger.info("Entering constructor...");
	}

	protected AbstractApplicationContext loadFactory() {
		return JbMain.getBatchAppContext();
	}

	/**
	 * process request. Either a ObjectMessage contains a MessageBean or a
	 * BytesMessage contains a SMTP raw stream.
	 * 
	 * @param req -
	 *            a JMS message
	 * @throws IOException 
	 * @throws JMSException 
	 * @throws InterruptedException 
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void process(Object req) throws IOException, JMSException, MessagingException,
			SmtpException, InterruptedException {
		if (req == null) {
			logger.error("a null request was received.");
			return;
		}
		if (!(req instanceof Message)) {
			logger.error("Request received was not a JMS Message.");
			throw new IllegalArgumentException("Request was not a JMS Message as expected.");
		}

		if (msgInboxBo == null) { // first time 
			loadBosAndDaos();
		}
		
		// define transaction properties
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus tStatus = jmsTransactionManager.getTransaction(definition);
		jmsProcessor.getJmsTemplate().setDefaultDestination(errorQueue);

		boolean isJmsMsgWritten = false;
		// defined here to be used in catch blocks
		MessageBean msgBean = null;
		Message message = (Message) req;
		try {
			if (message instanceof ObjectMessage) {
				Object msgObj = ((ObjectMessage) message).getObject();
				if (msgObj == null) {
					throw new MessageFormatException("Object Message is Null.");
				}
				if (msgObj instanceof MessageBean) {
					msgBean = (MessageBean) msgObj;
					logger.info("A MessageBean object received.");
					
					process(msgBean);
				}
				else {
					logger.error("message was not a MessageBean as expected" + LF + message);
					jmsProcessor.writeJmsMsg(message, true);
					isJmsMsgWritten = true;
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
				
				process(mailStream);
			}
			else {
				logger.error("message was not a message type as expected" + LF + message);
				jmsProcessor.writeJmsMsg(message, true);
				isJmsMsgWritten = true;
			}
			// issue commit if any message has been written to queue.
			if (isJmsMsgWritten) {
				jmsTransactionManager.commit(tStatus);
				logger.info("Messages written to Queue were committed.");
			}
		}
		catch (InterruptedException e) {
			logger.error("MailSenderProcessor thread was interrupted. Process exiting...");
			tStatus.setRollbackOnly(); // message will be re-delivered
		}
		catch (DataValidationException dex) {
			// failed to send the message
			logger.error("DataValidationException caught", dex);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (AddressException ae) {
			logger.error("AddressException caught", ae);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (MessagingException mex) {
			// failed to send the message
			logger.error("MessagingException caught", mex);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (MessageFormatException em) {
			logger.error("MessageFormatException caught", em);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (NullPointerException en) {
			logger.error("NullPointerException caught", en);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (IndexOutOfBoundsException eb) {
			// AddressException from InternetAddress.parse() caused this
			// Exception to be thrown
			// write the original message to error queue
			logger.error("IndexOutOfBoundsException caught", eb);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (NumberFormatException ef) {
			logger.error("NumberFormatException caught", ef);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (JMSException je) {
			logger.error("JMSException caught", je);
			// other JMS error, roll back and exit
			tStatus.setRollbackOnly();
			logger.error("JMSException caught", je);
			Exception e = je.getLinkedException();
			if (e != null) {
				logger.error("linked errortion", e);
			}
			throw je;
		}
		catch (SmtpException se) {
			logger.error("SmtpException caught", se);
			// SMTP error, roll back and exit
			tStatus.setRollbackOnly();
			throw se;
		}
	}

	/**
	 * Send the email off. <p>
	 * SMTP server properties are retrieved from database. 
	 * 
	 * @param msg -
	 *            message
	 * @param isSecure -
	 *            send via secure SMTP server when true
	 * @param errors -
	 *            contains delivery errors if any
	 * @throws InterruptedException
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public void sendMail(javax.mail.Message msg, boolean isSecure, Map<String, Address[]> errors)
			throws MessagingException, IOException, SmtpException, InterruptedException {
		NamedPools smtp = SmtpWrapperUtil.getSmtpNamedPools();
		NamedPools secu = SmtpWrapperUtil.getSecuNamedPools();
		/* Send Message */
		SmtpConnection smtp_conn = null;
		if (isSecure && !secu.isEmpty() || smtp.isEmpty()) {
			try {
				smtp_conn = (SmtpConnection) secu.getConnection();
				smtp_conn.sendMail(msg, errors);
			}
			finally {
				if (smtp_conn != null) {
					secu.returnConnection(smtp_conn);
				}
			}
		}
		else {
			try {
				smtp_conn = (SmtpConnection) smtp.getConnection();
				smtp_conn.sendMail(msg, errors);
			}
			finally {
				if (smtp_conn != null) {
					smtp.returnConnection(smtp_conn);
				}
			}
		}
	}

	/**
	 * Send the email off via unsecured SMTP server. <p>
	 * SMTP server properties are retrieved from database. 
	 * 
	 * @param msg -
	 *            message
	 * @throws InterruptedException 
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void sendMail(javax.mail.Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException, InterruptedException {
		NamedPools smtp = SmtpWrapperUtil.getSmtpNamedPools();
		if (smtp.isEmpty()) {
			smtp = SmtpWrapperUtil.getSecuNamedPools();
		}
		SmtpConnection smtp_conn = null;
		try {
			smtp_conn = (SmtpConnection) smtp.getConnection();
			smtp_conn.sendMail(msg, errors);
		}
		finally {
			if (smtp_conn != null) {
				smtp.returnConnection(smtp_conn);
			}
		}
	}
	
	public JmsProcessor getJmsProcessor() {
		return jmsProcessor;
	}

	public void setJmsProcessor(JmsProcessor jmsProcessor) {
		this.jmsProcessor = jmsProcessor;
	}

	public JmsTransactionManager getJmsTransactionManager() {
		return jmsTransactionManager;
	}

	public void setJmsTransactionManager(JmsTransactionManager transactionManager) {
		this.jmsTransactionManager = transactionManager;
	}

	public Queue getErrorQueue() {
		return errorQueue;
	}

	public void setErrorQueue(Queue errorQueue) {
		this.errorQueue = errorQueue;
	}
}