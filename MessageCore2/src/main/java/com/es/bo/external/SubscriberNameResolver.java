package com.es.bo.external;

import org.apache.log4j.Logger;

import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailVariableDao;
import com.es.exception.DataValidationException;

public class SubscriberNameResolver implements VariableResolver {
	static final Logger logger = Logger.getLogger(SubscriberNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public String process(long addrId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		String query = "SELECT CONCAT(c.firstName,' ',c.lastName) as ResultStr " +
				" FROM subscriber c, email_address e " +
				" where e.emailaddrId=c.emailAddrId and e.emailAddrId=?";
		
		EmailVariableDao dao = SpringUtil.getAppContext().getBean(EmailVariableDao.class);
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
}
