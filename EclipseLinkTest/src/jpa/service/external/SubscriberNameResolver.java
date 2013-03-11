package jpa.service.external;

import jpa.constant.Constants;
import jpa.exception.DataValidationException;
import jpa.service.EmailVariableService;
import jpa.util.JpaUtil;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("subscriberNameResolver")
public class SubscriberNameResolver implements VariableResolver {
	static final Logger logger = Logger.getLogger(SubscriberNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public String process(int addrId) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		String query = "SELECT CONCAT(c.firstName,' ',c.lastName) as ResultStr ";
		if (Constants.DB_PRODNAME_DERBY.equals(JpaUtil.getDBProductName())) {
			query = "SELECT (c.firstName || ' ' || c.lastName) as ResultStr ";
		}
		
		query += " FROM subscriber_data c, email_address e " +
				" where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1";
		
		EmailVariableService dao = (EmailVariableService) SpringUtil.getAppContext().getBean(
				"emailVariableService");
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
	
	public static void main(String[] args) {
		VariableResolver resolver = new SubscriberNameResolver();
		try {
			String name = resolver.process(1);
			System.err.println("Subscriber name: " + name);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
