package com.legacytojava.timerejb;
import javax.ejb.Local;

@Local
public interface MailReaderLocal {
	public void startMailReader(int interval);
	public void stopMailReader();
}
