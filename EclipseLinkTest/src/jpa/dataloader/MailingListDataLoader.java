package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.MailingListEnum;
import jpa.data.preload.SubscriberEnum;
import jpa.data.preload.SubscriberEnum.Subscriber;
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
		for (MailingListEnum mlist : MailingListEnum.values()) {
			if (mlist.isProd()) continue;
			MailingList in = new MailingList();
			in.setClientData(client);
			in.setListId(mlist.name());
			in.setDisplayName(mlist.getDisplayName());
			in.setAcctUserName(mlist.getAcctName());
			in.setDescription(mlist.getDescription());
			in.setStatusId(mlist.getStatusId().getValue());
			in.setBuiltin(mlist.isBuiltin());
			in.setCreateTime(createTime);
			in.setUpdtUserId(Constants.DEFAULT_USER_ID);
			in.setListMasterEmailAddr("sitemaster@"+domain);
			mlistService.insert(in);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	void loadProdMailingLists() throws SQLException {
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		String domain = client.getDomainName();

		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		
		for (MailingListEnum mlist : MailingListEnum.values()) {
			if (!mlist.isProd()) continue;
			MailingList in = new MailingList();
			in.setClientData(client);
			in.setListId(mlist.name());
			in.setDisplayName(mlist.getDisplayName());
			in.setAcctUserName(mlist.getAcctName());
			in.setDescription(mlist.getDescription());
			in.setStatusId(mlist.getStatusId().getValue());
			in.setBuiltin(mlist.isBuiltin());
			in.setCreateTime(createTime);
			in.setUpdtUserId(Constants.DEFAULT_USER_ID);
			// TODO get domain name from properties file
			in.setListMasterEmailAddr("sitemaster@"+domain);
			mlistService.insert(in);
		}

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadSubscribers() {
		java.sql.Timestamp createTime = new java.sql.Timestamp(System.currentTimeMillis());
		
		for (SubscriberEnum sublst : SubscriberEnum.values()) {
			MailingList mlist = mlistService.getByListId(sublst.getMailingList().name());
			for (Subscriber subscriber : sublst.getSubscribers()) {
				Subscription sub = new Subscription();
				sub.setMailingList(mlist);
				sub.setSubscribed(sublst.isSubscribed());
				sub.setStatusId(StatusId.ACTIVE.getValue());
				sub.setCreateTime(createTime);
				sub.setEmailAddr(emailService.findSertAddress(subscriber.getAddress()));
				if (Subscriber.Subscriber2.equals(subscriber)) {
					sub.setClickCount(1);
					sub.setOpenCount(2);
					sub.setSentCount(3);
				}
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				subService.insert(sub);
			}
		}
	}
}

