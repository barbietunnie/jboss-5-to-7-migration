package jpa.test.task;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.StatusId;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.EmailAddress;
import jpa.service.EmailAddressService;
import jpa.service.task.ActivateAddress;

import org.apache.commons.lang3.StringUtils;
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
public class ActivateAddressTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ActivateAddressTest.class);
	
	@Resource
	private ActivateAddress task;
	@Resource
	private EmailAddressService emailService;

	@BeforeClass
	public static void ActivateAddressPrepare() {
	}

	@Test
	public void testActivateAddress() throws Exception {
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
		mBean.setValue(new Date()+ "Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$From,$To,testto@test.com");
		task.process(ctx);
		
		// verify results
		EmailAddress from = emailService.getByAddress(mBean.getFromAsString());
		assertTrue(StatusId.ACTIVE.getValue().equals(from.getStatusId()));
		assertTrue(0==from.getBounceCount());
		EmailAddress to = emailService.getByAddress(mBean.getToAsString());
		assertTrue(StatusId.ACTIVE.getValue().equals(to.getStatusId()));
		assertTrue(0==to.getBounceCount());
		EmailAddress othr = emailService.getByAddress("testto@test.com");
		assertTrue(StatusId.ACTIVE.getValue().equals(othr.getStatusId()));
		assertTrue(0==othr.getBounceCount());
	}
}
