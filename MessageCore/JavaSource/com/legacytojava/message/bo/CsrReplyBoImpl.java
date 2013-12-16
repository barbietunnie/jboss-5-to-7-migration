package com.legacytojava.message.bo;

import javax.jms.JMSException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.StringUtil;

@Component("csrReplyBo")
@Scope(value="prototype")
@Lazy(true)
public class CsrReplyBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(CsrReplyBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * The input MessageBean should contain the CSR reply message plus the
	 * original message. If the input bean's getTo() method returns a null,
	 * get it from the original message by issuing getFrom() method.
	 * 
	 * @param messageBean -
	 *            the original email must be saved via setOriginalMail() before
	 *            calling this method.
	 * @return a Long value representing number of addresses the message has
	 *         been replied to.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException,
			AddressException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
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
		if (StringUtil.isEmpty(messageBean.getClientId())) {
			messageBean.setClientId(messageBean.getOriginalMail().getClientId());
		}
		messageBean.setCustId(messageBean.getOriginalMail().getCustId());
		if (isDebugEnabled) {
			logger.debug("Address(es) to reply to: " + messageBean.getToAsString());
		}
		
		// write to MailSender input queue
		setTargetToMailSender();
		messageBean.setMsgRefId(messageBean.getOriginalMail().getMsgId());
		String jmsMsgId = jmsProcessor.writeMsg(messageBean);
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		}
		return Long.valueOf(messageBean.getTo().length);
	}
}
