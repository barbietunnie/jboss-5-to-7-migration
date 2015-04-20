package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.es.dao.inbox.MsgAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddrType;
import com.es.data.constant.TableColumnName;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.comm.SenderDataVo;
import com.es.vo.inbox.MsgAddressVo;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class ForwardMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ForwardMessageTest.class);
	
	@Resource
	private ForwardMessage task;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private SenderDataDao senderDao;
	@Resource
	private MsgAddressDao addressDao;

	@BeforeClass
	public static void ForwardPrepare() {
	}

	@Test
	public void testForwardMessage() throws AddressException {
		String fromaddr = "testfrom@localhost";
		String toaddr = "testto@localhost";
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message.");
		mBean.setMailboxUser("testUser");
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);
		String forwardAddr = "twang@localhost";
		mBean.setForward(InternetAddress.parse(forwardAddr));

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$" + EmailAddrType.FORWARD_ADDR.getValue() + ",$" + TableColumnName.SUBSCRIBER_CARE_ADDR.getValue());
		try {
			task.process(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId from MesageContext = " + ctx.getMsgIdList().get(0));
		MsgInboxVo minbox = inboxDao.getByPrimaryKey(ctx.getMsgIdList().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress()));
		List<MsgAddressVo> addrs = addressDao.getByMsgId(minbox.getMsgId());
		assertTrue(addrs.size()>=3);
		SenderDataVo sender = senderDao.getBySenderId(mBean.getSenderId());
		int addrsFound = 0;
		for (MsgAddressVo addr : addrs) {
			if (forwardAddr.equals(addr.getAddrValue())) {
				addrsFound++;
			}
			else if (fromaddr.equals(addr.getAddrValue())) {
				addrsFound++;
			}
			else if (sender.getCustcareEmail().equals(addr.getAddrValue())) {
				addrsFound++;
			}
		}
		assertTrue(addrsFound==3);
		
		assertTrue(minbox.getMsgSubject().equals(mBean.getSubject()));
		assertTrue(minbox.getMsgBody().equals(mBean.getBody()));
	}
}
