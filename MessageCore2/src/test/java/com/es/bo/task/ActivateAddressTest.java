package com.es.bo.task;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.EmailAddressDao;
import com.es.data.constant.StatusId;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class ActivateAddressTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ActivateAddressTest.class);
	
	@Resource
	private ActivateAddress task;
	@Resource
	private EmailAddressDao emailDao;

	@BeforeClass
	public static void ActivateAddressPrepare() {
	}

	@Test
	public void testActivateAddress() {
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
			mBean.setMsgRefId(Long.parseLong(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$From,$To,testto@test.com");
		task.process(ctx);
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		EmailAddressVo from = emailDao.getByAddress(mBean.getFromAsString());
		assertTrue(StatusId.ACTIVE.getValue().equals(from.getStatusId()));
		assertTrue(0==from.getBounceCount());
		EmailAddressVo to = emailDao.getByAddress(mBean.getToAsString());
		assertTrue(StatusId.ACTIVE.getValue().equals(to.getStatusId()));
		assertTrue(0==to.getBounceCount());
		EmailAddressVo othr = emailDao.getByAddress("testto@test.com");
		assertTrue(StatusId.ACTIVE.getValue().equals(othr.getStatusId()));
		assertTrue(0==othr.getBounceCount());
	}
}
