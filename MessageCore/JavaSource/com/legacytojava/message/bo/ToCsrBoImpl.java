package com.legacytojava.message.bo;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.dao.action.MsgDataTypeDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.action.MsgDataTypeVo;

public class ToCsrBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ToCsrBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private MsgDataTypeDao msgDataTypeDao;

	/**
	 * Forward the message to CSR input queue. The queue name should be stored
	 * in "DataTypeValues" of MsgAction table. Queue name ruleEngineOutput
	 * will be used if the value from the table is not defined or not valid.
	 * 
	 * @return the JMS Message Id (a String) from the message that were sent to
	 *         the output queue.
	 * 
	 * @see com.legacytojava.message.bo.TaskBaseBo#process(com.legacytojava.message.bean.MessageBean)
	 */
	public Object process(MessageBean messageBean) throws DataValidationException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList().size() == 0) {
			logger.warn("Arguments is not valued, use default queue: ruleEngineOutput");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		String templateName = null;
		if (taskArguments != null && taskArguments.trim().length() > 0) {
			if (taskArguments.startsWith("$")) { // variable name
				// retrieve the real queue name (Spring JMS template) from database:
				MsgDataTypeVo vo = msgDataTypeDao.getByTypeValuePair("QUEUE_NAME", taskArguments);
				if (vo != null) {
					if (!StringUtil.isEmpty(vo.getMiscProperties())) {
						templateName = vo.getMiscProperties();
					}
				}
				else {
					throw new DataValidationException("Record not found by: QUEUE_NAME/"
							+ taskArguments);
				}
			}
			else { // should be a JMS template name
				templateName = taskArguments;
			}
		}
		// Configure JmsProcessor to use provided JMS template
		if (templateName != null) {
			if (SpringUtil.getAppContext().getBean(templateName) == null) {
				throw new DataValidationException(templateName + " not found in Spring xmls.");
			}
			else {
				Object obj = SpringUtil.getAppContext().getBean(templateName);
				if (!(obj instanceof JmsTemplate)) {
					throw new DataValidationException(templateName + " is not expected type.");
				}
			}
			setTargetToCsrWorkQueue(templateName);
		}
		else {
			setTargetToCsrWorkQueue(); // use default queue
		}
		String correlid = "ToCsrBo."
				+ (messageBean.getMsgRefId() == null ? "-1" : messageBean.getMsgRefId());
			// set correlation id. To be used in the future.
		String jmsMsgId = jmsProcessor.writeMsg(messageBean, correlid, false);
		/*
		 * jmsMsgId returned from the 1st message could be used as correlation id
		 * by the subsequent messages in the same group
		 */
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		}
		return jmsMsgId;
	}
	
	public MsgDataTypeDao getMsgDataTypeDao() {
		return msgDataTypeDao;
	}

	public void setMsgDataTypeDao(MsgDataTypeDao msgDataTypeDao) {
		this.msgDataTypeDao = msgDataTypeDao;
	}
}
