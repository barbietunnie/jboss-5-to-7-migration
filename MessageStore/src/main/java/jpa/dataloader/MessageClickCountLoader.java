package jpa.dataloader;

import java.sql.Timestamp;
import java.util.List;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.model.message.MessageClickCount;
import jpa.model.message.MessageHeader;
import jpa.model.message.MessageHeaderPK;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.EmailAddressService;
import jpa.service.MailingListService;
import jpa.service.SenderDataService;
import jpa.service.message.MessageClickCountService;
import jpa.service.message.MessageHeaderService;
import jpa.service.message.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class MessageClickCountLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MessageClickCountLoader.class);

	private MessageClickCountService service;

	public static void main(String[] args) {
		MessageClickCountLoader loader = new MessageClickCountLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (MessageClickCountService) SpringUtil.getAppContext().getBean("messageClickCountService");
		startTransaction();
		try {
			loadMessageClickCount();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMessageClickCount() {
		MessageInboxService inboxService = (MessageInboxService) SpringUtil.getAppContext().getBean("messageInboxService");
		EmailAddressService addrService = (EmailAddressService) SpringUtil.getAppContext().getBean("emailAddressService");
		SenderDataService senderService = (SenderDataService) SpringUtil.getAppContext().getBean("senderDataService");
		RuleLogicService logicService = (RuleLogicService) SpringUtil.getAppContext().getBean("ruleLogicService");
		MailingListService listService = (MailingListService) SpringUtil.getAppContext().getBean("mailingListService");
		MessageHeaderService headerService = (MessageHeaderService) SpringUtil.getAppContext().getBean("messageHeaderService");

		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		MessageInbox inbox1 = new MessageInbox();
		
		inbox1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		inbox1.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		inbox1.setMsgSubject("Test Subject");
		inbox1.setMsgPriority("2 (Normal)");
		inbox1.setReceivedTime(updtTime);
		
		EmailAddress from = addrService.findSertAddress("test@test.com");
		inbox1.setFromAddrRowId(from.getRowId());
		inbox1.setReplytoAddrRowId(null);

		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		String to_addr = sender.getReturnPathLeft() + "@" + sender.getDomainName();
		EmailAddress to = addrService.findSertAddress(to_addr);
		inbox1.setToAddrRowId(to.getRowId());
		inbox1.setSenderDataRowId(sender.getRowId());
		inbox1.setSubscriberDataRowId(null);
		inbox1.setPurgeDate(null);
		inbox1.setUpdtTime(updtTime);
		inbox1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		inbox1.setLockTime(null);
		inbox1.setLockId(null);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		inbox1.setRuleLogicRowId(logic.getRowId());
		inbox1.setMsgContentType("multipart/mixed");
		inbox1.setBodyContentType("text/plain");
		inbox1.setMsgBody("Test Message Body");
		inboxService.insert(inbox1);
		
		List<MailingList> mlists=listService.getAll(true);
		MailingList mlist=mlists.get(0);
		
		List<MessageHeader> headerList = inbox1.getMessageHeaderList();
		int seq = headerList.size();
		MessageHeader header1 = new MessageHeader();
		MessageHeaderPK pk1 = new MessageHeaderPK(inbox1, ++seq);
		header1.setMessageHeaderPK(pk1);
		header1.setHeaderName("Test-Header");
		header1.setHeaderValue("Test Header Value");
		headerService.insert(header1);
		MessageHeader header2 = new MessageHeader();
		MessageHeaderPK pk2 = new MessageHeaderPK(inbox1, ++seq);
		header2.setMessageHeaderPK(pk2);
		header2.setHeaderName("Test-Header-2");
		header2.setHeaderValue("Test Header Value 2");
		headerService.insert(header2);
		
		// insert
		Timestamp clickTime = new Timestamp(System.currentTimeMillis());
		MessageClickCount mcc1 = new MessageClickCount();
		mcc1.setMessageInbox(inbox1);
		mcc1.setClickCount(3);
		mcc1.setOpenCount(2);
		mcc1.setComplaintCount(0);
		mcc1.setSentCount(1);
		mcc1.setStartTime(clickTime);
		mcc1.setLastClickTime(clickTime);
		mcc1.setLastOpenTime(clickTime);
		mcc1.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		mcc1.setMailingListRowId(mlist.getRowId());
		service.insert(mcc1);

		logger.info("EntityManager persisted the record.");
	}
	
}

