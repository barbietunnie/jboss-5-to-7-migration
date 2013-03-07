package jpa.test.message;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.SenderDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.util.StringUtil;

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
public class MessageInboxTest {

	@BeforeClass
	public static void MessageInboxPrepare() {
	}

	@Autowired
	MessageInboxService service;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	RuleLogicService logicService;

	@Test
	public void messageInboxService() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		MessageInbox in = new MessageInbox();
		
		in.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		in.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		in.setMsgSubject("Test Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		
		EmailAddress from = addrService.findSertAddress("test@test.com");
		in.setFromAddrRowId(from.getRowId());
		in.setReplytoAddrRowId(null);

		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		String to_addr = sender.getReturnPathLeft() + "@" + sender.getDomainName();
		EmailAddress to = addrService.findSertAddress(to_addr);
		in.setToAddrRowId(to.getRowId());
		in.setSenderDataRowId(sender.getRowId());
		in.setSubscriberDataRowId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		in.setRuleLogicRowId(logic.getRowId());
		in.setMsgContentType("multipart/mixed");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Message Body");
		service.insert(in);
		
		MessageInbox msg1 = service.getByPrimaryKey(in.getRowId());
		assertNotNull(msg1.getLeadMessageRowId());
		System.out.println(StringUtil.prettyPrint(msg1,2));
		
		List<MessageInbox> lst1 = service.getByFromAddress(from.getAddress());
		assertFalse(lst1.isEmpty());
		List<MessageInbox> lst2 = service.getByToAddress(to.getAddress());
		assertFalse(lst2.isEmpty());

		List<MessageInbox> lst3 = service.getByLeadMsgId(msg1.getLeadMessageRowId());
		assertFalse(lst3.isEmpty());
		if (msg1.getReferringMessageRowId()!=null) {
			List<MessageInbox> lst4 = service.getByReferringMsgId(msg1.getReferringMessageRowId());
			assertFalse(lst4.isEmpty());
		}

		List<MessageInbox> lst5 = service.getRecent(10);
		assertFalse(lst5.isEmpty());
		
		MessageInbox msg2  =service.getLastRecord();
		System.out.println(StringUtil.prettyPrint(msg2,2));
		MessageInbox msg22  =service.getAllDataByPrimaryKey(msg2.getRowId());
		System.out.println(StringUtil.prettyPrint(msg22,2));
		
		try {
			service.getNextRecord(msg2);
			fail();
		}
		catch (NoResultException e) {}
		
		try {
			MessageInbox msg3  =service.getPrevoiusRecord(msg2);
			System.out.println(StringUtil.prettyPrint(msg3,1));
			
			assertFalse(msg1.equals(msg3));
	
			MessageInbox msg4  =service.getNextRecord(msg3);
			assertTrue(msg2.equals(msg4));
		}
		catch (NoResultException e) {
			assertTrue("MessageInbox table is empty", true);
		}
		
		// test delete
		assertTrue(1==service.deleteByRowId(msg2.getRowId()));
		try {
			service.getByRowId(msg2.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		// test duplicate message
		assertFalse(service.isMessageIdDuplicate("jpatest-smtp-message-id"));
		assertTrue(service.isMessageIdDuplicate("jpatest-smtp-message-id"));
		service.purgeMessageIdDuplicate(1);
	}
}
