package com.legacytojava.message.bo;

import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class BounceBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(BounceBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private EmailAddrDao emailAddrDao;

	/**
	 * Increase the bounce count to the email addresses involved. The column
	 * "DataTypeValues" from MsgAction table should contain address types (FROM,
	 * TO, etc) that need to be updated (bounce count increment).
	 * 
	 * @return a Long representing the number of addresses updated.
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
		
		// example: $FinalRcpt,$OriginalRcpt,badaddress@baddomain.com
		long addrsUpdated = 0;
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
			else { // address
				addrs = token;
			}
			
			Address[] iAddrs = null;
			if (StringUtils.isNotBlank(addrs)) {
				try {
					iAddrs = InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
				}
			}
			if (isDebugEnabled) {
				logger.debug("Address(es) to increase bounce count: " + addrs);
			}
			for (int i=0; iAddrs!=null && i<iAddrs.length; i++) {
				Address iAddr = iAddrs[i];
				String addr = iAddr.toString();
				EmailAddrVo emailAddrVo = emailAddrDao.getByAddress(addr);
				if (emailAddrVo != null) {
					if (isDebugEnabled) {
						logger.debug("Increasing bounce count to EmailAddr: " + addr);
					}
					emailAddrDao.updateBounceCount(emailAddrVo);
					addrsUpdated++;
				}
				else {
					if (isDebugEnabled) {
						logger.debug("Address (" + addr + ") does not exist, failed to increase bounce count!");
					}
				}
			}
		}
		return Long.valueOf(addrsUpdated);
	}

	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}

	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}
}
