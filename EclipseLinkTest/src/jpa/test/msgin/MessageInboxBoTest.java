package jpa.test.msgin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;

import jpa.constant.MsgStatusCode;
import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageStream;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;
import jpa.service.message.MessageStreamService;
import jpa.service.msgin.MessageInboxBo;
import jpa.service.msgin.MessageParserBo;
import jpa.util.TestUtil;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageInboxBoTest {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MessageInboxBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	MessageStreamService streamService;
	@Autowired
	MessageParserBo msgParser;
	@Autowired
	MessageInboxBo msgInboxBo;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	private EmailAddressService emailService;
	
	@Test
	public void testMessageInboxBo() throws MessagingException, IOException {
		MessageBean msgBean1 = testReadFromDatabase(100);
		assertNotNull(msgBean1);
		// parse the message bean to set rule name
		String ruleName = msgParser.parse(msgBean1);
		assertNotNull(ruleName);

		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean2 = testReadFromFile(fileName);
		assertNotNull(msgBean2);
		// parse the message bean to set rule name
		ruleName = msgParser.parse(msgBean2);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
		
		int rowId = msgInboxBo.saveMessage(msgBean2);
		logger.info("Msgid = " + rowId);
		MessageInbox inbox = TestUtil.verifyBouncedMail_1(rowId, inboxService, emailService);
		assertTrue(MsgStatusCode.PENDING.getValue().equals(inbox.getStatusId()));
	}
	
	private MessageBean testReadFromDatabase(int msgId) throws MessagingException {
		byte[] mailStream = readFromDatabase(msgId);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private MessageBean testReadFromFile(String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromFile(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private byte[] readFromDatabase(int msgId) {
		MessageStream msgStreamVo = null;
		try {
			msgStreamVo = streamService.getByMsgInboxId(msgId);
		}
		catch (NoResultException e) {
			try {
				msgStreamVo = streamService.getLastRecord();
			}
			catch (NoResultException e2) {
				String fileName = "BouncedMail_2.txt";
				return TestUtil.loadFromFile(fileName);
			}
		}
		logger.info("MsgStreamDao - getByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo.getMsgStream();
	}
}
