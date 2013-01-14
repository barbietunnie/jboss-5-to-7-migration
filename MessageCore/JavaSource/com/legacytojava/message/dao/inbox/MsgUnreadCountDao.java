package com.legacytojava.message.dao.inbox;

public interface MsgUnreadCountDao {
	public int updateInboxUnreadCount(int delta);
	public int updateSentUnreadCount(int delta);
	public int resetInboxUnreadCount(int inboxUnreadCount);
	public int resetSentUnreadCount(int sentUnreadCount);
	public int resetUnreadCounts(int inboxUnreadCount, int sentUnreadCount);
	public int selectInboxUnreadCount();
	public int selectSentUnreadCount();
}
