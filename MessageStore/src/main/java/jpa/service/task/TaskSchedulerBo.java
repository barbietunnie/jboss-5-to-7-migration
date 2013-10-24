package jpa.service.task;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.rule.RuleAction;
import jpa.service.rule.RuleActionService;
import jpa.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A Message Processor. It retrieve process class names or process bean
 * id's from RuleAction table and RuleActionDetail table by the rule name, and
 * process the message by invoking the classes or beans.
 */

@Component("taskSchedulerBo")
@Transactional(propagation=Propagation.REQUIRED)
public class TaskSchedulerBo implements java.io.Serializable {
	private static final long serialVersionUID = 1665720401848374683L;
	static final Logger logger = Logger.getLogger(TaskSchedulerBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator", "\n");

	public void scheduleTasks(MessageContext ctx) throws DataValidationException,
			MessagingException, IOException, TemplateException {
		if (isDebugEnabled)
			logger.debug("Entering scheduleTasks() method. MessageBean:" + LF + ctx.getMessageBean());
		if (ctx.getMessageBean().getRuleName() == null) {
			throw new DataValidationException("RuleName is not valued");
		}
		
		MessageBean msgBean = ctx.getMessageBean();
		RuleActionService ruleActionDao = (RuleActionService) SpringUtil.getAppContext().getBean("ruleActionService");
		List<RuleAction> actions = ruleActionDao.getByBestMatch(msgBean.getRuleName(), null, msgBean.getSenderId());
		if (actions == null || actions.isEmpty()) {
			// actions not defined, save the message.
			String processBeanId = "saveMessage";
			logger.warn("scheduleTasks() - No Actions found for ruleName: " + msgBean.getRuleName()
					+ ", ProcessBeanId [0]: " + processBeanId);
			TaskBaseBo bo = (TaskBaseBo) SpringUtil.getAppContext().getBean(processBeanId);
			bo.process(ctx);
			return;
		}
		for (int i = 0; i < actions.size(); i++) {
			RuleAction ruleActionVo = (RuleAction) actions.get(i);
			TaskBaseBo bo = null;
			String className = ruleActionVo.getRuleActionDetail().getClassName();
			if (StringUtils.isNotBlank(className)) {
				// use process class
				logger.info("scheduleTasks() - ClassName [" + i + "]: "
						+ className);
				try {
					bo = (TaskBaseBo) Class.forName(className).newInstance();
				}
				catch (ClassNotFoundException e) {
					logger.error("ClassNotFoundException caught", e);
					throw new DataValidationException(e.getMessage());
				}
				catch (InstantiationException e) {
					logger.error("InstantiationException caught", e);
					throw new DataValidationException(e.getMessage());
				}
				catch (IllegalAccessException e) {
					logger.error("IllegalAccessException caught", e);
					throw new DataValidationException(e.getMessage());
				}
			}
			else { // use process bean
				String processBeanId = ruleActionVo.getRuleActionDetail().getServiceName();
				logger.info("scheduleTasks() - ProcessBeanId [" + i + "]: " + processBeanId);
				bo = (TaskBaseBo) SpringUtil.getAppContext().getBean(processBeanId);
			}
			/*
			 * retrieve arguments
			 */
			if (StringUtils.isNotBlank(ruleActionVo.getFieldValues())) {
				ctx.setTaskArguments(ruleActionVo.getFieldValues());
			}
			else {
				ctx.setTaskArguments(null);
			}
			// invoke the processor
			bo.process(ctx);
		}
	}

	public static void main(String[] args) {
		TaskSchedulerBo bo = (TaskSchedulerBo) SpringUtil.getAppContext().getBean("taskSchedulerBo");
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
			mBean.setTo(InternetAddress.parse("watched_maibox@domain.com", false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("Delivery Status Notification (Failure)");
		mBean.setFinalRcpt("testbounce@test.com");
		mBean.setValue(new Date()+ "Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		mBean.setMailboxUser("testUser");
		mBean.setRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		SpringUtil.beginTransaction();
		try {
			MessageContext ctx = new MessageContext(mBean);
			bo.scheduleTasks(ctx);
			SpringUtil.commitTransaction();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		System.exit(0);
	}
}