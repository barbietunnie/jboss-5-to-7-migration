package jpa.test.message;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageActionLog;
import jpa.model.message.MessageActionLogPK;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.SenderDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageActionLogService;
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
public class MessageActionLogTest {

	@BeforeClass
	public static void MessageActionLogPrepare() {
	}

	@Autowired
	MessageActionLogService service;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	RuleLogicService logicService;

	private MessageInbox inbox1;
	private EmailAddress from;
	private EmailAddress to;

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
	}
	
	private MessageActionLog log1;
	private MessageActionLog log2;

	@Test
	public void messageActionLogService() {
		insertActionLogs();
		MessageActionLog log11 = service.getByRowId(log1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(log11,3));
		
		MessageActionLog log12 = service.getByPrimaryKey(log11.getMessageActionLogPK());
		assertTrue(log11.equals(log12));
		
		// test update
		log2.setUpdtUserId("jpa test");
		service.update(log2);
		MessageActionLog adr22 = service.getByRowId(log2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		assertTrue(1==service.getByLeadMsgId(inbox1.getRowId()).size());
		
		// test delete
		service.delete(log11);
		try {
			service.getByRowId(log11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(log2.getRowId()));
		assertTrue(0==service.deleteByLeadMsgId(log2.getMessageActionLogPK().getLeadMessageRowId()));
		
		insertActionLogs();
		assertTrue(1==service.deleteByPrimaryKey(log1.getMessageActionLogPK()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertActionLogs() {
		// test insert
		log1 = new MessageActionLog();
		MessageActionLogPK pk1 = new MessageActionLogPK(inbox1, inbox1.getLeadMessageRowId());
		log1.setMessageActionLogPK(pk1);
		log1.setActionService(RuleNameEnum.SEND_MAIL.name());
		log1.setParameters("sent");
		service.insert(log1);

		log2 = new MessageActionLog();
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		MessageActionLogPK pk2 = new MessageActionLogPK(inbox1,inbox2.getRowId());
		log2.setMessageActionLogPK(pk2);
		log2.setActionService(RuleNameEnum.CSR_REPLY.name());
		log2.setParameters("rowid=122");
		service.insert(log2);
		
		assertTrue(2==service.getByMsgInboxId(inbox1.getRowId()).size());
	}
}
