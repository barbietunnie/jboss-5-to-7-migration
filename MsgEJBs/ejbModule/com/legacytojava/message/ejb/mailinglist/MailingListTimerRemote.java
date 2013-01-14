package com.legacytojava.message.ejb.mailinglist;
import javax.ejb.Remote;

@Remote
public interface MailingListTimerRemote {
	public void scheduleTimerTasks();
	public void stopTimers();
}
