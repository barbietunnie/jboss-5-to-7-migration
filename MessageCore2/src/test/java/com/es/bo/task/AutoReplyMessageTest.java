package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgHeaderDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.data.preload.EmailTemplateEnum;
import com.es.msg.util.EmailIdParser;
import com.es.msg.util.MsgHeaderVoUtil;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.msgbean.MsgHeader;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class AutoReplyMessageTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(AutoReplyMessageTest.class);
	
	@Resource
	private AutoReplyMessage task;
	@Resource
	private EmailAddressDao emailDao;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private MsgHeaderDao headerDao;

	@BeforeClass
	public static void AutoReplyPrepare() {
	}

	@Test
	public void testAutoReplyMessage() {
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
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		mBean.setSubject("A Exception occured");
		MsgInboxVo randomRec = inboxDao.getRandomRecord();
		String emailIdStr = parser.createEmailId(randomRec.getMsgId());
		//mBean.setValue(new Date()+ " Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setValue(new Date()+ " Test body message." + LF + LF + emailIdStr + LF);
		mBean.setMailboxUser("testUser");
		String id = parser.parseMsg(mBean.getBody());
		mBean.setMsgRefId(Long.parseLong(id));
		mBean.setFinalRcpt("testbounce@test.com");

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(EmailTemplateEnum.SubscribeByEmailReply.name());
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

		assertTrue(minbox.getMsgRefId().equals(mBean.getMsgRefId()));
		assertTrue(minbox.getMsgBody().indexOf(fromaddr)>0);

		String emailIdBody = parser.parseMsg(minbox.getMsgBody());
		assertNotNull(emailIdBody);
		List<MsgHeader> hdrLst = MsgHeaderVoUtil.toMsgHeaderList(headerDao.getByMsgId(minbox.getMsgId()));
		String emailIdHdr = parser.parseHeaders(hdrLst);
		assertTrue(emailIdBody.equals(emailIdHdr));
	}
}
