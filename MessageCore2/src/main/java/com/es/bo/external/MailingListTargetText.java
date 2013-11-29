package com.es.bo.external;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.EmailAddrUtil;
import com.es.core.util.SpringUtil;
import com.es.core.util.StringUtil;
import com.es.dao.address.MailingListDao;
import com.es.exception.DataValidationException;
import com.es.vo.address.MailingListVo;

@Component("mailingListTargetText")
@Transactional(propagation=Propagation.REQUIRED)
public class MailingListTargetText implements AbstractTargetProc {
	static final Logger logger = Logger.getLogger(MailingListTargetText.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process() throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		StringBuffer sb = new StringBuffer();
		MailingListDao dao = SpringUtil.getAppContext().getBean(MailingListDao.class);
		List<MailingListVo> list = dao.getAll(false);
		for (int i = 0; i < list.size(); i++) {
			MailingListVo item = list.get(i);
			// no display name allowed for list address, just for safety
			String emailAddr = EmailAddrUtil.removeDisplayName(item.getEmailAddr(), true);
			// make it a regular expression
			emailAddr = StringUtil.replaceAll(emailAddr, ".", "\\.");
			if (i > 0) {
				sb.append("|");
			}
			sb.append("^" + emailAddr + "$");
		}
		return sb.toString();
	}
}
