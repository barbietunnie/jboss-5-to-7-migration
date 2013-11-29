package com.es.bo.task;

import java.io.IOException;

import javax.mail.MessagingException;

import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;

public interface AbstractTaskBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageBean messageBean) throws DataValidationException,
			MessagingException, IOException;
	
	public String getTaskArguments();

	public void setTaskArguments(String taskArguments);
}
