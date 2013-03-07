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
import jpa.model.message.MessageDeliveryStatus;
import jpa.model.message.MessageDeliveryStatusPK;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.SenderDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageDeliveryStatusService;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageDeliveryStatusTest {

	@BeforeClass
	public static void MessageDeliveryStatusPrepare() {
	}

	@Autowired
	MessageDeliveryStatusService service;
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
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.name());
		inbox1.setRuleLogicRowId(logic.getRowId());
		inbox1.setMsgContentType("multipart/mixed");
		inbox1.setBodyContentType("text/plain");
		inbox1.setMsgBody("Test Message Body");
		inboxService.insert(inbox1);
	}
	
	private MessageDeliveryStatus log1;
	private MessageDeliveryStatus log2;

	@Test
	public void messageDeliveryStatusService() {
		insertDeliveryStatuss();
		MessageDeliveryStatus log11 = service.getByRowId(log1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(log11,3));
		
		MessageDeliveryStatus log12 = service.getByPrimaryKey(log11.getMessageDeliveryStatusPK());
		assertTrue(log11.equals(log12));
		
		// test update
		log2.setUpdtUserId("jpa test");
		service.update(log2);
		MessageDeliveryStatus adr22 = service.getByRowId(log2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		assertTrue(2==service.getByMsgInboxId(inbox1.getRowId()).size());
		
		// test delete
		service.delete(log11);
		try {
			service.getByRowId(log11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(log2.getRowId()));
		
		insertDeliveryStatuss();
		assertTrue(1==service.deleteByPrimaryKey(log1.getMessageDeliveryStatusPK()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertDeliveryStatuss() {
		// test insert
		log1 = new MessageDeliveryStatus();
		MessageDeliveryStatusPK pk1 = new MessageDeliveryStatusPK(inbox1, from.getRowId());
		log1.setMessageDeliveryStatusPK(pk1);
		log1.setFinalRecipientAddress(from.getAddress());
		log1.setDeliveryStatus("jpa test delivery status");
		service.insert(log1);

		log2 = new MessageDeliveryStatus();
		MessageDeliveryStatusPK pk2 = new MessageDeliveryStatusPK(inbox1,to.getRowId());
		log2.setMessageDeliveryStatusPK(pk2);
		log2.setFinalRecipientAddress(to.getAddress());
		log2.setDsnStatus("jpa test DSN status");
		log2.setDsnReason("jpa test DSN reason");
		log2.setDsnText("jpa test DSN text");
		service.insert(log2);
		
		service.insertWithDelete(log2);
		
		assertTrue(2==service.getByMsgInboxId(inbox1.getRowId()).size());
	}
}
