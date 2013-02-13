package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.SubscriberEnum.Subscriber;
import jpa.model.EmailAddr;
import jpa.service.EmailAddrService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class EmailAddrLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(EmailAddrLoader.class);
	private EmailAddrService service;

	public static void main(String[] args) {
		EmailAddrLoader loader = new EmailAddrLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		startTransaction();
		try {
			loadEmailAddrs();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadEmailAddrs() {
		int count = 0;
		for (Subscriber sub : Subscriber.values()) {
			EmailAddr data = new EmailAddr();
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

