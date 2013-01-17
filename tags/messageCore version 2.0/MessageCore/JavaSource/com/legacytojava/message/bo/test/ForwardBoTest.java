package com.legacytojava.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.vo.inbox.MsgInboxWebVo;

/*** Please start MailEngine and MailSender before running this test ***/
public class ForwardBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo forwardBo;
	private String forwardAddress = "testto@localhost";
	private static Long msgRefId;
	@Test
	public void forward() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		forwardBo.setTaskArguments("$" + EmailAddressType.FROM_ADDR + "," + forwardAddress);
		msgRefId = messageBean.getMsgId();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		forwardBo.setJmsProcessor(jmsProcessor);
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an Forward email to the
		 * "forwardAddress", and add a record to MsgInbox table.
		 */
		forwardBo.process(messageBean);
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
			assertTrue(vo.getMsgSubject().startsWith("Fwd:"));
		}
	}
}
