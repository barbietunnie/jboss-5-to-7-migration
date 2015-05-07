package com.es.ejb.mailsender;
import javax.ejb.Local;

import jpa.message.MessageBean;
import jpa.model.EmailAddress;

@Local
public interface MailSenderLocal {
	public void send(MessageBean msgBean);

	public void send(byte[] msgStream);

	public void send(String fromAddr, String toAddr, String subject, String body);
	
	public void sendMailToSite(String siteId, String fromAddr, String subject, String body);
	
	public EmailAddress findByAddress(String address);
}
