package com.es.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;

@Component("msgUnreadCountDao")
public class MsgUnreadCountDao extends AbstractDao {
	protected static final Logger logger = Logger.getLogger(MsgUnreadCountDao.class);
	
	public int updateInboxUnreadCount(int delta) {
		String sql = 
			"update Msg_Unread_Count set InboxUnreadCount = (InboxUnreadCount + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	public int updateSentUnreadCount(int delta) {
		String sql = 
			"update Msg_Unread_Count set SentUnreadCount = (SentUnreadCount + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	public int resetInboxUnreadCount(int inboxUnreadCount) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(inboxUnreadCount);

		String sql = "update Msg_Unread_Count set " +
				"InboxUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int resetSentUnreadCount(int sentUnreadCount) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(sentUnreadCount);

		String sql = "update Msg_Unread_Count set " +
				"SentUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int resetUnreadCounts(int inboxUnreadCount, int sentUnreadCount) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(inboxUnreadCount);
		fields.add(sentUnreadCount);

		String sql = "update Msg_Unread_Count set " +
				"InboxUnreadCount=?," +
				"SentUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int selectInboxUnreadCount() {
		String sql = "select InboxUnreadCount from Msg_Unread_Count";
		int unreadCount = getJdbcTemplate().queryForObject(sql, Integer.class);
		return unreadCount;
	}

	public int selectSentUnreadCount() {
		String sql = "select SentUnreadCount from Msg_Unread_Count";
		int unreadCount = getJdbcTemplate().queryForObject(sql, Integer.class);
		return unreadCount;
	}
}
