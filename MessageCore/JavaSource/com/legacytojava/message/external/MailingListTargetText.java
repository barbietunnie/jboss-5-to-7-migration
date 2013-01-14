package com.legacytojava.message.external;

import java.util.List;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.MailingListVo;

public class MailingListTargetText implements RuleTargetProc {
	static final Logger logger = Logger.getLogger(MailingListTargetText.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process() throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		
		StringBuffer sb = new StringBuffer();
		MailingListDao dao = (MailingListDao) SpringUtil.getDaoAppContext().getBean("mailingListDao");
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
