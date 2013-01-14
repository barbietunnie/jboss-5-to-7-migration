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
import com.legacytojava.message.bo.customer.CustomerBo;
import com.legacytojava.message.dao.customer.CustomerDao;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

/**
 * Session Bean implementation class Customer
 */
@Stateless(mappedName = "ejb/Customer")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(CustomerRemote.class)
@Local(CustomerLocal.class)
public class Customer implements CustomerRemote, CustomerLocal {
	protected static final Logger logger = Logger.getLogger(Customer.class);
	@Resource
	SessionContext context;
	private CustomerDao customerDao;
	private EmailAddrDao emailAddrDao;
	private CustomerBo customerBo;

	/**
     * Default constructor. 
     */
    public Customer() {
		customerDao = (CustomerDao)SpringUtil.getAppContext().getBean("customerDao");
		emailAddrDao = (EmailAddrDao)SpringUtil.getAppContext().getBean("emailAddrDao");
		customerBo = (CustomerBo)SpringUtil.getAppContext().getBean("customerBo");
    }

	public CustomerVo getCustomerByCustId(String custId) throws DataValidationException {
		CustomerVo vo = customerBo.getByCustId(custId);
		return vo;
	}

	public CustomerVo getCustomerByEmailAddress(String emailAddr) {
		EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(emailAddr);
		CustomerVo customerVo = customerDao.getByEmailAddrId(emailAddrVo.getEmailAddrId());
		return customerVo;
	}

	public int insertCustomer(CustomerVo vo) throws DataValidationException {
		int rowsInserted = customerBo.insert(vo);
		return rowsInserted;
	}

	public int updateCustomer(CustomerVo vo) throws DataValidationException {
		int rowsUpdated = customerBo.update(vo);
		return rowsUpdated;
	}

	public int deleteCustomer(String custId) throws DataValidationException {
		int rowsDeleted = customerBo.delete(custId);
		return rowsDeleted;
	}

	public int deleteByEmailAddr(String emailAddr) throws DataValidationException {
		int rowsDeleted = customerBo.deleteByEmailAddr(emailAddr);
		return rowsDeleted;
	}
}
