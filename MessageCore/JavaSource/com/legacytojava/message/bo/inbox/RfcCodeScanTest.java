package com.legacytojava.message.bo.inbox;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.RuleNameType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-bo_jms-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class RfcCodeScanTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(RfcCodeScanTest.class);
	
	public static void RfcCodeScanPrepres() {
	}
	
	@Test
	public void testSoftBounce() throws Exception {
		RfcCodeScan scanner = RfcCodeScan.getInstance();
		
		String bodyText = "test message 421 Service not available, closing transmission channel";
		String ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.SOFT_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 422 Service not available, user mailbox full";
		ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.MAILBOX_FULL.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 450 Requested mail action not taken: mailbox unavailable";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.SOFT_BOUNCE.toString().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 451 Requested action aborted: local error in processing";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.SOFT_BOUNCE.toString().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 452 Requested action not taken: insufficient system storage";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.SOFT_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 455 delivery temporary failure";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.SOFT_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
	}
	@Test
	public void testHardBounce() throws Exception {
		RfcCodeScan scanner = RfcCodeScan.getInstance();
		
		String bodyText = "test message 500 Syntax error, command unrecognized";
		String ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 501 Syntax error in parameters or arguments";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 502 Command not implemented";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 503 Bad sequence of commands";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 504 Command parameter not implemented";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 550 Requested action not taken: mailbox unavailable";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 551 User not local; please try <forward-path>";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 552 Requested mail action aborted: exceeded storage allocation";
		ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.MAILBOX_FULL.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 553 Requested action not taken: mailbox name not allowed";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 554 Transaction failed";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 555 delivery permanent error";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameType.HARD_BOUNCE.toString().equals(ruleName));
		logger.info(LF);
	}
}
