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
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.Constants;
import com.es.data.constant.TableColumnName;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.comm.SenderDataVo;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class ForwardtoCsrTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ForwardtoCsrTest.class);
	
	@Resource
	private ForwardToCsr task;
	@Resource
	private MsgInboxDao inboxService;
	@Resource
	private SenderDataDao senderService;

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
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList().get(0));
		MsgInboxVo minbox = inboxService.getByPrimaryKey(ctx.getMsgIdList().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress()));
		SenderDataVo sender = senderService.getBySenderId(mBean.getSenderId());
		assertTrue(sender.getCustcareEmail().equals(minbox.getToAddress()));
		
		assertTrue(minbox.getMsgSubject().equals(mBean.getSubject()));
		assertTrue(minbox.getMsgBody().equals(mBean.getBody()));
	}
}
