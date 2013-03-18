package jpa.service.task;

import javax.persistence.NoResultException;

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

@Component("openMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class OpenMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(OpenMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxService msgInboxDao;

	/**
	 * Open the message by MsgId.
	 * @return a Integer representing the msgId opened.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		int msgId = -1;
		if (messageBean.getMsgId()==null) {
			logger.warn("MessageBean.msgId is null, nothing to open");
			return Integer.valueOf(msgId);
		}
		
		try {
			MessageInbox msgInboxVo = msgInboxDao.getByPrimaryKey(messageBean.getMsgId());
			msgId = msgInboxVo.getRowId();
			if (!MsgStatusCode.OPENED.getValue().equals(msgInboxVo.getStatusId())) {
				msgInboxVo.setStatusId(MsgStatusCode.OPENED.getValue());
				msgInboxDao.update(msgInboxVo);
			}
			if (isDebugEnabled)
				logger.debug("Message with Row_Id of (" + msgInboxVo.getRowId() + ") is Opened.");
		}
		catch (NoResultException e) {
			logger.error("Message record not found by row_id (" + messageBean.getMsgId() + ").");
		}
		return Integer.valueOf(msgId);
	}
}
