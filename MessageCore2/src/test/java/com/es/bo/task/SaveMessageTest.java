package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import com.es.bo.inbox.MessageParserBo;
import com.es.dao.inbox.MsgInboxDao;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SaveMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SaveMessageTest.class);
	
	@Resource
	private SaveMessage task;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private MessageParserBo parserBo;

	@BeforeClass
	public static void SaveMessagePrepare() {
	}

	@Test
	public void testSaveMessage() {
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
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		MsgInboxVo randomRec = inboxDao.getRandomRecord();
		String emailIdStr = parser.createEmailId(randomRec.getMsgId());
		mBean.setValue(new Date()+ "Test body message." + LF + LF + emailIdStr + LF);
		mBean.setMailboxUser("testUser");
		String ruleName = parserBo.parse(mBean);
		mBean.setRuleName(ruleName);

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList().get(0));
		MsgInboxVo minbox = inboxDao.getByPrimaryKey(ctx.getMsgIdList().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress()));
		assertTrue(toaddr.equals(minbox.getToAddress()));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		assertTrue(mBean.getBody().equals(minbox.getMsgBody()));
		String emailIdBody = parser.parseMsg(minbox.getMsgBody());
		assertNotNull(emailIdBody);
		assertTrue(emailIdBody.equals(String.valueOf(randomRec.getMsgId())));
	}
}
