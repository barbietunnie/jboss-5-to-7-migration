package com.legacytojava.message.dao.inbox;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;

@Component("msgUnreadCountDao")
public class MsgUnreadCountJdbcDao extends AbstractDao implements MsgUnreadCountDao {
	protected static final Logger logger = Logger.getLogger(MsgUnreadCountJdbcDao.class);
	
	public int updateInboxUnreadCount(int delta) {
		String sql = 
			"update MsgUnreadCount set InboxUnreadCount = (InboxUnreadCount + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	public int updateSentUnreadCount(int delta) {
		String sql = 
			"update MsgUnreadCount set SentUnreadCount = (SentUnreadCount + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	public int resetInboxUnreadCount(int inboxUnreadCount) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(inboxUnreadCount);

		String sql = "update MsgUnreadCount set " +
				"InboxUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int resetSentUnreadCount(int sentUnreadCount) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(sentUnreadCount);

		String sql = "update MsgUnreadCount set " +
				"SentUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int resetUnreadCounts(int inboxUnreadCount, int sentUnreadCount) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(inboxUnreadCount);
		fields.add(sentUnreadCount);

		String sql = "update MsgUnreadCount set " +
				"InboxUnreadCount=?," +
				"SentUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int selectInboxUnreadCount() {
		String sql = "select InboxUnreadCount from MsgUnreadCount";
		int unreadCount = getJdbcTemplate().queryForInt(sql);
		return unreadCount;
	}

	public int selectSentUnreadCount() {
		String sql = "select SentUnreadCount from MsgUnreadCount";
		int unreadCount = getJdbcTemplate().queryForInt(sql);
		return unreadCount;
	}
}
