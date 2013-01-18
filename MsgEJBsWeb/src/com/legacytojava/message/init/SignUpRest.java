package com.legacytojava.message.init;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.ejb.customer.CustomerLocal;
import com.legacytojava.message.ejb.customer.CustomerSignupLocal;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.CustomerVo;

@Path("/signup")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SignUpRest {
	protected static final Logger logger = Logger.getLogger(SignUpRest.class);

	private CustomerSignupLocal customerSignup;
	private CustomerLocal customer;

	public SignUpRest() {
		// LookupUtil.lookupLocalEjb(...) caused Exception been thrown from JBoss CXF module.
	}
	
	private CustomerSignupLocal getCustomerSignup() {
		if (customerSignup == null) {
			customerSignup = (CustomerSignupLocal) LookupUtil.lookupLocalEjb("java:app/MsgEJBs/CustomerSignup!com.legacytojava.message.ejb.customer.CustomerSignupLocal");
		}
		return customerSignup;
	}
	
	private CustomerLocal getCustomer() {
		if (customer == null) {
			customer = (CustomerLocal) LookupUtil.lookupLocalEjb("java:app/MsgEJBs/Customer!com.legacytojava.message.ejb.customer.CustomerLocal");
		}
		return customer;
	}

	public String signUpOnly(CustomerDto dto)  {
		CustomerVo vo = new CustomerVo();
		try {
			BeanUtils.copyProperties(vo, dto);
			logger.info("signUpOnly() - CustomerVo\n" + StringUtil.prettyPrint(vo));
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Exception caught - " + e.toString());
		}
		logger.info("signUpOnly() - about to add Customer with Email Address: " + vo.getEmailAddr());
		try {
			int rowsInserted = getCustomerSignup().signupOnly(vo);
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

	public String signUpAndSubscribe(CustomerDto dto, String listId) {
		CustomerVo vo = new CustomerVo();
		try {
			BeanUtils.copyProperties(vo, dto);
			logger.info("signUpAndSubscribe() - CustomerVo\n" + StringUtil.prettyPrint(vo));
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Exception caught - " + e.toString());
		}
		logger.info("signUpAndSubscribe() - about to add Customer with Email Address: "
				+ vo.getEmailAddr() + " to list: " + listId);
		try {
			getCustomerSignup().signupAndSubscribe(vo, listId);
			logger.info("signUpAndSubscribe() - Customer: " + vo.getEmailAddr()
					+ " has been added to list: " + listId);
			return vo.getCustId();
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@GET
	@Path("/customer/{email}")
	public CustomerDto getCustomer(@PathParam("email") String emailAddr) {
		logger.info("getCustomer() - about to retrieve Customer by Email Address: " + emailAddr
				+ " from the database.");
		try {
			CustomerDto dto = new CustomerDto();
			CustomerVo vo = getCustomer().getCustomerByEmailAddress(emailAddr);
			if (vo != null) {
				logger.info("getCustomer() - Customer with Email Address: " + emailAddr
						+ " has been retrieved from the database.");
				try {
					BeanUtils.copyProperties(dto, vo);
					logger.info("getCustomer() - CustomerDto\n" + StringUtil.prettyPrint(dto));
				}
				catch (Exception e) {
					throw new IllegalArgumentException("Exception caught - " + e.toString());
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

	public String updateCustomer(CustomerDto dto)  {
		logger.info("updateCustomer() - calling signUpOnly() to perform update...");
		try {
			return signUpOnly(dto);
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public int removeCustomer(String emailAddr) {
		logger.info("removeCustomer() - about to remove Customer with Email Address: " + emailAddr
				+ " from the database.");
		try {
			int rowsDeleted = getCustomer().deleteByEmailAddr(emailAddr);
			logger.info("removeCustomer() - Customer with Email Address: " + emailAddr
					+ " has been removed from the database.");
			return rowsDeleted;
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@GET
	@Path("/addtolist")
	public int addToList(@QueryParam("email") String emailAddr, @QueryParam("list")String listId) {
		logger.info("addToList() - about to add Customer: " + emailAddr + " to list: " + listId);
		try {
			int emailsSignedUp = getCustomerSignup().addToList(emailAddr, listId);
			logger.info("addToList() - Customer: " + emailAddr + " has been added to list: "
					+ listId);
			return emailsSignedUp;
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	@GET
	@Path("/removefromlist")
	public int removeFromList(@QueryParam("email") String emailAddr, @QueryParam("list")String listId) {
		logger.info("removeFromList() - about to remove Customer: " + emailAddr + " from list: "
				+ listId);
		try {
			int emailsSignedUp = getCustomerSignup().removeFromList(emailAddr, listId);
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
		SignUpRest signup = new SignUpRest();
		try {
			signup.addToList("jsmith@test.com", "SMPLLST1");
			signup.removeFromList("jsmith@test.com", "SMPLLST1");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
