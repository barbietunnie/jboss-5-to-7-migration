package jpa.test;

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
import jpa.model.ClientData;
import jpa.model.EmailAddr;
import jpa.model.MailingList;
import jpa.model.MessageUnsubComment;
import jpa.model.MessageInbox;
import jpa.model.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MailingListService;
import jpa.service.MessageUnsubCommentService;
import jpa.service.MessageInboxService;
import jpa.service.RuleLogicService;
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
	EmailAddrService addrService;
	@Autowired
	ClientDataService clientService;
	@Autowired
	RuleLogicService logicService;
	@Autowired
	MailingListService listService;

	private MessageInbox inbox1;
	private EmailAddr from;
	private EmailAddr to;
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
