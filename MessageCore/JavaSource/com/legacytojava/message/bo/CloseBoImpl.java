package com.legacytojava.message.bo;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

public class CloseBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(CloseBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private MsgInboxDao msgInboxDao;

	/**
	 * Close the message by MsgId.
	 * @return a Long representing the msgId closed.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		long msgId = -1L;
		if (messageBean.getMsgId()==null) {
			logger.warn("MessageBean.msgId is null, nothing to close");
			return Long.valueOf(msgId);
		}
		
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getMsgId();
			msgInboxVo.setStatusId(MsgStatusCode.CLOSED);
			int rowsUpdated = msgInboxDao.updateStatusId(msgInboxVo);
			if (isDebugEnabled)
				logger.debug("Rows updated to Closed status: " + rowsUpdated);
		}
		return Long.valueOf(msgId);
	}
	
	public MsgInboxDao getMsgInboxDao() {
		return msgInboxDao;
	}

	public void setMsgInboxDao(MsgInboxDao msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
	}
}
