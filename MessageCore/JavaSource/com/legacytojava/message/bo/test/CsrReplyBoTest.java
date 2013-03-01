package com.legacytojava.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.vo.inbox.MsgInboxWebVo;

public class CsrReplyBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo csrReplyBo;
	private final String replyBodyText = "This is Reply from CSR.";
	private final String replyToAddress = "testto@localhost";
	private static Long msgRefId;
	@Test
	public void csrReply() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		Address[] from = InternetAddress.parse(replyToAddress);
		messageBean.setFrom(from); // redirect to MailReader
		if (isDebugEnabled) {
			logger.debug("Original MessageBean:" + LF + messageBean);
		}
		msgRefId = messageBean.getMsgId();
		MessageBean mBean = new MessageBean();
		mBean.setOriginalMail(messageBean);
		mBean.setSubject("Re: " + messageBean.getSubject());
		mBean.setBody(replyBodyText);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + mBean);
		}
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		csrReplyBo.setJmsProcessor(jmsProcessor);
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an CsrReply email to the
		 * "replyToAddress", and add a record to MsgInbox table.
		 */
		csrReplyBo.process(mBean);
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
		// now verify the database record added
		List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(msgRefId);
		assertTrue(list.size()>0);
		for (MsgInboxWebVo vo : list) {
			assertEquals(RuleNameType.SEND_MAIL.name(),vo.getRuleName());
			assertTrue(vo.getMsgSubject().startsWith("Re:"));
		}
	}
}
