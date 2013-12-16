package com.legacytojava.message.bo;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Component("sendMailBo")
@Scope(value="prototype")
@Lazy(true)
public class SendMailBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SendMailBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddrDao emailAddrDao;

	/**
	 * Send the email off by writing the MessageBean object to MailSender input
	 * queue.
	 * 
	 * @param messageBean -
	 *            MsgRefId that links to a received message must be populated.
	 * @return a Long value representing number of addresses the message has
	 *         been sent to.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException,
			AddressException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (messageBean.getMsgRefId()==null) {
			logger.warn("messageBean.getMsgRefId() returned null");
			//throw new DataValidationException("messageBean.getMsgRefId() returned null");
		}
		
		if (isDebugEnabled) {
			logger.debug("Sending email to: " + messageBean.getToAsString());
		}
		setTargetToMailSender();
		// validate the TO address, just for safety
		Address[] addrs = InternetAddress.parse(messageBean.getToAsString());
		int mailsSent = 0;
		for (Address addr : addrs) {
			if (addr == null) continue;
			EmailAddrVo vo = emailAddrDao.findByAddress(addr.toString());
			if (StatusIdCode.ACTIVE.equalsIgnoreCase(vo.getStatusId())) {
				String jmsMsgId = jmsProcessor.writeMsg(messageBean);
				mailsSent++;
				if (isDebugEnabled)
					logger.debug("Jms Message Id returned: " + jmsMsgId);
			}
		}
		return Long.valueOf(mailsSent);
	}
}
