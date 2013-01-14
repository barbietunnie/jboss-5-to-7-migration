package com.legacytojava.message.ejb.mailinglist;
import javax.ejb.Local;

@Local
public interface CheckSchedulesTimerLocal {
	public void startTimers();
	public void stopTimers();
}
