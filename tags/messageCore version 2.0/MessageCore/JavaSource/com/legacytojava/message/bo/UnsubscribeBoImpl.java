package com.legacytojava.message.bo;

import java.util.List;

import javax.mail.Address;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.dao.emailaddr.SubscriptionDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;

public class UnsubscribeBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(UnsubscribeBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private EmailAddrDao emailAddrDao;
	private MailingListDao mailingListDao;
	private SubscriptionDao subscriptionDao;

	/**
	 * Remove the FROM address from the mailing list (TO).
	 * 
	 * @return a Long value representing number of addresses that have been
	 *         updated.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		long addrsUpdated = 0;
		Address[] toAddrs = messageBean.getTo();
		if (toAddrs == null || toAddrs.length == 0) { // just for safety
			logger.error("unsubscription address (TO) is null.");
			throw new DataValidationException("TO address is null");
		}
		for (int k = 0; k < toAddrs.length; k++) {
			Address toAddr = toAddrs[k];
			if (toAddr == null || StringUtil.isEmpty(toAddr.toString())) {
				continue; // just for safety
			}
			List<MailingListVo> mlist = mailingListDao.getByAddress(toAddr.toString());
			if (mlist.size() == 0) {
				logger.warn("Mailing List not found for address: " + toAddr);
				continue;
			}
			Address[] addrs = messageBean.getFrom();
			for (int i = 0; addrs != null && i < addrs.length; i++) {
				Address addr = addrs[i];
				if (addr == null || StringUtil.isEmpty(addr.toString())) {
					continue; // just for safety
				}
				EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(addr.toString());
				int rowsAffected = 0;
				for (MailingListVo item : mlist) {
					messageBean.setMailingListId(item.getListId());
					rowsAffected += subscriptionDao.unsubscribe(emailAddrVo.getEmailAddrId(), item
							.getListId());
					logger.info(addr + " unsubscribed from: " + item.getListId());
				}
				if (rowsAffected > 0) {
					addrsUpdated++;
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
	
	public MailingListDao getMailingListDao() {
		return mailingListDao;
	}

	public void setMailingListDao(MailingListDao mailingListDao) {
		this.mailingListDao = mailingListDao;
	}

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}
}
