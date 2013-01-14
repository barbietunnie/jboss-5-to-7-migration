package com.legacytojava.message.external;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.emailaddr.EmailVariableDao;
import com.legacytojava.message.exception.DataValidationException;

public class CustomerNameResolver implements VariableResolver {
	static final Logger logger = Logger.getLogger(CustomerNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public String process(long addrId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		String query = "SELECT CONCAT(c.firstName,' ',c.lastName) as ResultStr " +
				" FROM customers c, emailaddr e " +
				" where e.emailaddrId=c.emailAddrId and e.emailAddrId=?";
		
		EmailVariableDao dao = (EmailVariableDao) SpringUtil.getDaoAppContext().getBean(
				"emailVariableDao");
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
}
