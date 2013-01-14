package com.legacytojava.message.bo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.dao.action.MsgActionDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.action.MsgActionVo;

/**
 * An Email Message Processor. It retrieve process class names or process bean
 * id's from MsgAction table and MsgActionDetail table by the rule name, and
 * process the message by invoking the classes or beans.
 */
public class TaskScheduler {
	static final Logger logger = Logger.getLogger(TaskScheduler.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator", "\n");
	private final AbstractApplicationContext factory;

	private static Hashtable<String, String> mailSenderJndi = null;

	private static AbstractApplicationContext mailSenderFactory = null;
	public TaskScheduler(AbstractApplicationContext factory) {
		this.factory = factory;
	}

	public void scheduleTasks(MessageBean msgBean) throws DataValidationException,
			MessagingException, JMSException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering scheduleTasks() method. MessageBean:" + LF + msgBean);
		if (msgBean.getRuleName() == null) {
			throw new DataValidationException("RuleName is not valued");
		}
		
		MsgActionDao msgActionDao = (MsgActionDao) SpringUtil.getBean(factory, "msgActionDao");
		List<MsgActionVo> actions = msgActionDao.getByBestMatch(msgBean.getRuleName(), null,
				msgBean.getClientId());
		if (actions == null || actions.isEmpty()) {
			// actions not defined, save the message.
			String processBeanId = "saveBo";
			logger.warn("scheduleTasks() - No Actions found for ruleName: " + msgBean.getRuleName()
					+ ", ProcessBeanId [0]: " + processBeanId);
			TaskBaseBo bo = (TaskBaseBo) SpringUtil.getBean(factory, processBeanId);
			bo.process(msgBean);
			return;
		}
		for (int i = 0; i < actions.size(); i++) {
			MsgActionVo msgActionVo = (MsgActionVo) actions.get(i);
			TaskBaseBo bo = null;
			String processClassName = msgActionVo.getProcessClassName();
			if (processClassName != null && processClassName.trim().length() > 0) {
				// use process class
				logger.info("scheduleTasks() - ProcessClassName [" + i + "]: "
						+ processClassName);
				try {
					bo = (TaskBaseBo) Class.forName(processClassName).newInstance();
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
				String processBeanId = msgActionVo.getProcessBeanId();
				logger.info("scheduleTasks() - ProcessBeanId [" + i + "]: " + processBeanId);
				bo = (TaskBaseBo) SpringUtil.getBean(factory, processBeanId);
			}
			/*
			 * retrieve arguments
			 */
			if (msgActionVo.getDataTypeValues() != null) {
				bo.setTaskArguments(msgActionVo.getDataTypeValues());
			}
			else {
				bo.setTaskArguments(null);
			}
			// jmsProcessor's JNDI must point to the location where MailSenderEar is
			// deployed.
			JmsProcessor jmsProcessor = (JmsProcessor) getMailSenderFactory().getBean(
					"jmsProcessor");
			bo.setJmsProcessor(jmsProcessor);
			// invoke the processor
			bo.process(msgBean);
		}
	}

	private synchronized static Map<String, String> getMailSenderJndi() {
		if (mailSenderJndi != null) {
			return Collections.unmodifiableMap(mailSenderJndi);
		}
		mailSenderJndi = new Hashtable<String, String>();
		Properties props = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream("jndi.properties");
		try {
			props.load(is);
			Set<?> set = props.keySet();
			for (Iterator<?> it=set.iterator(); it.hasNext();) {
				String key = (String) it.next();
				mailSenderJndi.put(key, props.getProperty(key));
			}
			//mailSenderJndi.putAll(props);
			return Collections.unmodifiableMap(mailSenderJndi);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static AbstractApplicationContext getMailSenderFactory() {
		if (mailSenderFactory == null) {
			if (SpringUtil.isRunningInJBoss()) {
				mailSenderFactory = new ClassPathXmlApplicationContext("spring-taskscheduler-jee.xml");
			}
			else {
				mailSenderFactory = new ClassPathXmlApplicationContext("spring-taskscheduler-jms.xml");
			}
		}
		return mailSenderFactory;
	}

	public static void main(String[] args) {
		getMailSenderFactory();
		System.out.println(getMailSenderJndi());

		System.exit(0);
	}
}