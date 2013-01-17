package com.legacytojava.message.bo.rule;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.bean.BodypartBean;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageNode;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.XHeaderName;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-bo_jms-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class RuleMatchTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(RuleMatchTest.class);
	
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	@Resource
	private RuleLoader loader;
	@BeforeClass
	public static void RuleMatchPrepare() {
	}
	@Test
	public void testRuleMatch() throws Exception {
		RuleMatcher matcher = new RuleMatcher();
		loader.listRuleNames();
		runTest(loader, matcher);
	}
	
	private void runTest(RuleLoader loader, RuleMatcher matcher) throws AddressException {
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
			mBean.setTo(InternetAddress.parse("watched_maibox@domain.com", false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");
		
		// test #1 - null
		String ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertNull(ruleName);

		// test #2 - PreRule: MARKETING MAIL
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("noreply");
		mBean.setReturnPath("marketing@fake.net");
		ruleName = matcher.match(mBean, loader.getPreRuleSet(), loader.getSubRuleSet());
		mBean.setReturnPath(null); // reset
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("Unattended_Mailbox", ruleName);
		
		// test #3.1 - AUTO REPLY
		mBean.setSubject("A Exception occured - out of the office");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.AUTO_REPLY.toString(), ruleName);

		// test #3.2 - Out Of Office
		mBean.setSubject("Out of the office auto reply");
		mBean.setValue(new Date()+ "Test body message. will return.");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("OutOfOffice_AutoReply", ruleName);
		
		// test #4.1 - HARD BOUNCE (mailer-daemon)
		mBean.setMailboxUser("testUser");
		mBean.setFrom(InternetAddress.parse("mailer-daemon@localhost", false));
		mBean.setSubject("Delivery Status Notification (Failure)");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.HARD_BOUNCE.toString(), ruleName);
		
		// test #4.2 - MAILBOX FULL (postmaster)
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message. mailbox was full");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		//assertEquals("MailboxFull_Body_Match", ruleName);
		assertEquals(RuleNameType.MAILBOX_FULL.toString(), ruleName);
		
		// test #4.3 - HARD BOUNCE (postmaster)
		mBean.setSubject("Returned Mail: User unknown");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.HARD_BOUNCE.toString(), ruleName);
		
		// test #5 - VIRUS BLOCK
		mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
		mBean.setValue(new Date()+ "Virus abc was found.");
		mBean.setSubject("A Exception occured. norton antivirus detected");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.VIRUS_BLOCK.toString(), ruleName);
		
		// test #6 - SPAM BLOCK
		mBean.setSubject("A Exception occured.");
		mBean.setValue(new Date()+ "Test body message. earthlink spamblocker.");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.SPAM_BLOCK.toString(), ruleName);
		
		// test #7 - CHALLENGE RESPONSE
		mBean.setSubject("Re: A Exception occured.");
		MsgHeader header = new MsgHeader();
		header.setName(XHeaderName.RETURN_PATH);
		header.setValue("<spamblocker-challenge@bounce.earthlink.net>");
		mBean.getHeaders().add(header);
		String body = "I apologize for this automatic reply to your email." + LF + LF;
		body += "To control spam, I now allow incoming messages only from senders I have approved beforehand." + LF + LF;
		body += "If you could like to be added to my list of approved senders, please fill out the short request form (see link" + LF;
		body += "below). Once I approve you, I will receive your original message in my inbox. You do not need to resend your" + LF;
		body += "message. I apologize for this one-time inconvenience." + LF + LF;
		mBean.setValue(body);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### ruleName: "+ruleName+LF);
		assertEquals(RuleNameType.CHALLENGE_RESPONSE.toString(), ruleName);
		
		// test #8 - xheader_spam_score
		mBean.setSubject("Test X-Header rule");
		mBean.setValue(new Date()+ "Test body message.");
		header = new MsgHeader();
		header.setName("X_Spam_Score");
		header.setValue("110");
		mBean.getHeaders().add(header);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("XHeader_SpamScore", ruleName);
		
		// test #9.1 - Post Rule / Sub-Rule (post_rule -> mailbox_address)
		mBean.getHeaders().clear();
		mBean.setSubject("Test Post Rule");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setRuleName(RuleNameType.HARD_BOUNCE.toString());
		ruleName = matcher.match(mBean, loader.getPostRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("HardBouce_WatchedMailbox", ruleName);
		
		// test #9.2 - Post Rule / Sub-Rule (post_rule -> no recipient found)
		mBean.getHeaders().clear();
		mBean.setSubject("Test Post Rule No recipient found");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setTo(InternetAddress.parse("support@localhost"));
		mBean.setRuleName(RuleNameType.HARD_BOUNCE.toString());
		ruleName = matcher.match(mBean, loader.getPostRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("HardBounce_NoFinalRcpt", ruleName);
		
		// test #10 - Attachment File
		mBean.setRuleName(null);
		mBean.setSubject("your account");
		mBean.setValue(new Date()+ "Test body message.");
		MessageNode node = new MessageNode(new BodypartBean(), 1);
		BodypartBean aNode = node.getBodypartNode();
		aNode.setContentType("plain/text; name=test.bat");
		aNode.setValue("test attachment content");
		List<MessageNode> nodes = new ArrayList<MessageNode>();
		nodes.add(node);
		mBean.setAttachments(nodes);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("Executable_Attachment", ruleName);
		
		// test #11 - Subscribe
		mBean.setRuleName(null);
		//mBean.setTo(InternetAddress.parse("demolist1@localhost"));
		mBean.setTo(InternetAddress.parse("demolist1@espheredemo.com"));
		mBean.setSubject("subscribe  ");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.SUBSCRIBE.toString(), ruleName);
		
		// test #12 - UnSubscribe
		mBean.setRuleName(null);
		//mBean.setTo(InternetAddress.parse("demolist1@localhost"));
		mBean.setTo(InternetAddress.parse("demolist1@espheredemo.com"));
		mBean.setSubject("unsubscribe");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameType.UNSUBSCRIBE.toString(), ruleName);
		
		// test #13 - Contact Us
		mBean.setRuleName(null);
		mBean.setMailboxUser("support");
		mBean.setTo(InternetAddress.parse("support@localhost"));
		mBean.setSubject("Inquiry About: Technocal Question");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("Contact_Us", ruleName);
		
		// test #14
		mBean.setSubject("Test Subject");
		mBean.setAttachments(null);
	}
}
