package jpa.service.external;

import java.util.List;

import jpa.model.MailingList;
import jpa.service.maillist.MailingListService;
import jpa.util.EmailAddrUtil;
import jpa.util.SpringUtil;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("mailingListTargetText")
@Transactional(propagation=Propagation.REQUIRED)
public class MailingListTargetText implements TargetTextProc {
	static final Logger logger = Logger.getLogger(MailingListTargetText.class);
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
		MailingListService dao = SpringUtil.getAppContext().getBean(MailingListService.class);
		List<MailingList> list = dao.getAll(false);
		for (int i = 0; i < list.size(); i++) {
			MailingList item = list.get(i);
			// no display name allowed for list address, just for safety
			String emailAddr = EmailAddrUtil.removeDisplayName(item.getListEmailAddr(), true);
			// make it a regular expression
			emailAddr = StringUtil.replaceAll(emailAddr, ".", "\\.");
			if (i > 0) {
				sb.append("|");
			}
			sb.append("^" + emailAddr + "$");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		TargetTextProc resolver = new MailingListTargetText();
		try {
			String regex = resolver.process();
			System.err.println("Email regular expression: " + regex);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
