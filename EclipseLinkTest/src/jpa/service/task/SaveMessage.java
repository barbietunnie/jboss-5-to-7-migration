package jpa.service.task;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.message.MessageInbox;
import jpa.service.msgin.MessageInboxBo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("saveMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class SaveMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SaveMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxBo msgInboxBo;

	/**
	 * Save the message into the MsgInbox and its satellite tables.
	 * 
	 * @return a Integer value representing the msgId inserted into MsgInbox.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		MessageInbox msgInbox = msgInboxBo.saveMessage(messageBean);
		
		return msgInbox.getRowId();
	}
	
}
