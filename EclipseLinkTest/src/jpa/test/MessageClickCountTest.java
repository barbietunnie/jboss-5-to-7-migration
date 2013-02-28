package jpa.test;

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
import jpa.model.ClientData;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.message.MessageClickCount;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.ClientDataService;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
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
	ClientDataService clientService;
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
		
		List<MailingList> mlists=listService.getAll(true);
		mlist=mlists.get(0);
	}
	
	private MessageClickCount adr1;
	private MessageClickCount adr2;

	@Test
	public void messageClickCountService() throws IOException {
		insertMsgClickCounts();
		MessageClickCount adr11 = service.getByRowId(adr1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(adr11,2));
		
		MessageClickCount adr12 = service.getByMsgInboxId(inbox1.getRowId());
		assertTrue(adr11.equals(adr12));
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		MessageClickCount adr22 = service.getByRowId(adr2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(adr11);
		try {
			service.getByRowId(adr11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		
		insertMsgClickCounts();
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMsgClickCounts() throws IOException {
		Timestamp clickTime = new Timestamp(System.currentTimeMillis());
		// test insert
		adr1 = new MessageClickCount();
		adr1.setMessageInbox(inbox1);
		adr1.setClickCount(3);
		adr1.setOpenCount(2);
		adr1.setComplaintCount(0);
		adr1.setLastClickTime(clickTime);
		adr1.setLastOpenTime(clickTime);
		adr1.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		adr1.setMailingListRowId(mlist.getRowId());
		service.insert(adr1);
		
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		adr2 = new MessageClickCount();
		adr2.setMessageInbox(inbox2);
		adr2.setClickCount(1);
		adr2.setOpenCount(0);
		adr2.setComplaintCount(0);
		adr2.setLastClickTime(clickTime);
		adr2.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		adr2.setMailingListRowId(mlist.getRowId());
		service.insert(adr2);
	}
}
