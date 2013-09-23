package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.Constants;
import jpa.constant.TableColumnName;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.SenderData;
import jpa.model.message.MessageInbox;
import jpa.service.SenderDataService;
import jpa.service.message.MessageInboxService;
import jpa.service.task.ForwardToCsr;

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
public class ForwardtoCsrTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ForwardtoCsrTest.class);
	
	@Resource
	private ForwardToCsr task;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private SenderDataService senderService;

	@BeforeClass
	public static void ForwardPrepare() {
	}

	@Test
	public void testForwardToCsr() throws Exception {
		String fromaddr = "testfrom@localhost";
		String toaddr = "testto@localhost";
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message.");
		mBean.setMailboxUser("testUser");
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$" + TableColumnName.SUBSCRIBER_CARE_ADDR.getValue());
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress().getAddress()));
		SenderData sender = senderService.getBySenderId(mBean.getSenderId());
		assertTrue(sender.getSubrCareEmail().equals(minbox.getToAddress().getAddress()));
		
		assertTrue(minbox.getMsgSubject().equals(mBean.getSubject()));
		assertTrue(minbox.getMsgBody().equals(mBean.getBody()));
	}
}
