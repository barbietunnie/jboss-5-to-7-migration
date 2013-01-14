package com.legacytojava.message.bo.customer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.customer.CustSequenceDao;
import com.legacytojava.message.dao.customer.CustomerDao;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Service
@Component("customerBo")
public class CustomerBoImpl implements CustomerBo {
	static final Logger logger = Logger.getLogger(CustomerBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private CustomerDao customerDao;
	@Autowired
	private EmailAddrDao emailAddrDao;
	@Autowired
	private CustSequenceDao custSequenceDao;

	public synchronized int insert(CustomerVo vo) throws DataValidationException {
		validateCustomerVo(vo);
		int rowsInserted = 0;
		if (StringUtil.isEmpty(vo.getCustId())) {
			/* insert a new customer, will generate a custId using CustSequence */
			String custId = custSequenceDao.findNextValue() + "";
			if (custId.length() > CUSTOMER_ID_MAX_LEN) {
				throw new DataValidationException("Generated CustId exceeded maximum length: "
						+ custId);
			}
			// check the uniqueness of generated customer id
			CustomerVo custVo = customerDao.getByCustId(custId);
			if (custVo != null) {
				throw new DataValidationException("Generated CustId already exists: " + custId);
			}
			vo.setCustId(custId);
			rowsInserted = insertCustomer(vo);
		}
		else {
			// insert with a given custId
			CustomerVo custVo = customerDao.getByCustId(vo.getCustId());
			if (custVo == null) { // insert a customer
				rowsInserted = insertCustomer(vo);
			}
			else { // customer record exists
				throw new DataValidationException("Customer Id already exists: " + vo.getCustId());
			}
		}
		return rowsInserted;
	}

	public synchronized int update(CustomerVo vo) throws DataValidationException {
		validateCustomerVo(vo);
		if (StringUtil.isEmpty(vo.getCustId())) {
			throw new DataValidationException("Customer Id is not valued.");
		}
		int rowsUpdated = updateCustomer(vo);
		return rowsUpdated;
	}
	
	private void validateCustomerVo(CustomerVo vo) throws DataValidationException {
		if (vo == null) {
			throw new DataValidationException("Input CustomerVo is null.");
		}
		// validate customer email address
		if (StringUtil.isEmpty(vo.getEmailAddr())) {
			throw new DataValidationException("Email Address is not valued.");
		}
		if (!EmailAddrUtil.isRemoteEmailAddress(vo.getEmailAddr())) {
			throw new DataValidationException("Email Address is invalid: " + vo.getEmailAddr());
		}
		if (StringUtil.isEmpty(vo.getLastName())) {
			throw new DataValidationException("Last Name is not valued.");
		}
		// set default values
		if (StringUtil.isEmpty(vo.getClientId())) {
			vo.setClientId(Constants.DEFAULT_CLIENTID);
		}
		if (StringUtil.isEmpty(vo.getUpdtUserId())) {
			vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		}
		if (StringUtil.isEmpty(vo.getStatusId())) {
			vo.setStatusId(StatusIdCode.ACTIVE);
		}
		if (vo.getStartDate() == null) {
			vo.setStartDate(new java.util.Date());
		}
	}
	
	public synchronized int delete(String custId) throws DataValidationException {
		if (StringUtil.isEmpty(custId)) {
			throw new DataValidationException("Input Customer Id is not valued.");
		}
		int rowsDeleted = customerDao.delete(custId);
		return rowsDeleted;
	}
	
	public synchronized int deleteByEmailAddr(String emailAddr) throws DataValidationException {
		if (StringUtil.isEmpty(emailAddr)) {
			throw new DataValidationException("Input Email Address is not valued.");
		}
		int rowsDeleted = customerDao.deleteByEmailAddr(emailAddr);
		return rowsDeleted;
	}
	
	public CustomerVo getByCustId(String custId) throws DataValidationException {
		if (StringUtil.isEmpty(custId)) {
			throw new DataValidationException("Input Customer Id is not valued.");
		}
		CustomerVo vo = customerDao.getByCustId(custId);
		return vo;
	}
	
	public CustomerVo getByEmailAddr(String emailAddr) throws DataValidationException {
		if (StringUtil.isEmpty(emailAddr)) {
			throw new DataValidationException("Input Email Address is not valued.");
		}
		EmailAddrVo emailAddrVo = emailAddrDao.getByAddress(emailAddr);
		if (emailAddrVo == null) {
			logger.warn("getByEmailAddr() - email address not found: " + emailAddr);
			return null;
		}
		CustomerVo vo = customerDao.getByEmailAddrId(emailAddrVo.getEmailAddrId());
		return vo;
	}
	
	private int insertCustomer(CustomerVo vo) throws DataValidationException {
		// check if the email address is already used by someone else
		EmailAddrVo emailAddrVo = emailAddrDao.getByAddress(vo.getEmailAddr());
		if (emailAddrVo != null) { // email address found in EmailAddr table
			// is it used by any customers?
			CustomerVo cust_vo = customerDao.getByEmailAddrId(emailAddrVo.getEmailAddrId());
			if (cust_vo != null) { // yes, used by a customer
				throw new DataValidationException("Email Address: " + vo.getEmailAddr()
						+ " is used by another customer: " + cust_vo.getCustId());
			}
		}
		else { // email address not found from EmailAddr table
			// add the email address to EmailAddr table
			emailAddrVo = emailAddrDao.findByAddress(vo.getEmailAddr());
		}
		// ready for insert
		vo.setEmailAddrId(emailAddrVo.getEmailAddrId());
		vo.setStatusId(StatusIdCode.ACTIVE);
		if (vo.getStartDate() == null) {
			vo.setStartDate(new java.sql.Date(new java.util.Date().getTime()));
		}
		int rowsInserted = customerDao.insert(vo);
		return rowsInserted;
	}
	
	private int updateCustomer(CustomerVo vo) throws DataValidationException {
		// if it's a new email address, one will be inserted to EmailAddr table
		// if it's an existing address, one will be retrieved from EmailAddr table
		EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(vo.getEmailAddr());
		vo.setEmailAddrId(emailAddrVo.getEmailAddrId());
		// check the uniqueness of the new email address
		CustomerVo custVo = customerDao.getByEmailAddrId(emailAddrVo.getEmailAddrId());
		if (custVo != null && !(vo.getCustId().equals(custVo.getCustId()))) {
			throw new DataValidationException("Email Address: " + vo.getEmailAddr()
					+ " is used by another customer: " + custVo.getCustId());
		}
		if (vo.getStartDate() == null) { // just for safety
			vo.setStartDate(new java.sql.Date(new java.util.Date().getTime()));
		}
		int rowsUpdated = customerDao.update(vo);
		return rowsUpdated;
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

//	public void setCustomerDao(CustomerDao customerDao) {
//		this.customerDao = customerDao;
//	}

	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}

//	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
//		this.emailAddrDao = emailAddrDao;
//	}

	public CustSequenceDao getCustSequenceDao() {
		return custSequenceDao;
	}

//	public void setCustSequenceDao(CustSequenceDao custSequenceDao) {
//		this.custSequenceDao = custSequenceDao;
//	}

}
