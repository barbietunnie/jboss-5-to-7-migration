package jpa.test;

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
import jpa.model.ClientData;
import jpa.model.EmailAddr;
import jpa.model.MessageInbox;
import jpa.model.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MessageInboxService;
import jpa.service.RuleLogicService;
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
	EmailAddrService addrService;
	@Autowired
	ClientDataService clientService;
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
		
		EmailAddr from = addrService.findSertAddress("test@test.com");
		in.setFromAddress(from);
		in.setReplytoAddress(null);

		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		String to_addr = client.getReturnPathLeft() + "@" + client.getDomainName();
		EmailAddr to = addrService.findSertAddress(to_addr);
		in.setToAddress(to);
		in.setClientData(client);
		in.setCustomerData(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.name());
		in.setRuleLogic(logic);
		in.setMsgContentType("multipart/mixed");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Message Body");
		service.insert(in);
		
		MessageInbox msg1 = service.getByPrimaryKey(in.getRowId());
		assertNotNull(msg1.getLeadMessage());
		System.out.println(StringUtil.prettyPrint(msg1,2));
		
		List<MessageInbox> lst1 = service.getByFromAddress(from.getAddress());
		assertFalse(lst1.isEmpty());
		List<MessageInbox> lst2 = service.getByToAddress(to.getAddress());
		assertFalse(lst2.isEmpty());

		List<MessageInbox> lst3 = service.getByLeadMsgId(msg1.getLeadMessage().getRowId());
		assertFalse(lst3.isEmpty());
		if (msg1.getReferringMessage()!=null) {
			List<MessageInbox> lst4 = service.getByReferringMsgId(msg1.getReferringMessage().getRowId());
			assertFalse(lst4.isEmpty());
		}

		List<MessageInbox> lst5 = service.getRecent(10);
		assertFalse(lst5.isEmpty());
		
		MessageInbox msg2  =service.getLastRecord();
		System.out.println(StringUtil.prettyPrint(msg2,1));
		
		try {
			service.getNextRecord(msg2);
			fail();
		}
		catch (NoResultException e) {}
		
		MessageInbox msg3  =service.getPrevoiusRecord(msg2);
		System.out.println(StringUtil.prettyPrint(msg3,1));
		
		assertFalse(msg1.equals(msg3));

		MessageInbox msg4  =service.getNextRecord(msg3);
		assertTrue(msg2.equals(msg4));
	}
}
