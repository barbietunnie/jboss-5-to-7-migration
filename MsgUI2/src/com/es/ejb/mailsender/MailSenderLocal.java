package com.es.ejb.mailsender;
import java.io.IOException;

import javax.ejb.Local;

import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.msgout.SmtpException;

@Local
public interface MailSenderLocal {
	public void send(MessageBean msgBean) throws IOException, SmtpException;

	public void send(byte[] msgStream) throws IOException, SmtpException;

	public EmailAddress findByAddress(String address);
}
