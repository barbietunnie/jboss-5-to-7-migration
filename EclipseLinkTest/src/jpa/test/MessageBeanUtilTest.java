package jpa.test;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.mail.MessagingException;

import jpa.constant.RuleDataName;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanUtilTest {
	static final Logger logger = Logger.getLogger( MessageBeanUtilTest.class);
	static final String LF = System.getProperty("line.separator", "\n");

	@BeforeClass
	public static void MessageBeanutilPrepare() {
	}

	@Test
	public void testMessageBeanUtil() throws MessagingException, IOException {
		String filePath = "data/BouncedMail_1.txt";
		MessageBean msgBean = testReadFromFile(filePath);
		assertNotNull(msgBean);
		
		List<String> methodNameList = MessageBeanUtil.getMessageBeanMethodNames();
		StringBuffer sb = new StringBuffer();
		sb.append("========= MessageBean method name list ==========" + LF);
		for (int i=0; i<methodNameList.size(); i++) {
			sb.append(methodNameList.get(i) + LF);
		}
		sb.append("=========== End of method name list =============" + LF);
		logger.info(sb.toString());

		for (RuleDataName name : RuleDataName.values()) {
			MessageBeanUtil.invokeMethod(msgBean, name.getValue());
			// TODO
		}

	}

	private MessageBean testReadFromFile(String filePath) throws MessagingException, IOException {
		byte[] mailStream = readFromFile(filePath);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
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
