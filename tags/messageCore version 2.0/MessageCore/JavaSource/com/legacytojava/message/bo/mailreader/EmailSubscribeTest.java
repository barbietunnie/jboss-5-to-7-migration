package com.legacytojava.message.bo.mailreader;

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

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.SimpleEmailSender;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-bo_jms-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class EmailSubscribeTest {
	static final Logger logger = Logger.getLogger(EmailSubscribeTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@BeforeClass
	public static void EmailSubscribePrepare() {
	}
	@Test
	public void testSendNotify() throws Exception {
		try {
			String user = "demolist1@localhost";
			sendNotify("subscribe", "Test Subscription Body Message", user);
			sendNotify("unsubscribe", "Test Subscription Body Message", user);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	void sendNotify(String subject, String body, String user) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("testfrom@localhost", false));
				mBean.setTo(InternetAddress.parse(user, false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject);
			mBean.setValue(body + " " + new Date());
			//SimpleEmailSender mSend = (SimpleEmailSender) SpringUtil.getAppContext().getBean(
			//		"simpleEmailSender");
			if (mSend != null) {
				mSend.sendMessage(mBean);
			}
			else {
				logger.info("JbMain.sendNotify(): message not sent, mSend not initialized.");
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
}
