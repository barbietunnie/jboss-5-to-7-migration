package com.legacytojava.message.ejb.client;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.ejb.customer.CustomerRemote;
import com.legacytojava.message.ejb.customer.CustomerSignupRemote;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.LookupUtil;
import com.legacytojava.message.vo.CustomerVo;

/**
 * this class tests both CustomerSignup EJB's
 */
public class CustomerSignupClient {
	static final Logger logger = Logger.getLogger(CustomerSignupClient.class);
	public static void main(String[] args) {
		try {
			CustomerSignupClient signupClient = new CustomerSignupClient();
			CustomerVo vo = signupClient.getCustomerVo("test");
			try {
				signupClient.signUpAndSubscribe(vo, "SMPLLST1");
			}
			catch (DataValidationException e) {
				System.err.println("DataValidationException: " + e.getMessage());
			}
			signupClient.removeFromList("test", "SMPLLST1");
			signupClient.addToList("test", "SMPLLST1");
			
			//signupClient.testSignUp();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	void testSignUp() throws RemoteException, NamingException, CreateException,
			DataValidationException {
		CustomerRemote customer = getCustomerRemote();
		CustomerVo vo = getCustomerVo("test");
		vo.setCustId(null);
		for (int i = 0; i < 99; i++) {
			String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
			String emailAddr = "test" + suffix + "@localhost";
			vo.setEmailAddr(emailAddr);
			customer.deleteByEmailAddr(emailAddr); // delete the record first
			signUpAndSubscribe(vo, "SMPLLST1");
		}
	}
	
	private CustomerVo getCustomerVo(String custId) throws NamingException, RemoteException,
			CreateException, DataValidationException {
		CustomerRemote customer = getCustomerRemote();

		CustomerVo vo = customer.getCustomerByCustId(custId);
		return vo;
	}

	public void signUpAndSubscribe(CustomerVo vo, String listId) throws NamingException,
			RemoteException, CreateException, DataValidationException {
		CustomerSignupRemote customerSignup = getCustomerSignupRemote();

		int addrsSignedUp = 0;
		if (vo != null) {
			addrsSignedUp = customerSignup.signupAndSubscribe(vo, listId);
			logger.info("Number of customers signed up: " + addrsSignedUp);
		}
		else {
			logger.error("CustomerVo input is null.");
		}
	}

	public void addToList(String custId, String listId) throws NamingException, RemoteException,
			CreateException, DataValidationException {
		CustomerRemote customer = getCustomerRemote();
		CustomerSignupRemote customerSignup = getCustomerSignupRemote();

		CustomerVo vo = customer.getCustomerByCustId(custId);
		if (vo != null) {
			int custAdded = customerSignup.addToList(vo.getCustId(), listId);
			if (custAdded > 0) {
				logger.info("Customer " + custId + " has been added to list: " + listId);
			}
			else {
				logger.info("Customer " + custId + " was alreadey on list: " + listId);
			}
		}
		else {
			throw new DataValidationException("Failed to find customer record by " + custId);
		}
	}

	public void removeFromList(String custId, String listId) throws NamingException,
			RemoteException, CreateException, DataValidationException {
		CustomerRemote customer = getCustomerRemote();
		CustomerSignupRemote customerSignup = getCustomerSignupRemote();

		CustomerVo vo = customer.getCustomerByCustId(custId);
		if (vo != null) {
			int removed = customerSignup.removeFromList(vo.getCustId(), listId);
			if (removed > 0) {
				logger.info("Customer " + custId + " has been removed from list: " + listId);
			}
			else {
				logger.info("Customer " + custId + " was not on list: " + listId);
			}
		}
		else {
			throw new DataValidationException("Failed to find customer record by " + custId);
		}
	}
	
	private CustomerRemote getCustomerRemote() {
		return (CustomerRemote) LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/Customer!com.legacytojava.message.ejb.customer.CustomerRemote");
	}
	
	private CustomerSignupRemote getCustomerSignupRemote() {
		return (CustomerSignupRemote) LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/CustomerSignup!com.legacytojava.message.ejb.customer.CustomerSignupRemote");
	}

}
