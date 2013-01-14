package com.legacytojava.timerejb;
import javax.ejb.Remote;

@Remote
public interface MailReaderRemote {
	public void startMailReader(int interval);
	public void stopMailReader();
}
