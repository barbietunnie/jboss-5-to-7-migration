package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.EmailAddrType;
import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.MsgHeader;
import jpa.message.util.EmailIdParser;
import jpa.message.util.MsgHeaderUtil;
import jpa.model.EmailAddress;
import jpa.model.message.MessageAddress;
import jpa.model.message.MessageInbox;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;
import jpa.service.msgin.MessageParserBo;
import jpa.service.task.TaskSchedulerBo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class TaskSchedulerTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(TaskSchedulerTest.class);
	
	@Resource
	private TaskSchedulerBo taskBo;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private EmailAddressService emailService;
	@Resource
	private MessageParserBo parser;

	@BeforeClass
	public static void RuleMatchPrepare() {
	}

	@Test
	public void testTackScheduler() throws Exception {
		String from_addr = "event.alert@localhost";
		String to_addr = "watched_maibox@domain.com";
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse(from_addr, false));
			mBean.setTo(InternetAddress.parse(to_addr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		String subject = "A Exception occured";
		mBean.setSubject(subject);
		String body =  " Test body message.";
		mBean.setValue(new Date()+ body);
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		String final_rcpt = "testbounce@test.com";
		mBean.setFinalRcpt(final_rcpt);
		// test #1 - null
		mBean.setRuleName(RuleNameEnum.GENERIC.getValue());
		MessageContext ctx = new MessageContext(mBean);
		taskBo.scheduleTasks(ctx);
		
		// verify results
		assertTrue(ctx.getRowIds().size()==2);
		for (int i=0; i<ctx.getRowIds().size(); i++) {
			int rowid = ctx.getRowIds().get(i);
			MessageInbox inbox = inboxService.getAllDataByPrimaryKey(rowid);
			assertTrue(subject.equals(inbox.getMsgSubject()));
			assertTrue(inbox.getMsgBody().indexOf(body)>0);
			assertFalse(inbox.getMessageAddressList().isEmpty());
			for (MessageAddress addr : inbox.getMessageAddressList()) {
				if (EmailAddrType.FINAL_RCPT_ADDR.getValue().equals(addr.getAddressType())) {
					EmailAddress emailAddr = emailService.getByRowId(addr.getEmailAddrRowId());
					assertTrue(final_rcpt.equals(emailAddr.getAddress()));
				}
			}
			assertTrue(from_addr.equals(inbox.getFromAddress().getAddress()));
			if (i==0) {
				assertTrue(RuleNameEnum.GENERIC.getValue().equals(inbox.getRuleLogic().getRuleName()));
				assertTrue(to_addr.equals(inbox.getToAddress().getAddress()));
			}
			else if (i==1) {
				assertTrue(RuleNameEnum.SEND_MAIL.getValue().equals(inbox.getRuleLogic().getRuleName()));
				assertFalse(to_addr.equals(inbox.getToAddress().getAddress()));
				assertFalse(inbox.getMessageHeaderList().isEmpty());
				List<MsgHeader> msgHeaderList = MsgHeaderUtil.messageHeaderList2MsgHeaderList(inbox.getMessageHeaderList());
				String emailId = parser.parseHeaders(msgHeaderList);
				assertNotNull(emailId);
				assertTrue(emailId.equals(inbox.getRowId()+""));
			}
		}
	}
}
