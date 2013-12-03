package com.es.bo.task;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.exception.DataValidationException;
import com.es.msgbean.MessageContext;

@Component("dropMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class DropMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 8866276380038865588L;
	static final Logger logger = Logger.getLogger(DropMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * Only to log the message.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean() == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		// log the message
		logger.info("Message is droped:" + LF + ctx.getMessageBean());
		return 0;
	}
}
