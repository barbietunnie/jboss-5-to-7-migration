package jpa.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import javax.mail.Part;
import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.ClientData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageAttachment;
import jpa.model.message.MessageAttachmentPK;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageAttachmentService;
import jpa.service.message.MessageInboxService;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageAttachmentTest {

	@BeforeClass
	public static void MessageAttachmentPrepare() {
	}

	@Autowired
	MessageAttachmentService service;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	ClientDataService clientService;
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

		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		String to_addr = client.getReturnPathLeft() + "@" + client.getDomainName();
		to = addrService.findSertAddress(to_addr);
		inbox1.setToAddrRowId(to.getRowId());
		inbox1.setClientDataRowId(client.getRowId());
		inbox1.setCustomerDataRowId(null);
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
	
	private MessageAttachment atc1;
	private MessageAttachment atc2;
	private MessageAttachment atc3;

	@Test
	public void messageAttachmentService() {
		insertMessageAttachments();
		MessageAttachment atc11 = service.getByRowId(atc1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(atc11,3));
		
		MessageAttachment atc12 = service.getByPrimaryKey(atc11.getMessageAttachmentPK());
		assertTrue(atc11.equals(atc12));
		
		// test update
		atc2.setUpdtUserId("jpa test");
		service.update(atc2);
		MessageAttachment hdr22 = service.getByRowId(atc2.getRowId());
		assertTrue("jpa test".equals(hdr22.getUpdtUserId()));
		
		// test delete
		service.delete(atc11);
		try {
			service.getByRowId(atc11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(atc2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
		
		insertMessageAttachments();
		assertTrue(1==service.deleteByPrimaryKey(atc1.getMessageAttachmentPK()));
		assertTrue(2==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMessageAttachments() {
		// test insert
		atc1 = new MessageAttachment();
		MessageAttachmentPK pk1 = new MessageAttachmentPK(inbox1,1,1);
		atc1.setMessageAttachmentPK(pk1);
		atc1.setAttachmentDisp(Part.ATTACHMENT);
		atc1.setAttachmentName("test.txt");
		atc1.setAttachmentType("text/plain; name=\"test.txt\"");
		atc1.setAttachmentValue("Test blob content goes here.".getBytes());
		service.insert(atc1);
		
		atc2 = new MessageAttachment();
		MessageAttachmentPK pk2 = new MessageAttachmentPK(inbox1,1,2);
		atc2.setMessageAttachmentPK(pk2);
		atc2.setAttachmentDisp(Part.INLINE);
		atc2.setAttachmentName("one.gif");
		atc2.setAttachmentType("image/gif; name=one.gif");
		atc2.setAttachmentValue(TestUtil.loadFromFile("one.gif"));
		service.insert(atc2);
		
		atc3 = new MessageAttachment();
		MessageAttachmentPK pk3 = new MessageAttachmentPK(inbox1,1,3);
		atc3.setMessageAttachmentPK(pk3);
		atc3.setAttachmentDisp(Part.ATTACHMENT);
		atc3.setAttachmentName("jndi.bin");
		atc3.setAttachmentType("application/octet-stream; name=\"jndi.bin\"");
		atc3.setAttachmentValue(TestUtil.loadFromFile("jndi.bin"));
		service.insert(atc3);
		
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==3);		
	}
}
