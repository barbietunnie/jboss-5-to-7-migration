package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.Constants;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SendMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SendMessageTest.class);
	
	@Resource
	private SendMessage task;
	@Resource
	private MsgInboxDao inboxDao;

	private EmailIdParser parser = EmailIdParser.getDefaultParser();
	
	@BeforeClass
	public static void SendMessagePrepare() {
	}

	@Test
	public void testSendMessage() {
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
		MsgInboxVo randomRec = inboxDao.getRandomRecord();
		String emailIdStr = parser.createEmailId(randomRec.getMsgId());
		logger.info("Email_Id to embed: " + emailIdStr);
		// The Email_Id will be replaced by MailSender with a new one generated from MsgId
		mBean.setValue(new Date()+ " Test body message." + LF + LF + emailIdStr + LF);
		mBean.setMailboxUser("testUser");
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		try {
			task.process(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList());
		MsgInboxVo minbox = inboxDao.getByPrimaryKey(ctx.getMsgIdList().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress()));
		assertTrue(toaddr.equals(minbox.getToAddress()));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		assertTrue(mBean.getBody().equals(minbox.getMsgBody()));
		
		String id = parser.parseMsg(mBean.getBody());
		assertTrue(id.equals(String.valueOf(minbox.getMsgId())));
	}
}
