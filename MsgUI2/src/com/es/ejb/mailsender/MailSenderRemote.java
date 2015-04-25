package com.es.ejb.mailsender;
import java.io.IOException;

import javax.ejb.Remote;

import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.msgout.SmtpException;

@Remote
public interface MailSenderRemote {
	public void send(MessageBean msgBean) throws IOException, SmtpException;

	public void send(byte[] msgStream) throws IOException, SmtpException;

	public void send(String fromAddr, String toAddr, String subject, String body) throws IOException, SmtpException;
	
	public EmailAddress findByAddress(String address);
}
