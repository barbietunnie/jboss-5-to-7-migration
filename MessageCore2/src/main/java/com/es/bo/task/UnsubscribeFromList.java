package com.es.bo.task;

import javax.mail.Address;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.EmailAddressDao;
import com.es.dao.address.MailingListDao;
import com.es.dao.address.SubscriptionDao;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.address.MailingListVo;

@Component("unsubscribeFromList")
@Transactional(propagation=Propagation.REQUIRED)
public class UnsubscribeFromList extends TaskBaseAdaptor {
	private static final long serialVersionUID = -1655630086836497324L;
	static final Logger logger = Logger.getLogger(UnsubscribeFromList.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddrDao;
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private SubscriptionDao subService;

	/**
	 * Remove the FROM address from the mailing list (TO).
	 * 
	 * @return a Long value representing number of addresses that have been
	 *         updated.
	 */
	public Long process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		int addrsUpdated = 0;
		Address[] toAddrs = messageBean.getTo();
		if (toAddrs == null || toAddrs.length == 0) { // just for safety
			logger.error("unsubscription address (TO) is null.");
			throw new DataValidationException("TO address is null");
		}
		MailingListVo mlist = null;
		for (int k = 0; k < toAddrs.length; k++) {
			Address toAddr = toAddrs[k];
			if (toAddr == null || StringUtils.isBlank(toAddr.toString())) {
				continue; // just for safety
			}
			mlist = mailingListDao.getByListAddress(toAddr.toString());
			if (mlist == null) {
				logger.warn("Mailing List not found for address: " + toAddr);
				continue;
			}
			Address[] addrs = messageBean.getFrom();
			for (int i = 0; addrs != null && i < addrs.length; i++) {
				Address addr = addrs[i];
				if (addr == null || StringUtils.isBlank(addr.toString())) {
					continue; // just for safety
				}
				EmailAddressVo emailAddrVo = emailAddrDao.findSertAddress(addr.toString());
				messageBean.setMailingListId(mlist.getListId());
				int rows = subService.unsubscribe(emailAddrVo.getEmailAddr(), mlist.getListId());
				if (rows > 0) {
					ctx.getRowIds().add(emailAddrVo.getEmailAddrId());
				}
				logger.info(addr + " unsubscribed from: " + mlist.getListId());
				addrsUpdated++;
			}
		}
		return Long.valueOf(addrsUpdated);
	}

}
