package com.legacytojava.message.test;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.bo.inbox.MessageParser;
import com.legacytojava.message.constant.RuleNameType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-jmsqueue_rmt-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class BroadcastTest {
	static final Logger logger = Logger.getLogger(BroadcastTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	private static AbstractApplicationContext factory;
	//@Resource
	//private MsgInboxBo msgInboxBo;
	@Resource
	private MessageParser messageParser;
	
	@BeforeClass
	public static void BroadcastoPrepare() {
		factory = SpringUtil.getAppContext();
	}

	@Test
	@Rollback(false)
	public void broadcast() throws Exception {
		logger.info("=================================================");
		logger.info("Testing Broadcast ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse("support@localhost"));
		messageBean.setTo(InternetAddress.parse("testto@localhost"));
		messageBean.setSubject("Test Broadcast message");
		messageBean.setBody("Test Broadcast message body.");
		messageBean.setRuleName(RuleNameType.BROADCAST.toString());
		messageBean.setMailingListId("SMPLLST1");
		messageBean.setBody("Dear ${CustomerName}:" + LF + messageBean.getBody());
		messageParser.parse(messageBean);
		System.out.println("MessageBean:" + LF + messageBean);
		TaskScheduler taskScheduler = new TaskScheduler(factory);
		taskScheduler.scheduleTasks(messageBean);
	}
	
	@Test
	public void subscribe() throws Exception {
		logger.info("=================================================");
		logger.info("Testing Subscribe ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse("test@test.com"));
		messageBean.setTo(InternetAddress.parse("jwang@localhost"));
		messageBean.setSubject("subscribe");
		messageBean.setBody("sign me up to the email mailing list");
		messageParser.parse(messageBean);
		TaskScheduler taskScheduler = new TaskScheduler(factory);
		taskScheduler.scheduleTasks(messageBean);
	}

	@Test
	public void unsubscribe() throws Exception {
		logger.info("=================================================");
		logger.info("Testing Unsubscribe ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse("test@test.com"));
		messageBean.setTo(InternetAddress.parse("jwang@localhost"));
		messageBean.setSubject("unsubscribe");
		messageBean.setBody("remove mefrom the email mailing list");
		messageParser.parse(messageBean);
		TaskScheduler taskScheduler = new TaskScheduler(factory);
		taskScheduler.scheduleTasks(messageBean);
	}
}
