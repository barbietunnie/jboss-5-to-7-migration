package com.es.bo.task;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.inbox.MsgInboxBo;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageContext;

@Component("saveMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class SaveMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = -5524706653539538026L;
	static final Logger logger = Logger.getLogger(SaveMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgInboxBo msgInboxBo;

	/**
	 * Save the message into the MsgInbox and its satellite tables.
	 * 
	 * @return a Integer value representing the msgId inserted into MsgInbox.
	 */
	public Long process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		long msgId = msgInboxBo.saveMessage(ctx.getMessageBean());
		ctx.getMsgIdList().add(msgId);
		
		return msgId;
	}
	
}
