package com.legacytojava.message.ejb.mailinglist;
import javax.ejb.Local;

@Local
public interface MailingListTimerLocal {
	public void scheduleTimerTasks();
	public void stopTimers();
}
