package jpa.test.message;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.mail.MessagingException;

import jpa.message.BodypartBean;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.message.MsgHeader;
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
	public void testBodyPartUtil() throws MessagingException {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = testReadFromFile(fileName);
		assertNotNull(msgBean);

		BodypartBean bodyBean1 = BodypartUtil.retrieveDlvrStatus(msgBean, 0);
		logger.info(StringUtil.prettyPrint(bodyBean1));
		assertTrue("message/delivery-status".equals(bodyBean1.getContentType()));
		String dlvrStatus = new String(bodyBean1.getValue());
		assertTrue(dlvrStatus.indexOf("Reporting-MTA: dns;MELMX.synnex.com.au")>=0);
		assertTrue(dlvrStatus.indexOf("Final-Recipient: rfc822;jackwnn@synnex.com.au")>0);
		assertTrue(dlvrStatus.indexOf("Status: 5.1.1")>0);
		
		BodypartBean bodyBean2 = BodypartUtil.retrieveMessageRfc822(msgBean, 0);
		logger.info(StringUtil.prettyPrint(bodyBean2));
		assertTrue("message/rfc822".equals(bodyBean2.getContentType()));
		assertTrue(bodyBean2.getNodes().size()==1);
		BodypartBean bodyBean2_1 = bodyBean2.getNodes().get(0);
		logger.info(StringUtil.prettyPrint(bodyBean2_1));
		assertTrue(bodyBean2_1.getContentType().startsWith("text/html"));
		String rfc822 = new String(bodyBean2_1.getValue());
		assertTrue(rfc822.indexOf("Dear jackwnn@synnex.com.au")>0);
		assertTrue(rfc822.indexOf("Online Pharmacy Products!")>0);
		int hdrcnt = 0;
		for (MsgHeader hdr : bodyBean2_1.getHeaders()) {
			if ("Return-Path".equals(hdr.getName())) {
				assertTrue("jackwng@gmail.com".equals(hdr.getValue()));
				hdrcnt++;
			}
			else if ("Message-Id".equals(hdr.getName())) {
				assertTrue("<03907644185382.773588432734.799319-7043@cimail571.msn.com>".equals(hdr.getValue()));
				hdrcnt++;
			}
			else if ("To".equals(hdr.getName())) {
				assertTrue("<jackwnn@synnex.com.au>".equals(hdr.getValue()));
				hdrcnt++;
			}
			else if ("Subject".equals(hdr.getName())) {
				assertTrue("May 74% OFF".equals(hdr.getValue()));
				hdrcnt++;
			}
		}
		assertTrue(hdrcnt==4);

		List<BodypartBean> bodyBeans = BodypartUtil.retrieveReportText(msgBean, 0);
		assertTrue(bodyBeans.size()==1);
		BodypartBean bb = bodyBeans.get(0);
		logger.info(StringUtil.prettyPrint(bb));
		String reportText = new String(bb.getValue());
		assertTrue(reportText.indexOf("Delivery to the following recipients failed.")>0);
		assertTrue(reportText.indexOf("jackwnn@synnex.com.au")>0);
		
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

	private MessageBean testReadFromFile(String fileName) throws MessagingException {
		byte[] mailStream = TestUtil.loadFromFile(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
