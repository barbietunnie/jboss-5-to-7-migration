package jpa.test.msgin;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.service.msgin.MessageParserBo;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MessageParserTest {
	static final Logger logger = Logger.getLogger(MessageParserTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MessageParserBo messageParser;
	static AbstractApplicationContext factory = null;
	final int fromNumber = 1;
	final int toNumber = 24;
	@BeforeClass
	public static void RuleEnginePrepare() {
	}
	@Test
	public void processgetBouncedMails() throws IOException, MessagingException {
		for (int i = fromNumber; i <= toNumber; i++) {
			byte[] mailStream = getBouncedMail(i);
			MessageBean messageBean = MessageBeanUtil.createBeanFromStream(mailStream);
			messageBean.setIsReceived(true);
			String ruleName = messageParser.parse(messageBean);
			logger.info("##### RULE NAME [" + i + "]: " + ruleName);
			if (i==24) {
				assertTrue(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT.getValue().equals(ruleName));
			}
			else {
				assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
			}
		}
	}
	
	byte[] getBouncedMail(int fileNbr) throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream(
				"jpa/test/bouncedmails/BouncedMail_" + fileNbr + ".txt");
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[512];
		int len = 0;
		try { 
			while ((len = bis.read(bytes, 0, bytes.length)) >= 0) {
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
