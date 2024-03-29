package com.es.bo.task;

import static org.junit.Assert.assertFalse;
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

import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.MsgStatusCode;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class OpenMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(OpenMessageTest.class);
	
	@Resource
	private OpenMessage task;
	@Resource
	private MsgInboxDao inboxDao;

	@BeforeClass
	public static void OpenMessagePrepare() {
	}

	@Test
	public void testOpenMessage() {
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
		MsgInboxVo minbox = inboxDao.getRandomRecord();
		if (MsgStatusCode.OPENED.getValue().equals(minbox.getStatusId())) {
			minbox.setStatusId(MsgStatusCode.CLOSED.getValue());
			inboxDao.update(minbox);
		}
		mBean.setMsgId(minbox.getMsgId());

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList().get(0));
		MsgInboxVo minbox2 = inboxDao.getByPrimaryKey(ctx.getMsgIdList().get(0));
		assertTrue(MsgStatusCode.OPENED.getValue().equals(minbox2.getStatusId()));
	}
}
