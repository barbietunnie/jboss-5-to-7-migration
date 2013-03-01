package com.legacytojava.message.bo.mailinglist;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-jmsqueue_rmt-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MailingListBoTest {
	static final Logger logger = Logger.getLogger(MailingListBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MailingListBo mlServiceBo;
	@BeforeClass
	public static void MailingListBoPrepare() {
	}
	@Test
	public void testMailingListBo() throws Exception {
		String mailingListId = "SMPLLST1";
		String testEmailAddr = "test@test.com";
		try {
			String templateId = "SampleNewsletter2";
			int rows = broadcast(templateId);
			assertTrue(rows==1);
			rows = sendMail(templateId);
			assertTrue(rows==1);
			rows = unSubscribe(testEmailAddr, mailingListId);
			assertTrue(rows==1);
			rows = subscribe(testEmailAddr, mailingListId);
			assertTrue(rows==1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	int broadcast(String templateId) throws OutOfServiceException, TemplateNotFoundException,
			DataValidationException {
		int mailsSent = mlServiceBo.broadcast(templateId);
		logger.info("Number of emails sent: " + mailsSent);
		return mailsSent;
	}

	int sendMail(String templateId) throws OutOfServiceException, TemplateNotFoundException,
			DataValidationException {
		String toAddr = "testto@localhost";
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("CustomerName", "List Subscriber");
		int mailsSent = mlServiceBo.send(toAddr, vars, templateId);
		logger.info("Number of emails sent: " + mailsSent);
		return mailsSent;
	}

	int subscribe(String emailAddr, String listId) throws DataValidationException {
		int rows = mlServiceBo.subscribe(emailAddr, listId);
		logger.info("Number of rows added: " + rows);
		return rows;
	}

	int unSubscribe(String emailAddr, String listId) throws DataValidationException {
		int rows = mlServiceBo.unSubscribe(emailAddr, listId);
		logger.info("Number of rows removed: " + rows);
		return rows;
	}
}
