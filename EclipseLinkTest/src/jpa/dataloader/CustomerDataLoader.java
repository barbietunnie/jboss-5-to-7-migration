package jpa.dataloader;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.ClientData;
import jpa.model.CustomerData;
import jpa.model.EmailAddr;
import jpa.service.ClientDataService;
import jpa.service.CustomerDataService;
import jpa.service.EmailAddrService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class CustomerDataLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(CustomerDataLoader.class);
	private CustomerDataService service;
	private EmailAddrService emailAddrService;
	private ClientDataService clientService;

	public static void main(String[] args) {
		CustomerDataLoader loader = new CustomerDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (CustomerDataService) SpringUtil.getAppContext().getBean("customerDataService");
		emailAddrService = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("idtokens_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadCustomerData();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	private void loadCustomerData() {
		String addr = "jsmith@test.com";
		EmailAddr emailaddr = emailAddrService.getByEmailAddr(addr);
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		CustomerData data = new CustomerData();
		data.setClientData(cd);
		if (data.getEmailAddrs()==null) {
			data.setEmailAddrs(new ArrayList<EmailAddr>());
		}
		data.getEmailAddrs().add(emailaddr);
		data.setCustomerId("test");
		data.setSsnNumber("123-45-6789");
		data.setTaxId(null);
		data.setProfession("Software Consultant");
		data.setFirstName("Joe");
		data.setLastName("Smith");
		data.setStreetAddress("123 Main St.");
		data.setCityName("Dublin");
		data.setStateCode("OH");
		data.setZipCode5("43071");
		data.setPostalCode("43071");
		data.setCountry("US");
		data.setDayPhone("614-234-5678");
		data.setEveningPhone("614-789-6543");
		data.setMobilePhone("614-JOE-CELL");
		data.setBirthDate(new java.sql.Date(new GregorianCalendar(1980,01,01).getTimeInMillis()));
		data.setStartDate(new java.sql.Date(new GregorianCalendar(2004,05,10).getTimeInMillis()));
		data.setEndDate(new java.sql.Date(new GregorianCalendar(2016,05,10).getTimeInMillis()));
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data.setMsgHeader("Joe's Message Header");
		data.setMsgDetail("Dear Joe,");
		data.setMsgFooter("Have a nice day.");
		data.setTimeZone(TimeZone.getDefault().getID());
		data.setMemoText("E-Sphere Pilot customer");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setSecurityQuestion("What is your favorite movie?");
		data.setSecurityAnswer("Rambo");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
}

