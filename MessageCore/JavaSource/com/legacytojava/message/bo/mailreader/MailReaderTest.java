package com.legacytojava.message.bo.mailreader;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
public class MailReaderTest {
	static final Logger logger = Logger.getLogger(MailReaderTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@Test
	public void testMailReader() throws Exception {
		try {
			int loops = 1; //Integer.MAX_VALUE;
			for (int i = 0; i < loops; i++) {
				String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
				String user = "user" + suffix + "@localhost";
				if (i % 13 == 0) {
					try {
						Thread.sleep(1 * 1000);
					}
					catch (InterruptedException e) {
						break;
					}
				}
				sendNotify("Test MailReader - " + suffix, "Test MailReader Body Message - " + suffix, user);
			}
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
				mBean.setTo(InternetAddress.parse("testto-10.07410251.0-jsmith=test.com@localhost"));
				//mBean.setCc(InternetAddress.parse("jwang@localhost,twang@localhost", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject + " " + new Date());
			mBean.setValue(body);
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
