package jpa.service.msgout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.message.MessageRendered;
import jpa.service.message.MessageRenderedService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * process queue messages handed over by QueueListener
 * 
 * @author Administrator
 */
@Component("mailSenderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MailSenderBo extends MailSenderBase {
	static final Logger logger = Logger.getLogger(MailSenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * must use constructor without any parameters
	 */
	public MailSenderBo() {
		super();
	}

	public static void main(String[] args) {
		MailSenderBo sender = (MailSenderBo) SpringUtil.getAppContext().getBean("mailSenderBo");
		MsgOutboxBo msgOutboxBo = (MsgOutboxBo) SpringUtil.getAppContext().getBean("msgOutboxBo");
		MessageRenderedService msgRenderedService = (MessageRenderedService) SpringUtil.getAppContext().getBean("messageRenderedService");
		SpringUtil.startTransaction();
		try {
			MessageRendered mr = msgRenderedService.getFirstRecord();
			MessageBean bean = msgOutboxBo.getMessageByPK(mr.getRowId());
			if (bean.getTo()==null || bean.getTo().length==0) {
				bean.setTo(InternetAddress.parse("testto@localhost"));
			}
			System.out.println("MessageBean retrieved:\n" + bean);
			sender.process(bean);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			SpringUtil.commitTransaction();
		}
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
	public void process(MessageContext req) throws SmtpException, IOException, InterruptedException {
		if (req == null) {
			logger.error("a null request was received.");
			return;
		}
		if (req.getMessageBean()==null && req.getMessageStream()==null) {
			throw new IllegalArgumentException("Request did not contain a MessageBean nor a MessageStream.");
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
			SpringUtil.commitTransaction();
		}
		catch (IndexOutOfBoundsException eb) {
			// AddressException from InternetAddress.parse() caused this
			// Exception to be thrown
			// write the original message to error queue
			logger.error("IndexOutOfBoundsException caught", eb);
			SpringUtil.commitTransaction();
		}
		catch (NumberFormatException ef) {
			logger.error("NumberFormatException caught", ef);
			// TODO send error notification
			SpringUtil.commitTransaction();
		}
		catch (InterruptedException e) {
			logger.error("MailSenderBo thread was interrupted. Process exiting...");
			SpringUtil.rollbackTransaction(); // message will be re-delivered
			throw e;
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