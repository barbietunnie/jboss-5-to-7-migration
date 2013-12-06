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
import org.springframework.test.context.transaction.BeforeTransaction;
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

	private String fromaddr = "testfrom@localhost";
	private String toaddr = "testto@localhost";
	private MessageContext ctx = null; 

	@BeforeClass
	public static void ForwardPrepare() {
	}

	@BeforeTransaction
	public void performForwardToCsr() {
		/*
		 * perform the task in @BeforeTransaction block to have the new email
		 * address persisted before the JUnit assertions are executed. This will
		 * prevent the method call like "minbox.getToAddress()" from failing.
		 */
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

		ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$" + TableColumnName.SUBSCRIBER_CARE_ADDR.getValue());
		try {
			task.process(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void verifyForwardToCsr() {
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList().get(0));
		MsgInboxVo minbox = inboxService.getByPrimaryKey(ctx.getMsgIdList().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress()));
		MessageBean mBean = ctx.getMessageBean();
		SenderDataVo sender = senderService.getBySenderId(mBean.getSenderId());
		assertTrue(sender.getCustcareEmail().equals(minbox.getToAddress()));
		
		assertTrue(minbox.getMsgSubject().equals(mBean.getSubject()));
		assertTrue(minbox.getMsgBody().equals(mBean.getBody()));
	}
}
