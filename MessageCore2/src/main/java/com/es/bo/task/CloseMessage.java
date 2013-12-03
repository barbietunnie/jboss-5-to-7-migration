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

@Component("closeMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class CloseMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 8626771378364657544L;
	static final Logger logger = Logger.getLogger(CloseMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgInboxDao inboxService;

	/**
	 * Close the message by MsgId.
	 * @return a Integer representing the msgId closed.
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
			logger.warn("MessageBean.msgId is null, nothing to close");
			return Long.valueOf(msgId);
		}
		
		MsgInboxVo msgInboxVo = inboxService.getByPrimaryKey(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getMsgId();
			msgInboxVo.setStatusId(MsgStatusCode.CLOSED.getValue());
			inboxService.update(msgInboxVo);
			if (isDebugEnabled) {
				logger.debug("Message with Msg_Id of (" + msgInboxVo.getMsgId() + ") is Closed.");
			}
		}
		return Long.valueOf(msgId);
	}
	
}
