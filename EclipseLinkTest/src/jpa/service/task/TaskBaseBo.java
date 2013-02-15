package jpa.service.task;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.message.MessageBean;

public interface TaskBaseBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageBean messageBean) throws MessagingException, IOException;
	
	public String getTaskArguments();

	public void setTaskArguments(String taskArguments);
}
