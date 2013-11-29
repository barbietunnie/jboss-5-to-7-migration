package com.es.bo.external;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.EmailAddrUtil;
import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.vo.address.EmailAddressVo;

@Component("postmasterTargetText")
@Transactional(propagation=Propagation.REQUIRED)
public class PostmasterTargetText implements AbstractTargetProc {
	static final Logger logger = Logger.getLogger(PostmasterTargetText.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process() {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		StringBuffer sb = new StringBuffer();
		EmailAddressDao dao = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		List<EmailAddressVo> list = dao.getByAddressUser("(postmaster|mailmaster|mailadmin|administrator|mailer-(daemon|deamon)|smtp.gateway|majordomo)");
		for (int i = 0; i < list.size(); i++) {
			EmailAddressVo item = list.get(i);
			// no display name allowed for list address, just for safety
			String address = EmailAddrUtil.removeDisplayName(item.getEmailAddr(), true);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(address);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		AbstractTargetProc resolver = new PostmasterTargetText();
		try {
			String regex = resolver.process();
			System.err.println("Email regular expression: " + regex);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
