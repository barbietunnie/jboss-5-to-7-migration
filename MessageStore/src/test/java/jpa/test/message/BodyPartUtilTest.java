package jpa.test.message;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.mail.MessagingException;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.service.msgin.MessageParserBo;
import jpa.util.TestUtil;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
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
public class BodyPartUtilTest {
	static final Logger logger = Logger.getLogger(BodyPartUtilTest.class);
	static final String LF = System.getProperty("line.separator", "\n");
	
	@BeforeClass
	public static void BodyPartUtilPrepare() {
	}

	@Autowired
	private MessageParserBo msgParser;
	
	@Test
	public void testBodyPartUtil() throws MessagingException {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = testReadFromFile(fileName);
		assertNotNull(msgBean);

		TestUtil.verifyMessageBean4BounceMail_1(msgBean);

		// parse the message bean to set rule name
		String ruleName = msgParser.parse(msgBean);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
		
		String finalRcpt = msgBean.getFinalRcpt();
		assertTrue("jackwnn@synnex.com.au".equals(finalRcpt));
	}

	private MessageBean testReadFromFile(String fileName) throws MessagingException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
