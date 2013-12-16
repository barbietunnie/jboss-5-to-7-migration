package com.legacytojava.message.bo;

import java.sql.Timestamp;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

@Component("suspendBo")
@Scope(value="prototype")
@Lazy(true)
public class SuspendBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SuspendBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddrDao emailAddrDao;
	@Autowired
	private MsgInboxDao msgInboxDao;

	/**
	 * Suspend email addresses. The column "DataTypeValues" from MsgAction table
	 * contains address types (FROM, TO, etc) that to be suspended.
	 * 
	 * @return a Long value representing the number of addresses that have been
	 *         suspended.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (taskArguments == null || taskArguments.trim().length() == 0) {
			throw new DataValidationException("Arguments is not valued, nothing to suspend");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// example: $FinalRcpt,$OriginalRcpt,badaddress@badcompany.com
		long addrsSuspended = 0;
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		StringTokenizer st = new StringTokenizer(taskArguments, ",");
		while (st.hasMoreTokens()) {
			String addrs = null;
			String token = st.nextToken();
			if (token != null && token.startsWith("$")) { // address type
				token = token.substring(1);
				if (EmailAddressType.FROM_ADDR.equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (EmailAddressType.FINAL_RCPT_ADDR.equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (EmailAddressType.ORIG_RCPT_ADDR.equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (EmailAddressType.FORWARD_ADDR.equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (EmailAddressType.TO_ADDR.equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (EmailAddressType.REPLYTO_ADDR.equals(token)) {
					addrs = messageBean.getReplytoAsString();
				}
			}
			else { // real email address
				addrs = token;
			}
			
			if (!StringUtil.isEmpty(addrs)) {
				try {
					InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
					addrs = null;
				}
			}
			if (StringUtil.isEmpty(addrs)) {
				// just for safety
				continue;
			}
			if (isDebugEnabled) {
				logger.debug("Address(es) to suspend: " + addrs);
			}
			StringTokenizer st2 = new StringTokenizer(addrs, ",");
			while (st2.hasMoreTokens()) {
				String addr = st2.nextToken();
				EmailAddrVo emailAddrVo = emailAddrDao.getByAddress(addr);
				if (emailAddrVo != null && !StatusIdCode.SUSPENDED.equals(emailAddrVo.getStatusId())) {
					if (isDebugEnabled)
						logger.debug("Suspending EmailAddr: " + addr);
					emailAddrVo.setStatusId(StatusIdCode.SUSPENDED);
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
					emailAddrVo.setStatusChangeTime(updtTime);
					emailAddrDao.update(emailAddrVo);
					addrsSuspended++;
				}
			}
		} // end of while loop
		// if failed to suspend any address, check if MsgRefId is valued
		if (addrsSuspended == 0 && messageBean.getMsgRefId() != null) {
			// -- this has been taken care of by MessageParser
			//addrsSuspended = suspendByMsgRefId(messageBean, updtTime);
		}
		return Long.valueOf(addrsSuspended);
	}

	int suspendByMsgRefId(MessageBean messageBean, Timestamp updtTime) {
		int addrsSuspended = 0;
		if (messageBean.getMsgRefId() == null) {
			return addrsSuspended;
		}
		// find the "to address" by MsgRefId and suspend it
		long msgId = messageBean.getMsgRefId().longValue();
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.warn("Failed to find MsgInbox record by MsgId: " + msgId);
		}
		else if (!RuleNameType.SEND_MAIL.toString().equals(msgInboxVo.getRuleName())) {
			logger.error("Message from MsgRefId is not a 'SEND_MAIL', ignored." + LF
					+ messageBean);
		}
		else if (msgInboxVo.getToAddrId() != null) { // should always valued
			long toAddr = msgInboxVo.getToAddrId().longValue();
			EmailAddrVo emailAddrVo = emailAddrDao.getByAddrId(toAddr);
			if (!StatusIdCode.SUSPENDED.equals(emailAddrVo.getStatusId())) {
				if (isDebugEnabled)
					logger.debug("Suspending EmailAddr: " + emailAddrVo.getEmailAddr());
				emailAddrVo.setStatusId(StatusIdCode.SUSPENDED);
				emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				emailAddrVo.setStatusChangeTime(updtTime);
				emailAddrDao.update(emailAddrVo);
				addrsSuspended++;
			}
		}
		return addrsSuspended;
	}
	public void setJmsProcessor(JmsProcessor jmsProcessor) {
		// dummy implementation to satisfy the interface
	}
}
