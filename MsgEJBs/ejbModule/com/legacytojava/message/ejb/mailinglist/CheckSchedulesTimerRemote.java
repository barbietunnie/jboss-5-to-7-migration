package com.legacytojava.message.ejb.mailinglist;
import javax.ejb.Remote;

@Remote
public interface CheckSchedulesTimerRemote {
	public void startTimers();
	public void stopTimers();
}
