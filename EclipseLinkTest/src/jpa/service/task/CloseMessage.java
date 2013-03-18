package jpa.service.task;

import jpa.constant.MsgStatusCode;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.message.MessageInbox;
import jpa.service.message.MessageInboxService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("closeMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class CloseMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(CloseMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxService inboxService;

	/**
	 * Close the message by MsgId.
	 * @return a Integer representing the msgId closed.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		int msgId = -1;
		if (messageBean.getMsgId()==null) {
			logger.warn("MessageBean.msgId is null, nothing to close");
			return Integer.valueOf(msgId);
		}
		
		MessageInbox msgInboxVo = inboxService.getByPrimaryKey(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getRowId();
			msgInboxVo.setStatusId(MsgStatusCode.CLOSED.getValue());
			inboxService.update(msgInboxVo);
			if (isDebugEnabled)
				logger.debug("Message with Row_Id of (" + msgInboxVo.getRowId() + ") has been Closed.");
		}
		return Integer.valueOf(msgId);
	}
	
}
