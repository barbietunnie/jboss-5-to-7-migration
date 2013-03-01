package com.legacytojava.message.bo;

import java.util.List;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.constant.XHeaderName;
import com.legacytojava.message.exception.DataValidationException;

public class AssignRuleNameBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(AssignRuleNameBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * reset the rule name to the value from TaskArguments field and re-queue
	 * the message to ruleEnginInput queue.
	 * 
	 * @see com.legacytojava.message.bo.TaskBaseBo#process(com.legacytojava.message.bean.MessageBean)
	 * 
	 * @return the JMS Message Id (a String) from the message that were sent to
	 *         the ruleEngineInput queue.
	 */
	public Object process(MessageBean messageBean) throws DataValidationException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList().size() == 0) {
			throw new DataValidationException("Arguments is not valued, can't proceed");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// save original Rule Name to an X-header
		if (messageBean.getRuleName() != null) {
			List<MsgHeader> headers = messageBean.getHeaders();
			MsgHeader newHeader = new MsgHeader();
			newHeader.setName(XHeaderName.XHEADER_ORIG_RULE_NAME);
			newHeader.setValue(messageBean.getRuleName());
			headers.add(newHeader);
		}
		// Assign a new Rule Name
		messageBean.setRuleName(getArgumentList().get(0));
		messageBean.setIsReceived(true);
		if (messageBean.getMsgRefId() == null) {
			// append to the thread
			messageBean.setMsgRefId(messageBean.getMsgId());
		}
		// send the bean back to Rule Engine input queue
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"mailReaderOutputJmsTemplate");
		JmsTemplate errorJmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"unHandledOutputJmsTemplate");
		jmsProcessor.setJmsTemplate(jmsTemplate);
		jmsProcessor.setErrorJmsTemplate(errorJmsTemplate);
		
		String correlid = "AssignRuleNameBo."
				+ (messageBean.getMsgRefId() == null ? "-1" : messageBean.getMsgRefId());
			// set correlation id. To be used in the future.
		String jmsMsgId = jmsProcessor.writeMsg(messageBean, correlid, false);
		/*
		 * jmsMsgId returned from the 1st message could be used as correlation id
		 * by the subsequent messages in the same group
		 */
		if (isDebugEnabled)
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		return jmsMsgId;
	}
}
