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
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class BounceUpAddressTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(BounceUpAddressTest.class);
	
	@Resource
	private BounceUpAddress task;
	@Resource
	private EmailAddressDao emailDao;

	@BeforeClass
	public static void BounceUpPrepare() {
	}

	@Test
	public void testBounceUpAddress() throws Exception {
		MessageBean mBean = new MessageBean();
		String fromaddr = "testfrom@localhost";
		String toaddr = "testto@localhost";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Long.parseLong(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");

		String alertAddr = "event.alert@localhost";
		int fromCount = getBounceCount(fromaddr);
		int toCount = getBounceCount(toaddr);
		int alertCount = getBounceCount(alertAddr);
		
		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$From,$To," + alertAddr);
		task.process(ctx);
		
		// verify results
		verifyBounceCount(mBean.getFromAsString(), fromCount);
		verifyBounceCount(mBean.getToAsString(), toCount);
		verifyBounceCount(alertAddr, alertCount);
	}
	
	private int getBounceCount(String address) {
		EmailAddressVo addr = emailDao.getByAddress(address);
		if (addr == null) {
			return 0;
		}
		else {
			return addr.getBounceCount();
		}
	}
	
	private void verifyBounceCount(String address, int origCount) {
		EmailAddressVo addr = emailDao.getByAddress(address);
		assertTrue(origCount<addr.getBounceCount());
		if (addr.getBounceCount()>=Constants.BOUNCE_SUSPEND_THRESHOLD) {
			assertTrue(StatusId.SUSPENDED.getValue().equals(addr.getStatusId()));
		}
		else {
			assertTrue(!StatusId.SUSPENDED.getValue().equals(addr.getStatusId()));
		}
	}
}
