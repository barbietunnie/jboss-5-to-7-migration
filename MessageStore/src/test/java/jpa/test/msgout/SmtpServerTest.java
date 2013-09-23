package jpa.test.msgout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.MailServerType;
import jpa.data.preload.SmtpServerEnum;
import jpa.model.SmtpServer;

import org.apache.commons.beanutils.BeanUtils;
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
public class SmtpServerTest {

	@BeforeClass
	public static void SmtpServerPrepare() {
	}

	@Autowired
	jpa.service.msgout.SmtpServerService service;

	@Test
	public void smtpServerService() {
		SmtpServer mc1 = null;
		// test insert
		SmtpServerEnum mc = SmtpServerEnum.SUPPORT;
		try {
			mc1 = service.getByServerName(mc.getServerName());
		}
		catch (NoResultException e) {
			mc1 = new SmtpServer();
			mc1.setSmtpHostName(mc.getSmtpHost());
			mc1.setSmtpPortNumber(mc.getSmtpPort());
			mc1.setServerName(mc.getServerName());
			mc1.setDescription(mc.getDescription());
			mc1.setUseSsl(mc.isUseSsl());
			mc1.setUserId(mc.getUserId());
			mc1.setUserPswd(mc.getUserPswd());
			mc1.setPersistence(mc.isPersistence());
			mc1.setStatusId(mc.getStatus().getValue());
			mc1.setServerType(mc.getServerType().getValue());
			mc1.setNumberOfThreads(mc.getNumberOfThreads());
			mc1.setMaximumRetries(mc.getMaximumRetries());
			mc1.setRetryFrequence(mc.getRetryFreq());
			mc1.setMinimumWait(mc.getMinimumWait());
			mc1.setAlertAfter(mc.getAlertAfter());
			mc1.setAlertLevel(mc.getAlertLevel());
			mc1.setMessageCount(mc.getMessageCount());
			service.insert(mc1);
		}
		
		List<SmtpServer> list = service.getAll(true, null);
		assertFalse(list.isEmpty());
		
		SmtpServer tkn0 = service.getByServerName(list.get(0).getServerName());
		assertNotNull(tkn0);
		
		tkn0 = service.getByRowId(list.get(0).getRowId());
		assertNotNull(tkn0);
		
		assertTrue(1<=service.getByServerType(MailServerType.SMTP, true).size());
		
		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		SmtpServer tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		SmtpServer tkn2 = new SmtpServer();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setServerName(tkn1.getServerName()+"_v2");
		service.insert(tkn2);
		
		SmtpServer tkn3 = service.getByServerName(tkn2.getServerName());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with NoResultException
		service.delete(tkn3);
		try {
			service.getByServerName(tkn2.getServerName());
			fail();
		}
		catch (NoResultException e) {
		}
		
		assertTrue(1==service.deleteByServerName(tkn1.getServerName()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
