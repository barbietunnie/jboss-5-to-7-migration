package com.es.bo.task;

import static org.junit.Assert.*;

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
import com.es.data.constant.MsgStatusCode;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class CloseMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(CloseMessageTest.class);
	
	@Resource
	private CloseMessage task;
	@Resource
	private MsgInboxDao inboxDao;

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
		MsgInboxVo randomRec = inboxDao.getRandomRecord();
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String emailIdStr = parser.createEmailId(randomRec.getMsgId());
		mBean.setValue(new Date()+ "Test body message." + LF + LF + emailIdStr + LF);
		mBean.setMailboxUser("testUser");
		MsgInboxVo minbox = inboxDao.getRandomRecord();
		if (MsgStatusCode.CLOSED.getValue().equals(minbox.getStatusId())) {
			minbox.setStatusId(MsgStatusCode.OPENED.getValue());
			inboxDao.update(minbox);
		}
		mBean.setMsgId(minbox.getMsgId());

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList().get(0));
		assertTrue(mBean.getMsgId().equals(ctx.getMsgIdList().get(0)));
		MsgInboxVo minbox2 = inboxDao.getByPrimaryKey(mBean.getMsgId());
		assertTrue(MsgStatusCode.CLOSED.getValue().equals(minbox2.getStatusId()));
	}
}
