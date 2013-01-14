package com.legacytojava.mailsender.ejb;
import java.io.IOException;

import javax.ejb.Remote;
import javax.mail.MessagingException;

import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Remote
public interface MailSenderRemote {
	public void send(MessageBean msgBean) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException;

	public void send(byte[] msgStream) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException;

	public EmailAddrVo findByAddress(String address);
}
