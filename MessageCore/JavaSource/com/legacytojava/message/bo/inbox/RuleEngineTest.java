package com.legacytojava.message.bo.inbox;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.exception.DataValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-bo_jms-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class RuleEngineTest {
	static final Logger logger = Logger.getLogger(RuleEngineTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	MsgInboxBo msgInboxBo;
	@Resource
	private MessageParser messageParser;
	static AbstractApplicationContext factory = null;
	final int startFrom = 1;
	final int endTo = 1;
	@BeforeClass
	public static void RuleEnginePrepare() {
		factory = SpringUtil.getAppContext();
		// TODO use annotation on factory
	}
	@Test
	public void processgetBouncedMails() throws IOException, MessagingException,
			DataValidationException, JMSException {
		for (int i = startFrom; i <= endTo; i++) {
			byte[] mailStream = getBouncedMail(i);
			MessageBean messageBean = MessageBeanUtil.createBeanFromStream(mailStream);
			messageBean.setIsReceived(true);
			messageParser.parse(messageBean);
			TaskScheduler taskScheduler = new TaskScheduler(factory);
			taskScheduler.scheduleTasks(messageBean);
		}
	}
	
	byte[] getBouncedMail(int fileNbr) throws IOException {
		InputStream is = getClass().getResourceAsStream(
				"bouncedmails/BouncedMail_" + fileNbr + ".txt");
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[512];
		int len = 0;
		try { 
			while ((len = bis.read(bytes, 0, bytes.length)) >= 0) {
				baos.write(bytes, 0, len);
			}
			byte[] mailStream = baos.toByteArray();
			baos.close();
			bis.close();
			return mailStream;
		}
		catch (IOException e) {
			throw e;
		}
	}
}
