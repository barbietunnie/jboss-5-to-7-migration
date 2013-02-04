package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.ClientData;
import jpa.model.UserData;
import jpa.service.ClientDataService;
import jpa.service.UserDataService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserDataLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(UserDataLoader.class);
	private UserDataService service;
	private ClientDataService clientService;

	public static void main(String[] args) {
		UserDataLoader loader = new UserDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (UserDataService) SpringUtil.getAppContext().getBean("userDataService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadUserData();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	private void loadUserData() {
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		UserData data = new UserData();
		data.setClientData(cd);
		data.setUserId("admin");
		data.setPassword("admin");
		data.setFirstName("default");
		data.setLastName("admin");
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setRole(Constants.ADMIN_ROLE);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new UserData();
		data.setClientData(cd);
		data.setUserId("user");
		data.setPassword("user");
		data.setFirstName("default");
		data.setLastName("user");
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setRole(Constants.USER_ROLE);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		logger.info("EntityManager persisted the record.");
	}
	
}

