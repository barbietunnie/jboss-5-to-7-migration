package jpa.service.msgout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.message.MessageRendered;
import jpa.service.message.MessageRenderedService;
import jpa.util.EmailSender;
import jpa.util.SpringUtil;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class to send the email off.
 * 
 * @author Jack Wang
 */
@Component("mailSenderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MailSenderBo extends MailSenderBase {
	static final Logger logger = Logger.getLogger(MailSenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * must be a no-argument constructor.
	 */
	public MailSenderBo() {
		super();
	}

	public static void main(String[] args) {
		MailSenderBo sender = (MailSenderBo) SpringUtil.getAppContext().getBean("mailSenderBo");
		MsgOutboxBo msgOutboxBo = (MsgOutboxBo) SpringUtil.getAppContext().getBean("msgOutboxBo");
		MessageRenderedService msgRenderedService = (MessageRenderedService) SpringUtil.getAppContext().getBean("messageRenderedService");
		SpringUtil.beginTransaction();
		try {
			MessageRendered mr = msgRenderedService.getFirstRecord();
			MessageBean bean = msgOutboxBo.getMessageByPK(mr.getRowId());
			if (bean.getTo()==null || bean.getTo().length==0) {
				bean.setTo(InternetAddress.parse("testto@localhost"));
			}
			logger.info("MessageBean retrieved:\n" + bean);
			sender.process(new MessageContext(bean));
			SpringUtil.commitTransaction();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		}
	}

	/**
	 * Process the request. The message context should contain an email
	 * message represented by either a MessageBean or a ByteStream.
	 * 
	 * @param req - a message bean or a message stream
	 * @throws IOException 
	 * @throws SmtpException 
	 */
	public void process(MessageContext req) throws SmtpException, IOException {
		try {
			processMessage(req);
		}
		catch (DataValidationException dex) {
			// failed to send the message
			logger.error("DataValidationException caught", dex);
		}
		catch (MessagingException mex) {
			logger.error("MessagingException caught", mex);
		}
		catch (NullPointerException en) {
			logger.error("NullPointerException caught", en);
		}
		catch (IndexOutOfBoundsException eb) {
			// AddressException from InternetAddress.parse() caused this
			// Exception to be thrown
			// write the original message to error queue
			logger.error("IndexOutOfBoundsException caught", eb);
		}
		catch (NumberFormatException ef) {
			logger.error("NumberFormatException caught", ef);
			// send error notification
			EmailSender.sendEmail(null, ef.getMessage(),
					ExceptionUtils.getStackTrace(ef),
					EmailSender.EmailList.ToDevelopers);
		}
		catch (SmtpException se) {
			logger.error("SmtpException caught", se);
			// SMTP error, roll back and exit
			throw se;
		} 
		finally {
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
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public void sendMail(javax.mail.Message msg, boolean isSecure, Map<String, Address[]> errors)
			throws MessagingException, IOException, SmtpException {
		NamedPool smtp = SmtpWrapperUtil.getSmtpNamedPool();
		NamedPool secu = SmtpWrapperUtil.getSecuNamedPool();
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
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void sendMail(javax.mail.Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException {
		NamedPool smtp = SmtpWrapperUtil.getSmtpNamedPool();
		if (smtp.isEmpty()) {
			smtp = SmtpWrapperUtil.getSecuNamedPool();
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