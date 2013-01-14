package com.legacytojava.message.ejb.customer;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.logging.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bo.customer.CustomerSignupBo;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

/**
 * Session Bean implementation class CustomerSignup
 */
@Stateless(mappedName = "ejb/CustomerSignup")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(CustomerSignupRemote.class)
@Local(CustomerSignupLocal.class)
public class CustomerSignup implements CustomerSignupRemote, CustomerSignupLocal {
	protected static final Logger logger = Logger.getLogger(CustomerSignup.class);
	@Resource
	SessionContext context;
	private CustomerSignupBo customerSignupBo;
    /**
     * Default constructor. 
     */
    public CustomerSignup() {
    	customerSignupBo = (CustomerSignupBo)SpringUtil.getAppContext().getBean("customerSignupBo");
    }

	public int signupOnly(CustomerVo vo) throws DataValidationException {
		int emailsSignedUp = customerSignupBo.signUpOnly(vo);
		return emailsSignedUp;
	}

	public int signupAndSubscribe(CustomerVo vo, String listId) throws DataValidationException {
		int emailsSignedUp = customerSignupBo.signUpAndSubscribe(vo, listId);
		return emailsSignedUp;
	}

	public int addToList(String emailAddr, String listId) throws DataValidationException {
		int emailsSignedUp = customerSignupBo.addToList(emailAddr, listId);
		return emailsSignedUp;
	}

	public int removeFromList(String emailAddr, String listId) throws DataValidationException {
		int emailsSignedUp = customerSignupBo.removeFromList(emailAddr, listId);
		return emailsSignedUp;
	}
}
