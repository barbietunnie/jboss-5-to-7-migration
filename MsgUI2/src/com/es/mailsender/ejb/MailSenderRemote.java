package com.es.mailsender.ejb;
import java.io.IOException;

import javax.ejb.Remote;
import javax.mail.MessagingException;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.msgout.SmtpException;

@Remote
public interface MailSenderRemote {
	public void send(MessageBean msgBean) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException;

	public void send(byte[] msgStream) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException;

	public EmailAddress findByAddress(String address);
}
