package jpa.service.task;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("dropMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class DropMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(DropMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * Only to log the message.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		// log the message
		logger.info("Message is droped:" + LF + messageBean);
		return 0;
	}
}
