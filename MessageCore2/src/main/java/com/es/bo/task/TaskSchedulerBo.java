package com.es.bo.task;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.SpringUtil;
import com.es.dao.action.RuleActionDao;
import com.es.data.preload.RuleNameEnum;
import com.es.exception.DataValidationException;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.action.RuleActionVo;

/**
 * An Email Message Processor. It retrieve process class names or process bean
 * id's from MsgAction table and MsgActionDetail table by the rule name, and
 * process the message by invoking the classes or beans.
 */
@Component("taskSchedulerBo")
@Transactional(propagation = Propagation.REQUIRED)
public class TaskSchedulerBo {
	static final Logger logger = Logger.getLogger(TaskSchedulerBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator", "\n");

	public void scheduleTasks(MessageContext ctx) throws DataValidationException, 
			MessagingException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering scheduleTasks() method. MessageBean:" + LF
					+ ctx.getMessageBean());
		}
		if (ctx.getMessageBean().getRuleName() == null) {
			throw new DataValidationException("RuleName is not valued");
		}

		MessageBean msgBean = ctx.getMessageBean();
		RuleActionDao ruleActionDao = SpringUtil.getAppContext().getBean(RuleActionDao.class);
		List<RuleActionVo> actions = ruleActionDao.getByBestMatch(
				msgBean.getRuleName(), null, msgBean.getSenderId());
		if (actions == null || actions.isEmpty()) {
			// actions not defined, save the message.
			String processBeanId = "saveMessage";
			logger.warn("scheduleTasks() - No Actions found for ruleName: "
					+ msgBean.getRuleName() + ", ProcessBeanId [0]: "
					+ processBeanId);
			AbstractTaskBo bo = (AbstractTaskBo) SpringUtil.getAppContext().getBean(processBeanId);
			bo.process(ctx);
			return;
		}
		for (int i = 0; i < actions.size(); i++) {
			RuleActionVo ruleActionVo = actions.get(i);
			AbstractTaskBo bo = null;
			String className = ruleActionVo.getProcessClassName();
			if (StringUtils.isNotBlank(className)) {
				// use process class
				logger.info("scheduleTasks() - ClassName [" + i + "]: "
						+ className);
				try {
					bo = (AbstractTaskBo) Class.forName(className).newInstance();
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
			} else { // use process bean
				String processBeanId = ruleActionVo.getProcessBeanId();
				logger.info("scheduleTasks() - ProcessBeanId [" + i + "]: "
						+ processBeanId);
				bo = (AbstractTaskBo) SpringUtil.getAppContext().getBean(processBeanId);
			}
			/*
			 * retrieve arguments
			 */
			if (StringUtils.isNotBlank(ruleActionVo.getDataTypeValues())) {
				ctx.setTaskArguments(ruleActionVo.getDataTypeValues());
			} else {
				ctx.setTaskArguments(null);
			}
			// invoke the processor
			bo.process(ctx);
		}
	}

	public static void main(String[] args) {
		TaskSchedulerBo bo = SpringUtil.getAppContext().getBean(TaskSchedulerBo.class);
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
			mBean.setTo(InternetAddress.parse("watched_maibox@domain.com",
					false));
		} catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("Delivery Status Notification (Failure)");
		mBean.setFinalRcpt("testbounce@test.com");
		mBean.setValue(new Date() + "Test body message." + LF + LF
				+ "System Email Id: 10.2127.0" + LF);
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Long.parseLong(id));
		}
		mBean.setMailboxUser("testUser");
		mBean.setRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		SpringUtil.beginTransaction();
		try {
			MessageContext ctx = new MessageContext(mBean);
			bo.scheduleTasks(ctx);
			SpringUtil.commitTransaction();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		System.exit(0);
	}
}