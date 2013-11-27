package com.es.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.inbox.MsgUnsubCommentVo;

@Component("msgUnsubCommentDao")
public class MsgUnsubCommentDao {
	static final Logger logger = Logger.getLogger(MsgUnsubCommentDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MsgUnsubCommentVo getByPrimaryKey(int rowId){
		String sql = "select * from Msg_Unsub_Comment where RowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			MsgUnsubCommentVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgUnsubCommentVo> getAll() {
		String sql = "select * from Msg_Unsub_Comment " +
		" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public List<MsgUnsubCommentVo> getByMsgId(long msgId) {
		String sql = "select * from Msg_Unsub_Comment " +
			" where MsgId=" + msgId +
			" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public List<MsgUnsubCommentVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from Msg_Unsub_Comment " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public List<MsgUnsubCommentVo> getByListId(String listId) {
		String sql = "select * from Msg_Unsub_Comment " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public int update(MsgUnsubCommentVo msgUnsubCommentsVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(msgUnsubCommentsVo.getMsgId());
		keys.add(msgUnsubCommentsVo.getEmailAddrId());
		keys.add(msgUnsubCommentsVo.getListId());
		keys.add(msgUnsubCommentsVo.getComments());
		keys.add(msgUnsubCommentsVo.getRowId());

		String sql = "update Msg_Unsub_Comment set " +
			"MsgId=?," +
			"EmailAddrId=?," +
			"ListId=?," +
			"Comments=?" +
			" where RowId=?";
		
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from Msg_Unsub_Comment where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = "delete from Msg_Unsub_Comment where MsgId=?";
		Object[] parms = new Object[] {msgId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from Msg_Unsub_Comment where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MsgUnsubCommentVo msgUnsubCommentsVo) {
		msgUnsubCommentsVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				msgUnsubCommentsVo.getMsgId(),
				msgUnsubCommentsVo.getEmailAddrId(),
				msgUnsubCommentsVo.getListId(),
				msgUnsubCommentsVo.getComments(),
				msgUnsubCommentsVo.getAddTime()
			};
		
		String sql = "INSERT INTO Msg_Unsub_Comment (" +
			"MsgId," +
			"EmailAddrId," +
			"ListId," +
			"Comments," +
			"AddTime " +
			") VALUES (" +
				" ?, ?, ?, ?, ? " +
				")";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		msgUnsubCommentsVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
