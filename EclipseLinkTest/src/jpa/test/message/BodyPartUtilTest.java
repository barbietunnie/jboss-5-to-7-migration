package jpa.test.message;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import jpa.message.BodypartBean;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.util.StringUtil;
import jpa.util.TestUtil;

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
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class BodyPartUtilTest {
	static final Logger logger = Logger.getLogger(BodyPartUtilTest.class);
	static final String LF = System.getProperty("line.separator", "\n");
	
	@BeforeClass
	public static void BodyPartUtilPrepare() {
	}

	@Test
	public void testBodyPartUtil() throws MessagingException, IOException {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = testReadFromFile(fileName);
		assertNotNull(msgBean);

		BodypartBean bodyBean1 = BodypartUtil.retrieveDlvrStatus(msgBean, 0);
		logger.info(StringUtil.prettyPrint(bodyBean1));
		BodypartBean bodyBean2 = BodypartUtil.retrieveMessageRfc822(msgBean, 0);
		logger.info(StringUtil.prettyPrint(bodyBean2));
		List<BodypartBean> bodyBeans = BodypartUtil.retrieveReportText(msgBean, 0);
		for (BodypartBean bb : bodyBeans) {
			logger.info(StringUtil.prettyPrint(bb));
		}
		
		BodypartBean bodyBean3 = BodypartUtil.retrieveMDNReceipt(msgBean, 0);
		assertNull(bodyBean3);
		
		BodypartBean bodyBean4 = BodypartUtil.retrieveRfc822Headers(msgBean, 0);
		assertNull(bodyBean4);
		
		BodypartBean bodyBean5 = BodypartUtil.retrieveRfc822Text(msgBean, 0);
		assertNotNull(bodyBean5);
		
		List<BodypartBean> bodyBeans2 = BodypartUtil.retrieveAlternatives(msgBean);
		assertFalse(bodyBeans2.isEmpty());
		assertTrue(msgBean.equals(bodyBeans2.get(0)));
	}

	private MessageBean testReadFromFile(String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromFile(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}	
}
