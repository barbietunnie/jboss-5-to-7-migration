package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.message.MessageInbox;
import jpa.service.message.MessageInboxService;
import jpa.service.msgin.MessageParserBo;
import jpa.service.task.SendMessage;

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
public class SendMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SendMessageTest.class);
	
	@Resource
	private SendMessage task;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private MessageParserBo parserBo;

	@BeforeClass
	public static void SendMessagePrepare() {
	}

	@Test
	public void testSendMessage() throws Exception {
		MessageBean mBean = new MessageBean();
		String fromaddr = "event.alert@localhost";
		String toaddr = "support@localhost";
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
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress().getAddress()));
		assertTrue(toaddr.equals(minbox.getToAddress().getAddress()));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		assertTrue(mBean.getBody().equals(minbox.getMsgBody()));
		
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		assertTrue(id.equals(minbox.getRowId()+""));
	}
}
