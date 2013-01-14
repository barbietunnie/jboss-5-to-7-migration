package com.legacytojava.message.bo;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class ActivateBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ActivateBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private EmailAddrDao emailAddrDao;
	
	/**
	 * Activate email addresses. The column "DataTypeValues" from MsgAction
	 * table should contain address types (FROM, TO, etc) that need to be
	 * activated.
	 * 
	 * @return a Long value representing the number of addresses that have been
	 *         activated.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (taskArguments == null || taskArguments.trim().length() == 0) {
			throw new DataValidationException("Arguments is not valued, nothing to activate");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// example: $From,$To,myaddress@mydomain.com
		long addrsActiveted = 0;
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		List<String> list = TaskBaseAdaptor.getArgumentList(taskArguments);
		for (Iterator<String> it=list.iterator(); it.hasNext(); ) {
			String addrs = null;
			String token = it.next();
			if (token != null && token.startsWith("$")) { // address variable
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
			
			Address[] iAddrs = null;
			if (StringUtils.isNotBlank(addrs)) {
				try {
					iAddrs = InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
				}
			}
			for (int i=0; iAddrs!=null && i<iAddrs.length; i++) {
				Address iAddr = iAddrs[i];
				String addr = iAddr.toString();
				if (isDebugEnabled) {
					logger.debug("Address to actiavte: " + addr);
				}
				EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(addr);
				if (!StatusIdCode.ACTIVE.equals(emailAddrVo.getStatusId())) {
					if (isDebugEnabled) {
						logger.debug("Activating EmailAddr: " + addr);
					}
					emailAddrVo.setStatusId(StatusIdCode.ACTIVE);
					emailAddrVo.setBounceCount(0); // reset bounce count
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
					emailAddrVo.setStatusChangeTime(updtTime);
					emailAddrDao.update(emailAddrVo);
				}
				else { // email address already active, reset bounce count
					emailAddrVo.setBounceCount(0); // reset bounce count
					emailAddrDao.update(emailAddrVo);
				}
				addrsActiveted++;
			}
		}
		return Long.valueOf(addrsActiveted);
	}

	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}

	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}
}
