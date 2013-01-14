package com.legacytojava.message.bo.test;

import static org.junit.Assert.*;

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

/*** Please start MailEngine and MailSender before running this test ***/
public class AutoReplyBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo autoReplyBo;
	private final String replyToAddress = "testto@localhost";
	private static Long msgRefId;
	@Test
	public void autoReply() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setMailingListId("SMPLLST1");
		Address[] from = InternetAddress.parse(replyToAddress);
		messageBean.setFrom(from); // redirect to MailReader
		autoReplyBo.setTaskArguments("SubscribeByEmailReplyHtml");
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		autoReplyBo.setJmsProcessor(jmsProcessor);
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an AutoReply email to the
		 * "replyToAddress", and add a record to MsgInbox table.
		 */
		autoReplyBo.process(messageBean);
		msgRefId = messageBean.getMsgId();
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
			assertTrue(vo.getMsgSubject().startsWith("You have subscribed to "));
		}
	}
}
