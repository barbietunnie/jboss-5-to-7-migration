package com.es.bo.inbox;

import java.io.IOException;

import javax.annotation.Resource;
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

import com.es.bo.task.TaskScheduler;
import com.es.core.util.FileUtil;
import com.es.core.util.SpringUtil;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageBeanUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class RuleEngineTest {
	static final Logger logger = Logger.getLogger(RuleEngineTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	MsgInboxBo msgInboxBo;
	@Resource
	private MessageParserBo messageParser;
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
			DataValidationException {
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
		return FileUtil.loadFromFile("bouncedmails/", "BouncedMail_" + fileNbr + ".txt");
	}
}
