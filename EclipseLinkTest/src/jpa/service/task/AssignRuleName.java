package jpa.service.task;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import jpa.constant.XHeaderName;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBean;
import jpa.message.MsgHeader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("assignRuleName")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class AssignRuleName extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(AssignRuleName.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private TaskSchedulerBo taskBo;
	
	/**
	 * reset the rule name to the value from TaskArguments field and send
	 * the message back to task scheduler.
	 * 
	 * @return the message bean with assigned rule name.
	 * @throws TemplateException 
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public MessageBean process(MessageBean messageBean) throws DataValidationException,
			MessagingException, IOException, TemplateException {
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
		if (StringUtils.isNotBlank(messageBean.getRuleName())) {
			List<MsgHeader> headers = messageBean.getHeaders();
			MsgHeader newHeader = new MsgHeader();
			newHeader.setName(XHeaderName.ORIG_RULE_NAME.getValue());
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
		// send the bean back to task scheduler
		taskBo.scheduleTasks(messageBean);
		return messageBean;
	}
}
