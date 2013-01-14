package com.legacytojava.message.init;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.ejb.customer.CustomerLocal;
import com.legacytojava.message.ejb.customer.CustomerSignupLocal;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

public class CustomerSignUp {
	protected static final Logger logger = Logger.getLogger(CustomerSignUp.class);

	//@EJB(beanInterface=CustomerSignupRemote.class,mappedName="ejb/CustomerSignup")
	private CustomerSignupLocal customerSignup;
	//@EJB(beanInterface=CustomerLocal.class)
	private CustomerLocal customer;


	public CustomerSignUp() {
		customerSignup = (CustomerSignupLocal) LookupUtil.lookupLocalEjb("java:app/MsgEJBs/CustomerSignup!com.legacytojava.message.ejb.customer.CustomerSignupLocal");
		customer = (CustomerLocal) LookupUtil.lookupLocalEjb("java:app/MsgEJBs/Customer!com.legacytojava.message.ejb.customer.CustomerLocal");
	}

	public String signUpOnly(CustomerDto dto) throws DataValidationException {
		CustomerVo vo = new CustomerVo();
		try {
			BeanUtils.copyProperties(vo, dto);
			logger.info("signUpOnly() - CustomerVo\n" + vo);
		}
		catch (Exception e) {
			throw new DataValidationException("Exception caught - " + e.toString());
		}
		logger.info("signUpOnly() - about to add Customer with Email Address: " + vo.getEmailAddr());
		try {
			int rowsInserted = customerSignup.signupOnly(vo);
			if (rowsInserted > 0) {
				logger.info("signUpOnly() - Customer: " + vo.getEmailAddr()
						+ " has been added to customers table.");
			}
			else {
				logger.info("signUpOnly() - Customer: " + vo.getEmailAddr()
						+ " already exists, record updated.");
			}
			return vo.getCustId();
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public String signUpAndSubscribe(CustomerDto dto, String listId) throws DataValidationException {
		CustomerVo vo = new CustomerVo();
		try {
			BeanUtils.copyProperties(vo, dto);
			logger.info("signUpAndSubscribe() - CustomerVo\n" + vo);
		}
		catch (Exception e) {
			throw new DataValidationException("Exception caught - " + e.toString());
		}
		logger.info("signUpAndSubscribe() - about to add Customer with Email Address: "
				+ vo.getEmailAddr() + " to list: " + listId);
		try {
			customerSignup.signupAndSubscribe(vo, listId);
			logger.info("signUpAndSubscribe() - Customer: " + vo.getEmailAddr()
					+ " has been added to list: " + listId);
			return vo.getCustId();
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public CustomerDto getCustomer(String emailAddr) throws DataValidationException {
		logger.info("getCustomer() - about to retrieve Customer by Email Address: " + emailAddr
				+ " from the database.");
		try {
			CustomerDto dto = new CustomerDto();
			CustomerVo vo = customer.getCustomerByEmailAddress(emailAddr);
			if (vo != null) {
				logger.info("getCustomer() - Customer with Email Address: " + emailAddr
						+ " has been retrieved from the database.");
				try {
					BeanUtils.copyProperties(dto, vo);
					logger.info("getCustomer() - CustomerDto\n" + dto);
				}
				catch (Exception e) {
					throw new DataValidationException("Exception caught - " + e.toString());
				}
			}
			else {
				logger.info("getCustomer() - Customer not found by Email Address: " + emailAddr
						+ " from the database.");
			}
			return dto;
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public String updateCustomer(CustomerDto dto) throws DataValidationException {
		logger.info("updateCustomer() - calling signUpOnly() to perform update...");
		return signUpOnly(dto);
	}

	public int removeCustomer(String emailAddr) throws DataValidationException {
		logger.info("removeCustomer() - about to remove Customer with Email Address: " + emailAddr
				+ " from the database.");
		try {
			int rowsDeleted = customer.deleteByEmailAddr(emailAddr);
			logger.info("removeCustomer() - Customer with Email Address: " + emailAddr
					+ " has been removed from the database.");
			return rowsDeleted;
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public int addToList(String emailAddr, String listId) throws DataValidationException {
		logger.info("addToList() - about to add Customer: " + emailAddr + " to list: " + listId);
		try {
			int emailsSignedUp = customerSignup.addToList(emailAddr, listId);
			logger.info("addToList() - Customer: " + emailAddr + " has been added to list: "
					+ listId);
			return emailsSignedUp;
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public int removeFromList(String emailAddr, String listId) throws DataValidationException {
		logger.info("removeFromList() - about to remove Customer: " + emailAddr + " from list: "
				+ listId);
		try {
			int emailsSignedUp = customerSignup.removeFromList(emailAddr, listId);
			logger.info("removeFromList() - Customer: " + emailAddr
					+ " has been removed from list: " + listId);
			return emailsSignedUp;
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		CustomerSignUp signup = new CustomerSignUp();
		try {
			signup.addToList("jsmith@test.com", "SMPLLST1");
			signup.removeFromList("jsmith@test.com", "SMPLLST1");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
