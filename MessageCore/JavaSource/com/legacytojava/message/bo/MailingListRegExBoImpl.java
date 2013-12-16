package com.legacytojava.message.bo;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.MailingListVo;

@Component("mailingListRegExBo")
@Scope(value="prototype")
@Lazy(true)
public class MailingListRegExBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(MailingListRegExBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListDao mailingListDao;

	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		
		StringBuffer sb = new StringBuffer();
		List<MailingListVo> list = mailingListDao.getAll(false);
		for (int i = 0; i < list.size(); i++) {
			MailingListVo item = list.get(i);
			// no display name allowed for list address, just for safety
			String emailAddr = EmailAddrUtil.removeDisplayName(item.getEmailAddr(), true);
			emailAddr = StringUtil.replaceAll(emailAddr, ".", "\\.");
			if (i > 0) {
				sb.append("|");
			}
			sb.append("^" + emailAddr + "$");
		}
		return sb.toString();
	}
}
