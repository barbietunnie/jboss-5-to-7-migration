package jpa.test.task;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.MsgStatusCode;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.message.MessageInbox;
import jpa.service.message.MessageInboxService;
import jpa.service.task.OpenMessage;

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
public class OpenMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(OpenMessageTest.class);
	
	@Resource
	private OpenMessage task;
	@Resource
	private MessageInboxService inboxService;

	@BeforeClass
	public static void OpenMessagePrepare() {
	}

	@Test
	public void testOpenMessage() throws Exception {
		MessageBean mBean = new MessageBean();
		String fromaddr = "event.alert@localhost";
		String toaddr = "watched_maibox@domain.com";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");
		MessageInbox minbox = inboxService.getLastRecord();
		if (MsgStatusCode.OPENED.getValue().equals(minbox.getStatusId())) {
			minbox.setStatusId(MsgStatusCode.CLOSED.getValue());
			inboxService.update(minbox);
		}
		mBean.setMsgId(minbox.getRowId());

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		MessageInbox minbox2 = inboxService.getByPrimaryKey(mBean.getMsgId());
		assertTrue(MsgStatusCode.OPENED.getValue().equals(minbox2.getStatusId()));
	}
}
