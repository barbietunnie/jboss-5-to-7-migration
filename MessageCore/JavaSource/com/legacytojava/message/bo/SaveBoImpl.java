package com.legacytojava.message.bo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.inbox.MsgInboxBo;
import com.legacytojava.message.exception.DataValidationException;

@Component("saveBo")
@Scope(value="prototype")
@Lazy(true)
public class SaveBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SaveBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgInboxBo msgInboxBo;

	/**
	 * Save the message into the MsgInbox and its satellite tables.
	 * 
	 * @return a Long value representing the msgId inserted into MsgInbox.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		long  msgId = msgInboxBo.saveMessage(messageBean);
		
		return Long.valueOf(msgId);
	}
}
