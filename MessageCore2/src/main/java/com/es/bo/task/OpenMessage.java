package com.es.bo.task;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.MsgStatusCode;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;

@Component("openMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class OpenMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 5058402748410505313L;
	static final Logger logger = Logger.getLogger(OpenMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgInboxDao msgInboxDao;

	/**
	 * Open the message by MsgId.
	 * @return a Integer representing the msgId opened.
	 */
	public Long process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		long msgId = -1;
		if (messageBean.getMsgId()==null) {
			logger.warn("MessageBean.msgId is null, nothing to open");
			return Long.valueOf(msgId);
		}
		
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getMsgId();
			if (!MsgStatusCode.OPENED.getValue().equals(msgInboxVo.getStatusId())) {
				msgInboxVo.setStatusId(MsgStatusCode.OPENED.getValue());
				msgInboxDao.update(msgInboxVo);
			}
			if (isDebugEnabled)
				logger.debug("Message with Row_Id of (" + msgInboxVo.getMsgId() + ") is Opened.");
		}
		else {
			logger.error("Message record not found by row_id (" + messageBean.getMsgId() + ").");
		}
		return Long.valueOf(msgId);
	}
}
