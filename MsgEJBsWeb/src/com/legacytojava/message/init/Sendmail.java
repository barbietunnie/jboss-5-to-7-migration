package com.legacytojava.message.init;

import java.io.IOException;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.legacytojava.message.ejb.sendmail.SendMailLocal;
import com.legacytojava.message.exception.DataValidationException;

public class Sendmail {
	protected static final Logger logger = Logger.getLogger(Sendmail.class);

	//@EJB(beanInterface=SendMailRemote.class)
	private SendMailLocal sendMail;

	public Sendmail() {
		sendMail = (SendMailLocal) LookupUtil.lookupLocalEjb("java:app/MsgEJBs/SendMail!com.legacytojava.message.ejb.sendmail.SendMailLocal");
	}

	public int sendMailFromSite(String siteId, String toAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException {
		long emailsSent = sendMail.sendMailFromSite(siteId, toAddr, subject, body);
		logger.info("sendMailFromSite() - Emails Sent: " + emailsSent);
		return (int) emailsSent;
	}

	public int sendMailToSite(String siteId, String fromAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException {
		long emailsSent = sendMail.sendMailToSite(siteId, fromAddr, subject, body);
		logger.info("sendMailToSite() - Emails Sent: " + emailsSent);
		return (int) emailsSent;
	}

	public int sendMail(String fromAddr, String toAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException {
		long emailsSent = sendMail.sendMail(fromAddr, toAddr, subject, body);
		logger.info("sendMail() - Emails Sent: " + emailsSent);
		return (int) emailsSent;
	}

	public static void main(String[] args) {
		Sendmail smail = new Sendmail();
		try {
			smail.sendMail("jsmith@test.com", "test@test.com", "test subject from axis", "test body text");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
