package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.ClientData;
import jpa.model.EmailAddr;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MailingListService;
import jpa.service.SubscriptionService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class MailingListDataLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MailingListDataLoader.class);
	private MailingListService mlService;
	private ClientDataService clientService;
	private EmailAddrService eaService;
	private SubscriptionService subService;

	public static void main(String[] args) {
		MailingListDataLoader loader = new MailingListDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		mlService = (MailingListService) SpringUtil.getAppContext().getBean("mailingListService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		eaService = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		subService = (SubscriptionService) SpringUtil.getAppContext().getBean("subscriptionService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadMailingLists();
			loadSubscribers();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	
	private void loadMailingLists() throws SQLException {
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);

		Timestamp createTime = new Timestamp(new java.util.Date().getTime());

		EmailAddr ea1 = eaService.findSertEmailAddr("demolist1@localhost");
		
		MailingList in = new MailingList();
		in.setClientData(cd);
		in.setListId("SMPLLST1");
		in.setDisplayName("Sample List 1");
		in.setAcctUserName("demolist1");
		in.setDescription("Sample mailing list 1");
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setBuiltIn(false);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setListMasterEmailAddr(ea1);
		mlService.insert(in);

		EmailAddr ea2 = eaService.findSertEmailAddr("demolist2@localhost");

		in = new MailingList();
		in.setClientData(cd);
		in.setListId("SMPLLST2");
		in.setDisplayName("Sample List 2");
		in.setAcctUserName("demolist2");
		in.setDescription("Sample mailing list 2");
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setBuiltIn(false);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setListMasterEmailAddr(ea2);
		mlService.insert(in);

		EmailAddr ea3 = eaService.findSertEmailAddr("noreply@localhost");

		in = new MailingList();
		in.setClientData(cd);
		in.setListId("SYSLIST1");
		in.setDisplayName("NOREPLY Empty List");
		in.setAcctUserName("noreply");
		in.setDescription("Auto-Responder, used by Subscription and confirmation Templates");
		in.setStatusId(StatusId.INACTIVE.getValue());
		in.setBuiltIn(true);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setListMasterEmailAddr(ea3);
		mlService.insert(in);
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadSubscribers() {
		MailingList mlist1 = mlService.getByListId("SMPLLST1");
		MailingList mlist2 = mlService.getByListId("SMPLLST2");
		java.sql.Timestamp createTime = new java.sql.Timestamp(System.currentTimeMillis());
		
		eaService.findSertEmailAddr("jsmith@test.com");
		Subscription sub = new Subscription();
		sub.setMailingList(mlist1);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(eaService.findSertEmailAddr("jsmith@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
		
		sub = new Subscription();
		sub.setMailingList(mlist1);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(eaService.findSertEmailAddr("test@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);

		sub = new Subscription();
		sub.setMailingList(mlist1);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(eaService.findSertEmailAddr("testuser@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
	
		eaService.findSertEmailAddr("jsmith@test.com");
		sub = new Subscription();
		sub.setMailingList(mlist2);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(eaService.findSertEmailAddr("jsmith@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
		
		sub = new Subscription();
		sub.setMailingList(mlist2);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(eaService.findSertEmailAddr("test@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);

		sub = new Subscription();
		sub.setMailingList(mlist2);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(eaService.findSertEmailAddr("testuser@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
	}
}

