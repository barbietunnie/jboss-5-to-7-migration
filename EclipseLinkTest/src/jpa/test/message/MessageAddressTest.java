package jpa.test.message;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.ClientData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageAddress;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageAddressService;
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
public class MessageAddressTest {

	@BeforeClass
	public static void MessageAddressPrepare() {
	}

	@Autowired
	MessageAddressService service;
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
	
	private MessageAddress adr1;
	private MessageAddress adr2;

	@Test
	public void messageAddressService() {
		insertAddr1AndAddr2();
		MessageAddress adr11 = service.getByRowId(adr1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(adr11,2));
		
		MessageAddress adr12 = service.getByPrimaryKey(inbox1.getRowId(), EmailAddrType.FROM_ADDR.getValue(), from.getAddress());
		assertTrue(adr11.equals(adr12));
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		MessageAddress adr22 = service.getByRowId(adr2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(adr11);
		try {
			service.getByRowId(adr11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		
		insertAddr1AndAddr2();
		assertTrue(1==service.deleteByPrimaryKey(inbox1.getRowId(), EmailAddrType.FROM_ADDR.getValue(), from.getAddress()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertAddr1AndAddr2() {
		// test insert
		adr1 = new MessageAddress();
		adr1.setMessageInbox(inbox1);
		adr1.setAddressType(EmailAddrType.FROM_ADDR.getValue());
		adr1.setEmailAddrRowId(from.getRowId());
		service.insert(adr1);
		
		adr2 = new MessageAddress();
		adr2.setMessageInbox(inbox1);
		adr2.setAddressType(EmailAddrType.TO_ADDR.getValue());
		adr2.setEmailAddrRowId(to.getRowId());
		service.insert(adr2);
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==2);		
	}
}
