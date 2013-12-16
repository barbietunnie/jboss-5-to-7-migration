package com.legacytojava.message.bo;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;

@Component("dropBo")
@Scope(value="prototype")
@Lazy(true)
public class DropBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(DropBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * Only to log the message.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		// log the message
		logger.info("Message is droped:" + LF + messageBean);
		return Long.valueOf(0);
	}
}
