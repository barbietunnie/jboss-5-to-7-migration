package jpa.test.msgout;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.message.MessageInbox;
import jpa.service.message.MessageInboxService;
import jpa.service.msgin.MessageInboxBo;
import jpa.service.msgin.MessageParserBo;
import jpa.service.msgout.MessageBeanBo;
import jpa.util.TestUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanBoTest {

	@BeforeClass
	public static void MessageBeanBoPrepare() {
	}

	@Autowired
	MessageBeanBo msgBeanBo;
	@Autowired
	MessageInboxBo inboxBo;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	MessageParserBo msgParser;

	private int msgId = -1;

	@Before
	@Rollback(false)
	public void prepare() throws MessagingException, IOException {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = readFromFile(fileName);
		msgParser.parse(msgBean);
		msgId = inboxBo.saveMessage(msgBean);
	}

	@Test
	public void testMessageBeanBo() throws MessagingException, IOException {
		MessageInbox inbox = inboxService.getAllDataByPrimaryKey(msgId);
		MessageBean mBean = msgBeanBo.createMessageBean(inbox);
		TestUtil.verifyMessageBean4BounceMail_1(mBean);
	}
	
	private MessageBean readFromFile(String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
