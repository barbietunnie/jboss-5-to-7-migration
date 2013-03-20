package jpa.test.task;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.service.task.TaskSchedulerBo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class TaskSchedulerTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(TaskSchedulerTest.class);
	
	@Resource
	private TaskSchedulerBo taskBo;

	@BeforeClass
	public static void RuleMatchPrepare() {
	}

	@Test
	public void testTackScheduler() throws Exception {
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
			mBean.setTo(InternetAddress.parse("watched_maibox@domain.com", false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");
		// test #1 - null
		mBean.setRuleName(RuleNameEnum.GENERIC.getValue());
		taskBo.scheduleTasks(new MessageContext(mBean));
	}
}
