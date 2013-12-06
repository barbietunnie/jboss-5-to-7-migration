package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.es.bo.sender.MessageBeanBo;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.Constants;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class CsrReplyMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(CsrReplyMessageTest.class);
	
	@Resource
	private CsrReplyMessage task;
	@Resource
	private EmailAddressDao emailDao;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private MessageBeanBo msgBeanBo;

	@BeforeClass
	public static void CsrReplyPrepare() {
	}

	@Test
	public void testCsrReplyMessage() throws AddressException {
		String fromaddr = "testfrom@localhost";
		String toaddr = "support@localhost";
		MessageBean origMsg = new MessageBean();
		try {
			origMsg.setFrom(InternetAddress.parse(fromaddr, false));
			origMsg.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		origMsg.setSubject("A Exception occured");
		origMsg.setValue(new Date()+ " Test body message.");
		origMsg.setMailboxUser("testUser");
		// get and set MsgId
		MsgInboxVo inbox = inboxDao.getRandomRecord();
		origMsg.setMsgId(inbox.getMsgId());

		MessageBean mBean = new MessageBean();
		mBean.setOriginalMail(origMsg);
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);
		mBean.setFrom(InternetAddress.parse(toaddr, false));
		mBean.setSubject("Re: " + origMsg.getSubject());
		String replyBody = "test CSR reply message.";
		mBean.setBody(replyBody);

		MessageContext ctx = new MessageContext(mBean);
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
		assertTrue(toaddr.equals(minbox.getFromAddress()));
		assertTrue(fromaddr.equals(minbox.getToAddress()));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		System.out.println("MsgInbox Body: >>>>>" + LF + minbox.getMsgBody() + LF);
		System.out.println("MsgBean  Body: >>>>>" + LF + mBean.getBody() + LF);
		assertTrue(minbox.getMsgBody().indexOf(replyBody)>=0);
		assertTrue(minbox.getMsgBody().indexOf(origMsg.getBody())>0);
		assertTrue(minbox.getMsgBody().indexOf(origMsg.getSubject())>0);
		assertTrue(StringUtils.contains(minbox.getMsgBody(), ("--- " + fromaddr + " wrote:")));
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id_bean = parser.parseMsg(mBean.getBody());
		String id_ibox = parser.parseMsg(minbox.getMsgBody());
		assertTrue(id_bean.equals(id_ibox));
		assertTrue(id_bean.equals(minbox.getMsgId()+""));
	}
}
