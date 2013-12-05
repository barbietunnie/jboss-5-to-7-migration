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
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.outbox.DeliveryStatusDao;
import com.es.data.constant.StatusId;
import com.es.data.preload.RuleNameEnum;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.outbox.DeliveryStatusVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class AssignRuleNameTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(AssignRuleNameTest.class);
	
	@Resource
	private AssignRuleName task;
	@Resource
	private EmailAddressDao emailDao;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private DeliveryStatusDao dlvrStatDao;

	private EmailIdParser parser = EmailIdParser.getDefaultParser();
	private MessageContext ctx = null;
	private MessageBean mBean = null;
	String bounceAddr = "testbounce@test.com";
	
	@BeforeClass
	public static void AssignRuleNamePrepare() {
	}

	@BeforeTransaction
	public void prepareAssignRuleName() {
		mBean = new MessageBean();
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
		MsgInboxVo randomRec = inboxDao.getRandomRecord();
		String emailIdStr = parser.createEmailId(randomRec.getMsgId());
		mBean.setValue(new Date()+ "Test body message." + LF + LF + emailIdStr + LF);
		mBean.setMailboxUser("testUser");
		String id = parser.parseMsg(mBean.getBody());
		mBean.setMsgRefId(Long.parseLong(id));

		mBean.setFinalRcpt(bounceAddr);
		mBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());

		ctx = new MessageContext(mBean);
		ctx.setTaskArguments(RuleNameEnum.HARD_BOUNCE.getValue());
		try {
			task.process(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAssignRuleName() {
		System.out.println("Verifying Results ##################################################################");
		// verify results
		if (emailDao.getByAddress(bounceAddr)!=null) {
			assertFalse(ctx.getEmailAddrIdList().isEmpty());
			for (Long addrId : ctx.getEmailAddrIdList()) {
				EmailAddressVo addr = emailDao.getByAddrId(addrId);
				assertTrue(StatusId.SUSPENDED.getValue().equals(addr.getStatusId()));
			}
		}
		assertFalse(ctx.getMsgIdList().isEmpty());
		logger.info("MsgId List: " + ctx.getMsgIdList());
		for (Long msgId : ctx.getMsgIdList()) {
			MsgInboxVo minbox = inboxDao.getByPrimaryKey(msgId);
			if (minbox.getMsgId()==mBean.getMsgRefId().longValue()) {
				logger.info("Found message in msg_inbox by msgRefId: " + mBean.getMsgRefId());
				List<DeliveryStatusVo> statusList = dlvrStatDao.getByMsgId(minbox.getMsgId());
				assertFalse(statusList.isEmpty());
				assertTrue(bounceAddr.equals(statusList.get(statusList.size()-1).getFinalRecipient()));
			}
			else {
				String id = parser.parseMsg(mBean.getBody());
				String emailId = parser.parseMsg(minbox.getMsgBody());
				logger.info("Email_Id parsed from body: " + emailId + ", MsgId: " + minbox.getMsgId());
				assertTrue(id.equals(emailId));
			}
		}
	}
}
