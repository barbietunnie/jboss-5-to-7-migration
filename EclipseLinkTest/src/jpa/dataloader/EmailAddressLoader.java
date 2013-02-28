package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.SubscriberEnum.Subscriber;
import jpa.model.EmailAddress;
import jpa.service.EmailAddressService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class EmailAddressLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(EmailAddressLoader.class);
	private EmailAddressService service;

	public static void main(String[] args) {
		EmailAddressLoader loader = new EmailAddressLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (EmailAddressService) SpringUtil.getAppContext().getBean("emailAddressService");
		startTransaction();
		try {
			loadEmailAddress();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadEmailAddress() {
		int count = 0;
		for (Subscriber sub : Subscriber.values()) {
			EmailAddress data = new EmailAddress();
			data.setOrigAddress(sub.getAddress());
			data.setAddress(data.getOrigAddress());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
			data.setStatusChangeUserId("testuser" + (++count));
			data.setBounceCount(0);
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
}

