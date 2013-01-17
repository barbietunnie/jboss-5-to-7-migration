package com.legacytojava.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.vo.inbox.MsgInboxWebVo;

public class SendMailBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo sendMailBo;
	final int loops = 1; //Integer.MAX_VALUE;
	private static MessageBean messageBean = null;
	private String testSubject = "Test Subject - ";
	@Test
	@Rollback(false)
	public void sendMail() throws Exception {
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		sendMailBo.setJmsProcessor(jmsProcessor);
		for (int i = 0; i < loops; i++) {
			String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
			String user = "user" + suffix;
			if (i % 13 == 1) {
				try {
					Thread.sleep(1 * 1000);
				}
				catch (InterruptedException e) {
					break;
				}
			}
			if (messageBean==null) {
				messageBean = buildMessageBeanFromMsgStream();
			}
			messageBean.getHeaders().clear();
			messageBean.setReplyto(null);
			/* set association for verifyDatabaseRecord() */
			messageBean.setMsgRefId(messageBean.getMsgId());
			messageBean.setSubject(testSubject + suffix);
			messageBean.setBody("Test Body Message - " + suffix);
			messageBean.setFrom(InternetAddress.parse("twang@localhost", false));
			messageBean.setTo(InternetAddress.parse(user, false));
			if (isDebugEnabled) {
				logger.debug("MessageBean created:" + LF + messageBean);
			}
			sendMailBo.process(messageBean);
		}
	}
	@Test
	@Rollback(false)
	public void sendMailVERP() throws Exception {
		if (messageBean==null) {
			messageBean = buildMessageBeanFromMsgStream();
		}
		messageBean.getHeaders().clear();
		messageBean.setReplyto(null);
		/* set association for verifyDatabaseRecord() */
		messageBean.setMsgRefId(messageBean.getMsgId());
		messageBean.setFrom(InternetAddress.parse("testto@localhost"));
		messageBean.setTo(InternetAddress.parse("testto@localhost"));
		// VERP test
		messageBean.setTo(InternetAddress.parse("testto-10.07410251.0-jsmith=test.com@localhost"));
		messageBean.setTo(InternetAddress.parse("testto-testlist-jsmith=test.com@localhost"));
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		sendMailBo.setJmsProcessor(jmsProcessor);

		String body = messageBean.getBody();
		for (int i = 0; i < loops; i++) {
			String nbr = StringUtils.leftPad(i + "", 2, "0");
			messageBean.setSubject(testSubject + nbr);
			messageBean.setBody(i + LF + body);
			//messageBean.setFrom(InternetAddress.parse("test" + nbr + "@localhost"));
			sendMailBo.process(messageBean);
		}
	}
	@Test
	public void waitForMailEngine() {
		// wait for the MailEngine to add a record to MsgInbox
		try {
			Thread.sleep(5 * 1000);
		}
		catch (InterruptedException e) {}
	}
	@Test
	public void verifyDatabaseRecord() {
		List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(messageBean.getMsgId());
		assertTrue(list.size()>0);
		for (MsgInboxWebVo vo : list) {
			assertEquals(RuleNameType.SEND_MAIL.name(), vo.getRuleName());
			assertTrue(vo.getMsgSubject().startsWith(testSubject));
		}
	}
}
