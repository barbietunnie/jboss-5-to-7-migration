package jpa.dataloader;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import jpa.constant.Constants;
import jpa.constant.MobileCarrierEnum;
import jpa.constant.StatusId;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.model.EmailAddress;
import jpa.service.SenderDataService;
import jpa.service.SubscriberDataService;
import jpa.service.EmailAddressService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class SubscriberDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(SubscriberDataLoader.class);
	private SubscriberDataService service;
	private EmailAddressService emailAddrService;
	private SenderDataService senderService;

	public static void main(String[] args) {
		SubscriberDataLoader loader = new SubscriberDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (SubscriberDataService) SpringUtil.getAppContext().getBean("subscriberDataService");
		emailAddrService = (EmailAddressService) SpringUtil.getAppContext().getBean("emailAddressService");
		senderService = (SenderDataService) SpringUtil.getAppContext().getBean("senderDataService");
		startTransaction();
		try {
			loadSubscriberData();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadSubscriberData() {
		String addr = getProperty("subscriber.email.1");
		EmailAddress emailaddr = emailAddrService.findSertAddress(addr);
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		SubscriberData data = new SubscriberData();
		data.setSenderData(cd);
		data.setEmailAddr(emailaddr);
		data.setSubscriberId(getProperty("subscriber.id.1"));
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
		data.setMobileCarrier(MobileCarrierEnum.TMobile.getValue());
		data.setBirthDate(new java.sql.Date(new GregorianCalendar(1980,01,01).getTimeInMillis()));
		data.setStartDate(new java.sql.Date(new GregorianCalendar(2004,05,10).getTimeInMillis()));
		data.setEndDate(new java.sql.Date(new GregorianCalendar(2016,05,10).getTimeInMillis()));
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data.setMsgHeader("Joe's Message Header");
		data.setMsgDetail("Dear Joe,");
		data.setMsgFooter("Have a nice day.");
		data.setTimeZone(TimeZone.getDefault().getID());
		data.setMemoText("E-Sphere Pilot subscriber");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setSecurityQuestion("What is your favorite movie?");
		data.setSecurityAnswer("Rambo");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
}

