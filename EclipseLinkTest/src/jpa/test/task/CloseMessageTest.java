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
import jpa.service.task.CloseMessage;

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
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class CloseMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(CloseMessageTest.class);
	
	@Resource
	private CloseMessage task;
	@Resource
	private MessageInboxService inboxService;

	@BeforeClass
	public static void CloseMessagePrepare() {
	}

	@Test
	public void testCloseMessage() throws Exception {
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
		mBean.setValue(new Date()+ "Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		MessageInbox minbox = inboxService.getLastRecord();
		if (MsgStatusCode.CLOSED.getValue().equals(minbox.getStatusId())) {
			minbox.setStatusId(MsgStatusCode.OPENED.getValue());
			inboxService.update(minbox);
		}
		mBean.setMsgId(minbox.getRowId());

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		MessageInbox minbox2 = inboxService.getByPrimaryKey(mBean.getMsgId());
		assertTrue(MsgStatusCode.CLOSED.getValue().equals(minbox2.getStatusId()));
	}
}
