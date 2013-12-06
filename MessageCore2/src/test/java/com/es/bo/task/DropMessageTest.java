package com.es.bo.task;

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

import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class DropMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(DropMessageTest.class);
	
	@Resource
	private DropMessage task;

	@BeforeClass
	public static void DropMessagePrepare() {
	}

	@Test
	public void testDropMessage() {
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

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertTrue(ctx.getMsgIdList().isEmpty());
	}
}
