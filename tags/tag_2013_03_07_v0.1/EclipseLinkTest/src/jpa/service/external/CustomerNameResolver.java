package jpa.service.external;

import jpa.exception.DataValidationException;
import jpa.service.EmailVariableService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("customerNameResolver")
public class CustomerNameResolver implements VariableResolver {
	static final Logger logger = Logger.getLogger(CustomerNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public String process(int addrId) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		String query = "SELECT CONCAT(c.firstName,' ',c.lastName) as ResultStr " +
				" FROM customer_data c, email_addr e " +
				" where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1";
		
		EmailVariableService dao = (EmailVariableService) SpringUtil.getAppContext().getBean(
				"emailVariableService");
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
	
	public static void main(String[] args) {
		VariableResolver resolver = new CustomerNameResolver();
		try {
			String name = resolver.process(1);
			System.err.println("Customer name: " + name);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
