package com.es.bo.task;

import java.io.IOException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.sender.MailSenderBo;
import com.es.bo.smtp.SmtpException;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;

@Component("csrReplyMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class CsrReplyMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 50896288572118819L;
	static final Logger logger = Logger.getLogger(CsrReplyMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailSenderBo mailSenderBo;

	/**
	 * The input MessageBean should contain the CSR reply message plus the
	 * original message. If the input bean's getTo() method returns a null,
	 * get it from the original message's From address.
	 * 
	 * @param messageBean -
	 *            the original email must be saved via setOriginalMail() before
	 *            calling this method.
	 * @return a Long value representing number of addresses the message has
	 *         been replied to.
	 * @throws IOException 
	 */
	public Long process(MessageContext ctx) throws DataValidationException,
			AddressException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (messageBean.getOriginalMail()==null) {
			throw new DataValidationException("Original MessageBean is null");
		}
		if (messageBean.getOriginalMail().getMsgId()==null) {
			throw new DataValidationException("Original MessageBean's MsgId is null");
		}
		
		if (messageBean.getTo() == null) {
			// validate the TO address, just for safety
			InternetAddress.parse(messageBean.getOriginalMail().getFromAsString());
			messageBean.setTo(messageBean.getOriginalMail().getFrom());
		}
		if (messageBean.getFrom() == null) {
			messageBean.setFrom(messageBean.getOriginalMail().getTo());
		}
		if (StringUtils.isBlank(messageBean.getSenderId())) {
			messageBean.setSenderId(messageBean.getOriginalMail().getSenderId());
		}
		messageBean.setSubrId(messageBean.getOriginalMail().getSubrId());
		if (isDebugEnabled) {
			logger.debug("Address(es) to reply to: " + messageBean.getToAsString());
		}
		
		// write to MailSender input queue
		messageBean.setMsgRefId(messageBean.getOriginalMail().getMsgId());
		// send the reply off
		try {
			mailSenderBo.process(ctx);
			if (isDebugEnabled) {
				logger.debug("Message replied to: " + messageBean.getToAsString());
			}
		}
		catch (SmtpException e) {
			throw new IOException(e.getMessage(), e);
		}
		return Long.valueOf(messageBean.getTo().length);
	}
}
