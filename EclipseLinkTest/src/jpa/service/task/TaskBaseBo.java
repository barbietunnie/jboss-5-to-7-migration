package jpa.service.task;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBean;

public interface TaskBaseBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageBean messageBean) throws DataValidationException, 
			MessagingException, IOException, TemplateException;
	
	public String getTaskArguments();

	public void setTaskArguments(String taskArguments);
}
