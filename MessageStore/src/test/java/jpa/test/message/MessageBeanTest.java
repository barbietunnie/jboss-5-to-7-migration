package jpa.test.message;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;

import jpa.data.preload.RuleNameEnum;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageNode;
import jpa.model.message.MessageStream;
import jpa.service.message.MessageStreamService;
import jpa.service.msgin.MessageParserBo;
import jpa.util.StringUtil;
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
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanTest {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MessageBeanTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	MessageStreamService streamService;
	@Autowired
	MessageParserBo msgParser;
	
	@Test
	public void testMessageBean() throws MessagingException, IOException {
		MessageBean msgBean1 = testReadFromDatabase(1);
		assertNotNull(msgBean1);

		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean2 = testReadFromFile(fileName);
		assertNotNull(msgBean2);
		TestUtil.verifyMessageBean4BounceMail_1(msgBean2);

		msgBean2.setToPlainText(true);
		List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean2);
		logger.info("Number of Attachments: " + mNodes.size());
		logger.info("******************************");
		logger.info("MessageBean created:" + LF + msgBean2);
		
		// parse the message bean to set rule name
		String ruleName = msgParser.parse(msgBean2);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
		
		String finalRcpt = msgBean2.getFinalRcpt();
		assertTrue("jackwnn@synnex.com.au".equals(finalRcpt));
		
		javax.mail.Message msg = MessageBeanUtil.createMimeMessage(msgBean2);
		logger.info(StringUtil.prettyPrint(msg));
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
			msgStreamVo = streamService.getLastRecord();
		}
		System.out.println("MsgStreamDao - getByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo.getMsgStream();
	}
}
