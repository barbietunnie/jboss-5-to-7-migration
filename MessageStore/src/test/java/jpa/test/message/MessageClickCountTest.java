package jpa.test.message;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.message.MessageClickCount;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.msgui.vo.PagingVo;
import jpa.service.EntityManagerService;
import jpa.service.SenderDataService;
import jpa.service.EmailAddressService;
import jpa.service.MailingListService;
import jpa.service.message.MessageClickCountService;
import jpa.service.message.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.util.StringUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageClickCountTest {

	@BeforeClass
	public static void MessageClickCountPrepare() {
	}

	@Autowired
	MessageClickCountService service;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	RuleLogicService logicService;
	@Autowired
	MailingListService listService;
	@Autowired
	EntityManagerService emService;

	private MessageInbox inbox1;
	private EmailAddress from;
	private EmailAddress to;
	private MailingList mlist;

	@Before
	public void prepare() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		inbox1 = new MessageInbox();
		
		inbox1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		inbox1.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		inbox1.setMsgSubject("Test Subject");
		inbox1.setMsgPriority("2 (Normal)");
		inbox1.setReceivedTime(updtTime);
		
		from = addrService.findSertAddress("test@test.com");
		inbox1.setFromAddrRowId(from.getRowId());
		inbox1.setReplytoAddrRowId(null);

		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		String to_addr = sender.getReturnPathLeft() + "@" + sender.getDomainName();
		to = addrService.findSertAddress(to_addr);
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
		mlist=mlists.get(0);
	}
	
	private MessageClickCount mcc1;
	private MessageClickCount mcc2;

	@Test
	public void messageClickCountService() throws IOException {
		insertMsgClickCounts();
		MessageClickCount mcc11 = service.getByRowId(mcc1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(mcc11,2));
		
		MessageClickCount mcc12 = service.getByMsgInboxId(inbox1.getRowId());
		assertTrue(mcc11.equals(mcc12));
		
		// test paging for UI application
		PagingVo vo = new PagingVo();
		List<MessageClickCount> listpg = service.getBroadcastsWithPaging(vo);
		assertTrue(listpg.size()>0);
		int count = service.getMessageCountForWeb();
		assertTrue(count==listpg.size());
		
		// test update
		mcc2.setUpdtUserId("jpa test");
		service.update(mcc2);
		MessageClickCount mcc22 = service.getByRowId(mcc2.getRowId());
		assertTrue("jpa test".equals(mcc22.getUpdtUserId()));
		assertTrue(1==service.updateStartTime(mcc11.getMessageInbox().getRowId()));
		assertTrue(1==service.updateSentCount(mcc11.getMessageInbox().getRowId()));
		
		 // to work around Derby/EclipseLink error on the next "delete"
		service.update(mcc11);
		emService.clearEM();
		mcc11 = service.getByRowId(mcc11.getRowId());
		// end of work around
		
		// test delete
		service.delete(mcc11);
		try {
			service.getByRowId(mcc11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(mcc2.getRowId()));
		
		insertMsgClickCounts();
		assertTrue(1==service.deleteByRowId(mcc2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMsgClickCounts() throws IOException {
		Timestamp clickTime = new Timestamp(System.currentTimeMillis());
		// test insert
		mcc1 = new MessageClickCount();
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
		
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		mcc2 = new MessageClickCount();
		mcc2.setMessageInbox(inbox2);
		mcc2.setClickCount(1);
		mcc2.setOpenCount(0);
		mcc2.setComplaintCount(0);
		mcc2.setLastClickTime(clickTime);
		mcc2.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		mcc2.setMailingListRowId(mlist.getRowId());
		service.insert(mcc2);
	}
}
