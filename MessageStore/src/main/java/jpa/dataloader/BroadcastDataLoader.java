package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EmailTemplateService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;
import jpa.service.maillist.MailingListService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class BroadcastDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(BroadcastDataLoader.class);
	private MailingListService mlistService;
	private EmailTemplateService etmpltService;
	private EmailAddressService emailService;
	private BroadcastMessageService bcastService;
	private BroadcastTrackingService trkngService;;

	public static void main(String[] args) {
		BroadcastDataLoader loader = new BroadcastDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		mlistService = SpringUtil.getAppContext().getBean(MailingListService.class);
		etmpltService = SpringUtil.getAppContext().getBean(EmailTemplateService.class);
		emailService = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		bcastService = SpringUtil.getAppContext().getBean(BroadcastMessageService.class);
		trkngService = SpringUtil.getAppContext().getBean(BroadcastTrackingService.class);
		startTransaction();
		try {
			loadBroadcastData();
			loadEmailBroadcasts();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadBroadcastData() throws SQLException {
		List<MailingList> mllst = mlistService.getAll(true);
		List<EmailTemplate> etlst = etmpltService.getAll();
		if (etlst.isEmpty()) {
			return;
		}
		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		for (MailingList ml : mllst) {
			BroadcastMessage vo = new BroadcastMessage();
			vo.setMailingList(ml);
			vo.setEmailTemplate(etlst.get(0));
			vo.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
			vo.setStatusId(StatusId.ACTIVE.getValue());
			vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			vo.setStartTime(createTime);
			vo.setUpdtTime(createTime);
			bcastService.insert(vo);
			break;
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadEmailBroadcasts() {
		java.sql.Timestamp createTime = new java.sql.Timestamp(System.currentTimeMillis());
		List<BroadcastMessage> bdlist = bcastService.getAll();
		
		for (BroadcastMessage bd : bdlist) {
			BroadcastTracking eb = new BroadcastTracking();
			eb.setBroadcastMessage(bd);
			eb.setStatusId(StatusId.ACTIVE.getValue());
			eb.setUpdtUserId(Constants.DEFAULT_USER_ID);
			eb.setUpdtTime(createTime);
			eb.setEmailAddress(emailService.findSertAddress("test@test.com"));
			trkngService.insert(eb);
			
			eb = new BroadcastTracking();
			eb.setBroadcastMessage(bd);
			eb.setStatusId(StatusId.ACTIVE.getValue());
			eb.setUpdtUserId(Constants.DEFAULT_USER_ID);
			eb.setUpdtTime(createTime);
			eb.setEmailAddress(emailService.findSertAddress("testto@test.com"));
			trkngService.insert(eb);
			break;
		}
		
		logger.info("EntityManager persisted the record.");
	}
}
