package com.legacytojava.message.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.queue.JmsProcessor;

@Component("taskBaseBo")
@Scope(value="prototype")
public abstract class TaskBaseAdaptor implements TaskBaseBo {

	@Autowired
	protected JmsProcessor jmsProcessor;
	protected String taskArguments;
	
	public void setJmsProcessor(JmsProcessor jmsProcessor) {
		this.jmsProcessor = jmsProcessor;
	}
	
	public String getTaskArguments() {
		return taskArguments;
	}
	
	public void setTaskArguments(String taskArguments) {
		this.taskArguments = taskArguments;
	}
	
	protected void setTargetToMailSender() {
		// send MailSender queue
		JmsTemplate jmsTemplate = (JmsTemplate) TaskScheduler.getMailSenderFactory().getBean(
				"mailSenderInputJmsTemplate");
		JmsTemplate errorJmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"unHandledOutputJmsTemplate");
		jmsProcessor.setJmsTemplate(jmsTemplate);
		jmsProcessor.setErrorJmsTemplate(errorJmsTemplate);
	}
	
	protected void setTargetToRuleEngine() {
		// send RuleEngine queue
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"mailReaderOutputJmsTemplate");
		JmsTemplate errorJmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"unHandledOutputJmsTemplate");
		jmsProcessor.setJmsTemplate(jmsTemplate);
		jmsProcessor.setErrorJmsTemplate(errorJmsTemplate);
	}
	
	protected void setTargetToCsrWorkQueue() {
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"ruleEngineOutputJmsTemplate");
		JmsTemplate errorJmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"unHandledOutputJmsTemplate");
		jmsProcessor.setJmsTemplate(jmsTemplate);
		jmsProcessor.setErrorJmsTemplate(errorJmsTemplate);
	}
	
	protected void setTargetToCsrWorkQueue(String templateName) {
		JmsTemplate jmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(templateName);
		JmsTemplate errorJmsTemplate = (JmsTemplate) SpringUtil.getAppContext().getBean(
				"unHandledOutputJmsTemplate");
		jmsProcessor.setJmsTemplate(jmsTemplate);
		jmsProcessor.setErrorJmsTemplate(errorJmsTemplate);
	}
	
	/*
	 * Define convenience methods
	 */
	public List<String> getArgumentList() {
		return getArgumentList(taskArguments);
	}
	
	public static List<String> getArgumentList(String taskArguments) {
		ArrayList<String> list = new ArrayList<String>();
		if (taskArguments != null) {
			StringTokenizer st = new StringTokenizer(taskArguments, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token != null && token.trim().length() > 0)
					list.add(token);
			}
		}
		return list;
	}
}
