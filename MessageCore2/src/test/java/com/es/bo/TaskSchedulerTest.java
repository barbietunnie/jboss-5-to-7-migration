package com.es.bo;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.inbox.MessageParserBo;
import com.es.bo.task.TaskSchedulerBo;
import com.es.core.util.FileUtil;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageBeanUtil;
import com.es.msgbean.MessageContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class TaskSchedulerTest {
	static final Logger logger = Logger.getLogger(TaskSchedulerTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");

	@Resource
	private TaskSchedulerBo taskScheduler;
	@Resource
	private MessageParserBo messageParser;
	@BeforeClass
	public static void RuleEnginePrepare() {
	}

	@Test
	public void processgetBouncedMails() {
		int startFrom = 1;
		int endTo = 2;
		int failedCount = 0;
		for (int i = startFrom; i <= endTo; i++) {
			try {
				byte[] mailStream = getBouncedMail(i);
				MessageBean messageBean = MessageBeanUtil.createBeanFromStream(mailStream);
				messageBean.setIsReceived(true);
				messageParser.parse(messageBean);
				taskScheduler.scheduleTasks(new MessageContext(messageBean));
			}
			catch (Exception e) {
				e .printStackTrace();
				failedCount++;
			}
		}
		if (failedCount>0) {
			fail();
		}
	}
	
	byte[] getBouncedMail(int fileNbr) throws IOException {
		return FileUtil.loadFromFile("bouncedmails/", "BouncedMail_" + fileNbr + ".txt");
	}
}
