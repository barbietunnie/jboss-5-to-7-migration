package jpa.test.msgin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageNode;
import jpa.message.MsgHeader;
import jpa.service.msgin.MessageParserBo;
import jpa.util.StringUtil;
import jpa.util.TestUtil;

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
	
	@Test
	public void testMessageParser() throws MessagingException {
		String fileName = "BouncedMail_1.txt";
		byte[] mailStream = TestUtil.loadFromFile(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		String ruleName = messageParser.parse(msgBean);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));

		logger.info(StringUtil.prettyPrint(msgBean));
		assertTrue("jackwnn@synnex.com.au".equals(msgBean.getFinalRcpt()));
		assertTrue("failed".equals(msgBean.getDsnAction()));

		assertNotNull(msgBean.getDsnDlvrStat());
		assertTrue(msgBean.getDsnDlvrStat().indexOf("Received-From-MTA: dns;asp-6.reflexion.net")>0);
		assertTrue(msgBean.getDsnDlvrStat().indexOf("Final-Recipient: rfc822;jackwnn@synnex.com.au")>0);
		assertTrue(msgBean.getDsnDlvrStat().indexOf("Status: 5.1.1")>0);
		
		assertNotNull(msgBean.getDsnRfc822());
		assertTrue(msgBean.getDsnRfc822().indexOf("from asp-6.reflexion.net ([205.237.99.181]) by MELMX.synnex.com.au")>=0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Received: by asp-6.reflexion.net")>0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Received: (qmail 22418 invoked from network); 13 May 2008 22:47:48 -0000")>0);
		assertTrue(msgBean.getDsnRfc822().indexOf("From: Viagra ® Official Site <jackwnn@synnex.com.au>")>0);
		assertTrue(msgBean.getDsnRfc822().indexOf("X-Rfx-Unknown-Address: Address <jackwnn@synnex.com.au> is not protected by Reflexion.")>0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Date: 14 May 2008 08:50:31 +1000")>0);
		
		assertTrue("5.1.1".equals(msgBean.getDsnStatus()));
		assertNotNull(msgBean.getDsnText());
		assertTrue(msgBean.getDsnText().indexOf("content=\"text/html; charset=iso-8859-1\"")>0);
		assertTrue(msgBean.getDsnText().indexOf("Dear jackwnn@synnex.com.au")>0);
		assertTrue(msgBean.getDsnText().indexOf("Coupon No. 194")>0);
		
		List<MsgHeader> dlvrd_to = msgBean.getHeader("Delivered-To");
		assertFalse(dlvrd_to.isEmpty());
		assertTrue("support.hotline@jbatch.com".equals(dlvrd_to.get(0).getValue()));
		
		List<MsgHeader> received = msgBean.getHeader("Received");
		assertTrue(received.size()==3);
		
		List<MsgHeader> subject = msgBean.getHeader("Subject");
		assertTrue("Delivery Status Notification (Failure)".equals(subject.get(0).getValue()));
		
		assertTrue("multipart/report".equals(msgBean.getMimeType()));
		
		assertTrue("May 74% OFF".equals(msgBean.getOrigSubject()));
		assertTrue("Delivery Status Notification (Failure)".equals(msgBean.getSubject()));
		assertTrue("support.hotline@jbatch.com".equals(msgBean.getToAsString()));
		
		MessageNode report = msgBean.getReport();
		assertNotNull(report);
		//logger.info("Report: " + StringUtil.prettyPrint(report));
		MessageBean msgBean2 = (MessageBean) report.getBodypartNode();
		assertTrue(msgBean2.getBody().indexOf("Delivery to the following recipients failed.")>0);
		assertTrue(msgBean2.getBody().indexOf("jackwnn@synnex.com.au")>0);
		assertTrue(msgBean2.getBody().indexOf("Content-Type: message/delivery-status")>0);
		assertTrue(msgBean2.getBody().indexOf("Reporting-MTA: dns;MELMX.synnex.com.au")>0);
		assertTrue(msgBean2.getBody().indexOf("Received-From-MTA: dns;asp-6.reflexion.net")>0);
		assertTrue(msgBean2.getBody().indexOf("Final-Recipient: rfc822;jackwnn@synnex.com.au")>0);
		assertTrue(msgBean2.getBody().indexOf("Content-Type: message/rfc822")>0);
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
