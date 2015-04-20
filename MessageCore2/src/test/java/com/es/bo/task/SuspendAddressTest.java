package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

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

import com.es.dao.address.EmailAddressDao;
import com.es.data.constant.EmailAddrType;
import com.es.data.constant.StatusId;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SuspendAddressTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SuspendAddressTest.class);
	
	@Resource
	private SuspendAddress task;
	@Resource
	private EmailAddressDao emailService;

	@BeforeClass
	public static void SuspendAddressPrepare() {
	}

	@Test
	public void testSuspendAddress() {
		MessageBean mBean = new MessageBean();
		String fromaddr = "event.alert@localhost";
		String toaddr = "watched_maibox@domain.com";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");
		String finalRcptAddr = "testbounce@test.com";
		mBean.setFinalRcpt(finalRcptAddr);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$" + EmailAddrType.FINAL_RCPT_ADDR.getValue() +",$" + EmailAddrType.FROM_ADDR.getValue());
		task.process(ctx);
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getEmailAddrIdList().isEmpty());
		logger.info("EmailAddrIds from MesageContext = " + ctx.getEmailAddrIdList());
		for (Long addrId : ctx.getEmailAddrIdList()) {
			EmailAddressVo addrvo = emailService.getByAddrId(addrId);
			assertTrue(StatusId.SUSPENDED.getValue().equals(addrvo.getStatusId()));
			assertTrue(addrvo.getEmailAddr().equals(finalRcptAddr) || addrvo.getEmailAddr().equals(fromaddr));
		}
	}
}
