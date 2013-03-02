package jpa.test;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;

import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.message.MessageStream;
import jpa.service.mailbox.MessageInboxBo;
import jpa.service.message.MessageParser;
import jpa.service.message.MessageStreamService;

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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageInboxBoTest {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MessageInboxBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	MessageStreamService streamService;
	@Autowired
	MessageParser msgParser;
	@Autowired
	MessageInboxBo msgInboxBo;
	
	@Test
	public void testMessageInboxBo() throws MessagingException, IOException {
		MessageBean msgBean1 = testReadFromDatabase(1);
		assertNotNull(msgBean1);
		// parse the message bean to set rule name
		msgParser.parse(msgBean1);

		String filePath = "data/BouncedMail_1.txt";
		MessageBean msgBean2 = testReadFromFile(filePath);
		assertNotNull(msgBean2);
		// parse the message bean to set rule name
		msgParser.parse(msgBean2);
		
		System.out.println("Msgid = " + msgInboxBo.saveMessage(msgBean1));
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
