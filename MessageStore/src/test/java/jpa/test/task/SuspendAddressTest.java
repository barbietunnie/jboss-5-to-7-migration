package jpa.test.task;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.EmailAddrType;
import jpa.constant.StatusId;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.service.EmailAddressService;
import jpa.service.task.SuspendAddress;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SuspendAddressTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SuspendAddressTest.class);
	
	@Resource
	private SuspendAddress task;
	@Resource
	private EmailAddressService emailService;

	@BeforeClass
	public static void SuspendAddressPrepare() {
	}

	@Test
	public void testSuspendAddress() throws Exception {
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
		
		// verify results
		EmailAddress from = emailService.getByAddress(mBean.getFromAsString());
		assertTrue(StatusId.SUSPENDED.getValue().equals(from.getStatusId()));
		assertTrue(0<=from.getBounceCount());
		try {
			EmailAddress othr = emailService.getByAddress(finalRcptAddr);
			assertTrue(StatusId.SUSPENDED.getValue().equals(othr.getStatusId()));
			assertTrue(0<=othr.getBounceCount());
		}
		catch (NoResultException e) {}
	}
}
