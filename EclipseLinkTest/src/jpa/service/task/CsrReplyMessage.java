package jpa.service.task;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("csrReplyMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class CsrReplyMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(CsrReplyMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * The input MessageBean should contain the CSR reply message plus the
	 * original message. If the input bean's getTo() method returns a null,
	 * get it from the original message's From address.
	 * 
	 * @param messageBean -
	 *            the original email must be saved via setOriginalMail() before
	 *            calling this method.
	 * @return a Integer value representing number of addresses the message has
	 *         been replied to.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException,
			AddressException {
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
		if (StringUtils.isBlank(messageBean.getSenderId())) {
			messageBean.setSenderId(messageBean.getOriginalMail().getSenderId());
		}
		messageBean.setSubrId(messageBean.getOriginalMail().getSubrId());
		if (isDebugEnabled) {
			logger.debug("Address(es) to reply to: " + messageBean.getToAsString());
		}
		
		// write to MailSender input queue
		messageBean.setMsgRefId(messageBean.getOriginalMail().getMsgId());
		// TODO send the reply off
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: ");
		}
		return Integer.valueOf(messageBean.getTo().length);
	}
}
