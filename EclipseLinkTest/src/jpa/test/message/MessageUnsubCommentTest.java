package jpa.test.message;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageUnsubComment;
import jpa.model.rule.RuleLogic;
import jpa.service.SenderDataService;
import jpa.service.EmailAddressService;
import jpa.service.MailingListService;
import jpa.service.message.MessageInboxService;
import jpa.service.message.MessageUnsubCommentService;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageUnsubCommentTest {

	@BeforeClass
	public static void MessageUnsubCommentPrepare() {
	}

	@Autowired
	MessageUnsubCommentService service;
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
	
	private MessageUnsubComment adr1;
	private MessageUnsubComment adr2;

	@Test
	public void messageUnsubCommentService() throws IOException {
		insertUnsubComments();
		MessageUnsubComment adr11 = service.getByRowId(adr1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(adr11,2));
		
		MessageUnsubComment adr12 = service.getByMsgInboxId(inbox1.getRowId());
		assertTrue(adr11.equals(adr12));
		
		assertTrue(2==service.getByFromAddress(from.getAddress()).size());
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		MessageUnsubComment adr22 = service.getByRowId(adr2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(adr11);
		try {
			service.getByRowId(adr11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		
		insertUnsubComments();
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertUnsubComments() throws IOException {
		// test insert
		adr1 = new MessageUnsubComment();
		adr1.setMessageInbox(inbox1);
		adr1.setComments("jpa test unsub comment 1");
		adr1.setEmailAddrRowId(from.getRowId());
		adr1.setMailingListRowId(mlist.getRowId());
		service.insert(adr1);
		
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		adr2 = new MessageUnsubComment();
		adr2.setMessageInbox(inbox2);
		adr2.setComments("jpa test unsub comment 2");
		adr2.setEmailAddrRowId(from.getRowId());
		adr2.setMailingListRowId(mlist.getRowId());
		service.insert(adr2);
	}
}
