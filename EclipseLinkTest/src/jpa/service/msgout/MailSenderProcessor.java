package jpa.service.msgout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import jpa.exception.DataValidationException;
import jpa.message.MessageContext;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * process queue messages handed over by QueueListener
 * 
 * @author Administrator
 */
public class MailSenderProcessor extends MailSenderBase {
	static final Logger logger = Logger.getLogger(MailSenderProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * must use constructor without any parameters
	 */
	public MailSenderProcessor() {
		logger.info("Entering constructor...");
	}

	protected AbstractApplicationContext loadFactory() {
		return SpringUtil.getAppContext();
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
	public void process(MessageContext req) throws IOException, MessagingException,
			InterruptedException, SmtpException {
		if (req == null) {
			logger.error("a null request was received.");
			return;
		}
		if (req.getMessages()==null || req.getMessages().length==0) {
			logger.error("Request received was not a JMS Message.");
			throw new IllegalArgumentException("Request was not a JMS Message as expected.");
		}

		if (msgInboxBo == null) { // first time 
			loadBosAndDaos();
		}
		
		// define transaction properties
		SpringUtil.startTransaction();

		// defined here to be used in catch blocks
		try {
			if (req.getMessageBean()!=null) {
				process(req.getMessageBean());
			}
			else if (req.getMessageStream()!=null) {
				// SMTP raw stream
				process(req.getMessageStream());
			}
			else {
				logger.error("message was not a message type as expected");
			}
			SpringUtil.commitTransaction();
		}
		catch (InterruptedException e) {
			logger.error("MailSenderProcessor thread was interrupted. Process exiting...");
			SpringUtil.rollbackTransaction(); // message will be re-delivered
		}
		catch (DataValidationException dex) {
			// failed to send the message
			logger.error("DataValidationException caught", dex);
			SpringUtil.commitTransaction();
		}
		catch (AddressException ae) {
			logger.error("AddressException caught", ae);
			SpringUtil.commitTransaction();
		}
		catch (MessagingException mex) {
			// failed to send the message
			logger.error("MessagingException caught", mex);
			SpringUtil.commitTransaction();
		}
		catch (NullPointerException en) {
			logger.error("NullPointerException caught", en);
			SpringUtil.commitTransaction();		}
		catch (IndexOutOfBoundsException eb) {
			// AddressException from InternetAddress.parse() caused this
			// Exception to be thrown
			// write the original message to error queue
			logger.error("IndexOutOfBoundsException caught", eb);
			SpringUtil.commitTransaction();		}
		catch (NumberFormatException ef) {
			logger.error("NumberFormatException caught", ef);
			// TODO send error notification
			SpringUtil.commitTransaction();
		}
		catch (SmtpException se) {
			logger.error("SmtpException caught", se);
			// SMTP error, roll back and exit
			SpringUtil.rollbackTransaction();
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
}