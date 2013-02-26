package jpa.test;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;

import jpa.data.preload.RuleNameEnum;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageNode;
import jpa.model.MessageStream;
import jpa.service.MessageStreamService;
import jpa.service.message.MessageParser;
import jpa.util.EmailAddrUtil;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanTest {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MessageBeanTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	MessageStreamService streamService;
	@Autowired
	MessageParser msgParser;
	
	@Test
	public void testMessageBean() throws MessagingException, IOException {
		MessageBean msgBean1 = testReadFromDatabase(1);
		assertNotNull(msgBean1);

		String filePath = "data/BouncedMail_1.txt";
		MessageBean msgBean2 = testReadFromFile(filePath);
		assertNotNull(msgBean2);
		assertTrue("support.hotline@jbatch.com".equals(msgBean2.getToAsString()));
		assertTrue("postmaster@synnex.com.au".equals(msgBean2.getFromAsString()));
		assertNotNull(msgBean2.getToEnvelope());
		assertTrue("jackwng@gmail.com".equals(EmailAddrUtil.emailAddrToString(msgBean2.getToEnvelope())));
		assertTrue("Delivery Status Notification (Failure)".equals(msgBean2.getSubject()));
		String contentType = msgBean2.getContentType();
		assertTrue(StringUtils.contains(contentType, "multipart/report;"));

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
	
	private MessageBean testReadFromFile(String filePath) throws MessagingException, IOException {
		byte[] mailStream = readFromFile(filePath);
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
	
	private byte[] readFromFile(String filePath) throws IOException {
		InputStream is = getClass().getResourceAsStream(filePath);
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[512];
		int len = 0;
		try { 
			while ((len = bis.read(bytes, 0, bytes.length)) > 0) {
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
