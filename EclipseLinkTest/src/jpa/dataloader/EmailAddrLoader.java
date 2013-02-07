package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddr;
import jpa.service.EmailAddrService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class EmailAddrLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(EmailAddrLoader.class);
	private EmailAddrService service;

	public static void main(String[] args) {
		EmailAddrLoader loader = new EmailAddrLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadEmailAddrs();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	private void loadEmailAddrs() {
		EmailAddr data = new EmailAddr();
		data.setOrigAddress("jsmith@test.com");
		data.setAddress(data.getOrigAddress());
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusChangeUserId("testuser 1");
		data.setBounceCount(0);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailAddr();
		data.setOrigAddress("test@test.com");
		data.setAddress(data.getOrigAddress());
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusChangeUserId("testuser 2");
		data.setBounceCount(0);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailAddr();
		data.setOrigAddress("testuser@test.com");
		data.setAddress(data.getOrigAddress());
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusChangeUserId("testuser 3");
		data.setBounceCount(0);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		logger.info("EntityManager persisted the record.");
	}
	
}

