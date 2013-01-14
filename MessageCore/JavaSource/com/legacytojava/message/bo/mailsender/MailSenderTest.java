package com.legacytojava.message.bo.mailsender;

import java.io.IOException;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.exception.DataValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-bo_jms-config.xml", "/spring-jms_remote-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MailSenderTest  {
	static final Logger logger = Logger.getLogger(MailSenderTest.class);
	@Resource
	private TaskBaseBo sendMailBo;
	private static JmsProcessor jmsProcessor;
	@BeforeClass
	public static void MailSenderPrepare() {
		jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean("jmsProcessor");
	}
	@Test
	public void testMailSender() throws Exception {
		int loops = 2; //Integer.MAX_VALUE;
		try {
			testSendMail(loops);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void testSendMail(int loops) throws DataValidationException, MessagingException,
			JMSException, IOException {
		sendMailBo.setJmsProcessor(jmsProcessor);
		long startTime = new java.util.Date().getTime();
		int i;
		for (i = 0; i < loops; i++) {
			String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
			String user = "user" + suffix + "@localhost";
			if (i % 13 == 0) {
				try {
					Thread.sleep(2 * 1000);
				}
				catch (InterruptedException e) {
					break;
				}
			}
			MessageBean messageBean = new MessageBean();
			messageBean.setSubject("Test MailSender - " + suffix + " " + new java.util.Date());
			messageBean.setBody("Test MailSender Body Message - " + suffix);
			messageBean.setFrom(InternetAddress.parse("testfrom@localhost", false));
			messageBean.setTo(InternetAddress.parse(user, false));
			logger.info("testSendMail() - before calling for " + user);
			Long mailsSent = (Long) sendMailBo.process(messageBean);
			logger.info("Emails Queued: " + mailsSent);
		}
		logger.info("Total Emails Queued: " + i + ", Time taken: "
				+ (new java.util.Date().getTime() - startTime) / 1000 + " seconds");
	}
}
