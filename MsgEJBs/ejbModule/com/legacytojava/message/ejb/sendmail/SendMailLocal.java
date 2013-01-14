package com.legacytojava.message.ejb.sendmail;
import java.io.IOException;
import java.text.ParseException;

import javax.ejb.Local;
import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.exception.DataValidationException;

@Local
public interface SendMailLocal {
	public long saveRenderData(RenderResponse rsp) throws DataValidationException;
	public long saveMessage(MessageBean messageBean) throws DataValidationException;
	public MessageBean getMessageByPK(long renderId) throws AddressException,
	DataValidationException, ParseException;
	public long sendMail(MessageBean messageBean) throws DataValidationException,
	MessagingException, JMSException, IOException;
	public long sendMailFromSite(String siteId, String toAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException;
	public long sendMailToSite(String siteId, String fromAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException;
	public long sendMail(String fromAddr, String toAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException;
}
