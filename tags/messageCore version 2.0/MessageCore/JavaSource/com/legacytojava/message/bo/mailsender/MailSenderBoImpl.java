package com.legacytojava.message.bo.mailsender;

import java.io.IOException;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.pool.NamedPools;
import com.legacytojava.jbatch.smtp.SmtpConnection;
import com.legacytojava.jbatch.smtp.SmtpException;

/**
 * process queue messages handed over by QueueListener.
 * 
 * @author Administrator
 */
public class MailSenderBoImpl extends MailSenderBase {
	static final Logger logger = Logger.getLogger(MailSenderBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	/**
	 * constructor
	 */
	public MailSenderBoImpl() {
		if (isDebugEnabled)
			logger.debug("Entering constructor...");
		loadBosAndDaos();
	}

	protected AbstractApplicationContext loadFactory() {
		return SpringUtil.getAppContext();
	}
	
	/**
	 * send the email off via SMTP server.
	 * 
	 * @param msg -
	 *            a JavaMail message object
	 * @param isSecure -
	 *            true to send via secure SMTP server
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
		/* Send the Message */
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
	 * send the email off via unsecured SMTP server.
	 * 
	 * @param msg -
	 *            a JavaMail message object
	 * @throws InterruptedException 
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void sendMail(javax.mail.Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException, InterruptedException {
		NamedPools smtp = SmtpWrapperUtil.getSmtpNamedPools();
		if (smtp.isEmpty()) {
			// for secure server only shop
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