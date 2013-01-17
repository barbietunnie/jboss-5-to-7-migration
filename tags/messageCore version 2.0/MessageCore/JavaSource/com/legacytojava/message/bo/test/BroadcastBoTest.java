package com.legacytojava.message.bo.test;

import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.Rollback;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.bo.template.RenderBo;
import com.legacytojava.message.bo.template.RenderRequest;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.bo.template.RenderVariable;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.exception.DataValidationException;

/*** Please start MailEngine and MailReader before running this test ***/
public class BroadcastBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo broadcastBo;
	@Resource
	private TaskBaseBo mailingListRegExBo;
	@Resource
	private RenderBo util;
	@Test
	@Rollback(false)
	public void broadcast() throws Exception {
		try {
			MessageBean msgBean = buildMessageBeanFromMsgStream();
			MessageBean messageBean = render();
			messageBean.setMsgId(msgBean.getMsgId());
			messageBean.setTo(msgBean.getTo());
			messageBean.setFinalRcpt(msgBean.getFinalRcpt());
			messageBean.setRuleName(RuleNameType.BROADCAST.toString());
			messageBean.setBody("Dear ${CustomerName}:\n" + messageBean.getBody());
			if (isDebugEnabled) {
				logger.debug("MessageBean created:" + LF + messageBean);
			}
			JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory()
					.getBean("jmsProcessor");
			broadcastBo.setJmsProcessor(jmsProcessor);
			broadcastBo.setTaskArguments("SMPLLST2");
			broadcastBo.process(messageBean);
			String regEx = (String) mailingListRegExBo.process(messageBean);
			logger.info("RegEx: " + regEx + ", " + "test-list@domain.com".matches(regEx));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private MessageBean render() throws AddressException, DataValidationException, ParseException {
		RenderRequest req = new RenderRequest(
				"WeekendDeals",
				Constants.DEFAULT_CLIENTID,
				new Timestamp(new java.util.Date().getTime()),
				new HashMap<String, RenderVariable>()
				);
		RenderResponse rsp = util.getRenderedEmail(req);
		assertNotNull(rsp);
		logger.info(rsp);
		return rsp.getMessageBean();
	}

	public void broadcastRuleEngine() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setRuleName(RuleNameType.BROADCAST.toString());
		messageBean.setMailingListId("SMPLLST1");
		messageBean.setBody("Dear ${CustomerName}:\n" + messageBean.getBody());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		// send the bean back to Rule Engine input queue
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"mailReaderOutputJmsTemplate");
		jmsProcessor.setJmsTemplate(jmsTemplate);
		String jmsMsgId = jmsProcessor.writeMsg(messageBean);
		logger.info("Jms Message Id returned: " + jmsMsgId);
	}
}
