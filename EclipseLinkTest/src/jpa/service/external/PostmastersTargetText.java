package jpa.service.external;

import java.util.List;

import jpa.exception.DataValidationException;
import jpa.model.EmailAddr;
import jpa.service.EmailAddrService;
import jpa.util.EmailAddrUtil;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("postmastersTargetText")
public class PostmastersTargetText implements RuleTargetProc {
	static final Logger logger = Logger.getLogger(PostmastersTargetText.class);
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
		EmailAddrService dao = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		List<EmailAddr> list = dao.getByAddressUser("(postmaster|mailmaster|mailadmin|administrator|mailer-(daemon|deamon)|smtp.gateway|majordomo)");
		for (int i = 0; i < list.size(); i++) {
			EmailAddr item = list.get(i);
			// no display name allowed for list address, just for safety
			String address = EmailAddrUtil.removeDisplayName(item.getAddress(), true);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(address);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		RuleTargetProc resolver = new PostmastersTargetText();
		try {
			String regex = resolver.process();
			System.err.println("Email regular expression: " + regex);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
