package jpa.test.message;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageStream;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.message.MessageInboxService;
import jpa.service.message.MessageStreamService;
import jpa.service.rule.RuleLogicService;
import jpa.util.StringUtil;
import jpa.util.TestUtil;

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
public class MessageStreamTest {

	@BeforeClass
	public static void MessageStreamPrepare() {
	}

	@Autowired
	MessageStreamService service;
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
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		inbox1.setRuleLogicRowId(logic.getRowId());
		inbox1.setMsgContentType("multipart/mixed");
		inbox1.setBodyContentType("text/plain");
		inbox1.setMsgBody("Test Message Body");
		inboxService.insert(inbox1);
	}
	
	private MessageStream adr1;
	private MessageStream adr2;

	@Test
	public void messageAddressService() throws IOException {
		insertMsgStreams();
		MessageStream adr11 = service.getByRowId(adr1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(adr11,2));
		
		MessageStream adr12 = service.getByMsgInboxId(inbox1.getRowId());
		assertTrue(adr11.equals(adr12));
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		MessageStream adr22 = service.getByRowId(adr2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(adr11);
		try {
			service.getByRowId(adr11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		
		insertMsgStreams();
		assertNotNull(service.getLastRecord());
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMsgStreams() throws IOException {
		// test insert
		adr1 = new MessageStream();
		adr1.setMessageInbox(inbox1);
		adr1.setMsgSubject("test jpa subject");
		adr1.setFromAddrRowId(from.getRowId());
		adr1.setToAddrRowId(to.getRowId());
		adr1.setMsgStream(TestUtil.loadFromSamples("BouncedMail_1.txt"));;
		service.insert(adr1);
		
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		try {
			adr2 = service.getByMsgInboxId(inbox2.getRowId());
		}
		catch (NoResultException e) {
			adr2 = new MessageStream();
			adr2.setMessageInbox(inbox2);
			adr2.setMsgSubject("jpa test");
			adr2.setFromAddrRowId(from.getRowId());
			adr2.setMsgStream(TestUtil.loadFromSamples("BouncedMail_1.txt"));
			service.insert(adr2);
		}
	}
}
