package com.es.bo.task;

import java.io.IOException;

import javax.mail.MessagingException;

import com.es.exception.DataValidationException;
import com.es.msgbean.MessageContext;

public interface AbstractTaskBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageContext ctx) throws DataValidationException,
			MessagingException, IOException;
}
