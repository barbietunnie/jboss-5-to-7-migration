package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.ClientData;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MailingListService;
import jpa.service.SubscriptionService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class MailingListDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MailingListDataLoader.class);
	private MailingListService mlistService;
	private ClientDataService clientService;
	private EmailAddrService emailService;
	private SubscriptionService subService;

	public static void main(String[] args) {
		MailingListDataLoader loader = new MailingListDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		mlistService = (MailingListService) SpringUtil.getAppContext().getBean("mailingListService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		emailService = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		subService = (SubscriptionService) SpringUtil.getAppContext().getBean("subscriptionService");
		startTransaction();
		try {
			loadMailingLists();
			loadSubscribers();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMailingLists() throws SQLException {
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		String domain = client.getDomainName();

		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		
		MailingList in = new MailingList();
		in.setClientData(client);
		in.setListId("SMPLLST1");
		in.setDisplayName("Sample List 1");
		in.setAcctUserName("demolist1");
		in.setDescription("Sample mailing list 1");
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setBuiltin(false);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setListMasterEmailAddr("sitemaster@"+domain);
		mlistService.insert(in);

		in = new MailingList();
		in.setClientData(client);
		in.setListId("SMPLLST2");
		in.setDisplayName("Sample List 2");
		in.setAcctUserName("demolist2");
		in.setDescription("Sample mailing list 2");
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setBuiltin(false);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setListMasterEmailAddr("sitemaster@"+domain);
		mlistService.insert(in);

		in = new MailingList();
		in.setClientData(client);
		in.setListId("SYSLIST1");
		in.setDisplayName("NOREPLY Empty List");
		in.setAcctUserName("noreply");
		in.setDescription("Auto-Responder, used by Subscription and confirmation Templates");
		in.setStatusId(StatusId.INACTIVE.getValue());
		in.setBuiltin(true);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setListMasterEmailAddr("sitemaster@" + domain);
		mlistService.insert(in);
		logger.info("EntityManager persisted the record.");
	}
	
	void loadProdMailingLists() throws SQLException {
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		String domain = client.getDomainName();

		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		
		MailingList in = new MailingList();
		in.setClientData(client);
		in.setListId("ORDERLST");
		in.setDisplayName("Sales ORDER List");
		in.setAcctUserName("support");
		in.setDescription("Auto-Responder, used by order processing");
		in.setStatusId(StatusId.INACTIVE.getValue());
		in.setBuiltin(true);
		in.setCreateTime(createTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		// TODO get domain name from properties file
		in.setListMasterEmailAddr("sitemaster@" + domain);
		mlistService.insert(in);

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadSubscribers() {
		MailingList mlist1 = mlistService.getByListId("SMPLLST1");
		MailingList mlist2 = mlistService.getByListId("SMPLLST2");
		java.sql.Timestamp createTime = new java.sql.Timestamp(System.currentTimeMillis());
		
		Subscription sub = new Subscription();
		sub.setMailingList(mlist1);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(emailService.findSertAddress("jsmith@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
		
		sub = new Subscription();
		sub.setMailingList(mlist1);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(emailService.findSertAddress("test@test.com"));
		sub.setClickCount(1);
		sub.setOpenCount(2);
		sub.setSentCount(3);
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);

		sub = new Subscription();
		sub.setMailingList(mlist1);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(emailService.findSertAddress("testuser@test.com"));
		sub.setClickCount(2);
		sub.setOpenCount(3);
		sub.setSentCount(4);
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
	
		emailService.findSertAddress("jsmith@test.com");
		sub = new Subscription();
		sub.setMailingList(mlist2);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(emailService.findSertAddress("jsmith@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
		
		sub = new Subscription();
		sub.setMailingList(mlist2);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(emailService.findSertAddress("test@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);

		sub = new Subscription();
		sub.setMailingList(mlist2);
		sub.setSubscribed(true);
		sub.setStatusId(StatusId.ACTIVE.getValue());
		sub.setCreateTime(createTime);
		sub.setEmailAddr(emailService.findSertAddress("testuser@test.com"));
		sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
		subService.insert(sub);
	}
}

