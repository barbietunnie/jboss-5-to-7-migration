package jpa.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.constant.XHeaderName;
import jpa.data.preload.RuleNameEnum;
import jpa.model.ClientData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageHeader;
import jpa.model.message.MessageHeaderPK;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageHeaderService;
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
public class MessageHeaderTest {

	@BeforeClass
	public static void MessageHeaderPrepare() {
	}

	@Autowired
	MessageHeaderService service;
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
	
	private MessageHeader hdr1;
	private MessageHeader hdr2;
	private MessageHeader hdr3;

	@Test
	public void messageHeaderService() {
		insertMessageHeaders();
		MessageHeader hdr11 = service.getByRowId(hdr1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(hdr11,2));
		
		MessageHeader hdr12 = service.getByPrimaryKey(hdr11.getMessageHeaderPK());
		assertTrue(hdr11.equals(hdr12));
		
		// test update
		hdr2.setUpdtUserId("jpa test");
		service.update(hdr2);
		MessageHeader hdr22 = service.getByRowId(hdr2.getRowId());
		assertTrue("jpa test".equals(hdr22.getUpdtUserId()));
		
		// test delete
		service.delete(hdr11);
		try {
			service.getByRowId(hdr11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(hdr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
		
		insertMessageHeaders();
		assertTrue(1==service.deleteByPrimaryKey(hdr1.getMessageHeaderPK()));
		assertTrue(2==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMessageHeaders() {
		// test insert
		hdr1 = new MessageHeader();
		MessageHeaderPK pk1 = new MessageHeaderPK(inbox1,1);
		hdr1.setMessageHeaderPK(pk1);
		hdr1.setHeaderName(XHeaderName.MAILER.getValue());
		hdr1.setHeaderValue("Mailserder");
		service.insert(hdr1);
		
		hdr2 = new MessageHeader();
		MessageHeaderPK pk2 = new MessageHeaderPK(inbox1,2);
		hdr2.setMessageHeaderPK(pk2);
		hdr2.setHeaderName(XHeaderName.RETURN_PATH.getValue());
		hdr2.setHeaderValue("demolist1@localhost");
		service.insert(hdr2);
		
		hdr3 = new MessageHeader();
		MessageHeaderPK pk3 = new MessageHeaderPK(inbox1,3);
		hdr3.setMessageHeaderPK(pk3);
		hdr3.setHeaderName(XHeaderName.CLIENT_ID.getValue());
		hdr3.setHeaderValue(Constants.DEFAULT_CLIENTID);
		service.insert(hdr3);
		
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==3);		
	}
}
