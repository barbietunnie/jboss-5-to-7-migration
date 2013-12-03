package com.es.bo.task;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.data.constant.XHeaderName;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.msgbean.MsgHeader;

@Component("assignRuleName")
@Transactional(propagation=Propagation.REQUIRED)
public class AssignRuleName extends TaskBaseAdaptor {
	private static final long serialVersionUID = 2835514922267686344L;
	static final Logger logger = Logger.getLogger(AssignRuleName.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private TaskSchedulerBo taskBo;
	
	/**
	 * reset the rule name to the value from TaskArguments field and send
	 * the message back to task scheduler. The MessageBean in context will
	 * be assigned a new rule name provided in "TaskArguments".
	 * 
	 * @return zero.
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public Long process(MessageContext ctx) throws DataValidationException,
			MessagingException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList(ctx.getTaskArguments()).size() == 0) {
			throw new DataValidationException("Arguments is not valued, can't proceed");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		// save original Rule Name to an X-header
		if (StringUtils.isNotBlank(messageBean.getRuleName())) {
			List<MsgHeader> headers = messageBean.getHeaders();
			MsgHeader newHeader = new MsgHeader();
			newHeader.setName(XHeaderName.ORIG_RULE_NAME.getValue());
			newHeader.setValue(messageBean.getRuleName());
			headers.add(newHeader);
		}
		// Assign a new Rule Name
		messageBean.setRuleName(getArgumentList(ctx.getTaskArguments()).get(0));
		messageBean.setIsReceived(true);
		if (messageBean.getMsgRefId() == null) {
			// append to the thread
			messageBean.setMsgRefId(messageBean.getMsgId());
		}
		// send the bean back to task scheduler
		taskBo.scheduleTasks(ctx);
		return 0L;
	}
}
