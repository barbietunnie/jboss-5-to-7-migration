package com.legacytojava.message.dao.inbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.inbox.MsgUnsubCommentsVo;

@Component("msgUnsubCommentsDao")
public class MsgUnsubCommentsJdbcDao implements MsgUnsubCommentsDao {
	static final Logger logger = Logger.getLogger(MsgUnsubCommentsJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class MsgUnsubCommentsMapper implements RowMapper<MsgUnsubCommentsVo> {
		
		public MsgUnsubCommentsVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgUnsubCommentsVo msgUnsubCommentsVo = new MsgUnsubCommentsVo();
			
			msgUnsubCommentsVo.setRowId(rs.getInt("RowId"));
			msgUnsubCommentsVo.setMsgId(rs.getLong("MsgId"));
			msgUnsubCommentsVo.setEmailAddrId(rs.getLong("EmailAddrId"));
			msgUnsubCommentsVo.setListId(rs.getString("ListId"));
			msgUnsubCommentsVo.setComments(rs.getString("Comments"));
			msgUnsubCommentsVo.setAddTime(rs.getTimestamp("AddTime"));
			
			return msgUnsubCommentsVo;
		}
	}
	
	public MsgUnsubCommentsVo getByPrimaryKey(int rowId){
		String sql = "select * from MsgUnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgUnsubCommentsMapper());
		if (list.size()>0) {
			return (MsgUnsubCommentsVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<MsgUnsubCommentsVo> getAll() {
		String sql = "select * from MsgUnsubComments " +
		" order by RowId";
		List<MsgUnsubCommentsVo> list = (List<MsgUnsubCommentsVo>)getJdbcTemplate().query(sql, new MsgUnsubCommentsMapper());
		return list;
	}
	
	public List<MsgUnsubCommentsVo> getByMsgId(long msgId) {
		String sql = "select * from MsgUnsubComments " +
			" where MsgId=" + msgId +
			" order by RowId";
		List<MsgUnsubCommentsVo> list = (List<MsgUnsubCommentsVo>)getJdbcTemplate().query(sql, new MsgUnsubCommentsMapper());
		return list;
	}
	
	public List<MsgUnsubCommentsVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from MsgUnsubComments " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<MsgUnsubCommentsVo> list = (List<MsgUnsubCommentsVo>)getJdbcTemplate().query(sql, new MsgUnsubCommentsMapper());
		return list;
	}
	
	public List<MsgUnsubCommentsVo> getByListId(String listId) {
		String sql = "select * from MsgUnsubComments " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<MsgUnsubCommentsVo> list = (List<MsgUnsubCommentsVo>)getJdbcTemplate().query(sql, new MsgUnsubCommentsMapper());
		return list;
	}
	
	public int update(MsgUnsubCommentsVo msgUnsubCommentsVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(msgUnsubCommentsVo.getMsgId());
		keys.add(msgUnsubCommentsVo.getEmailAddrId());
		keys.add(msgUnsubCommentsVo.getListId());
		keys.add(msgUnsubCommentsVo.getComments());
		keys.add(msgUnsubCommentsVo.getRowId());

		String sql = "update MsgUnsubComments set " +
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
		String sql = "delete from MsgUnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = "delete from MsgUnsubComments where MsgId=?";
		Object[] parms = new Object[] {msgId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from MsgUnsubComments where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MsgUnsubCommentsVo msgUnsubCommentsVo) {
		msgUnsubCommentsVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				msgUnsubCommentsVo.getMsgId(),
				msgUnsubCommentsVo.getEmailAddrId(),
				msgUnsubCommentsVo.getListId(),
				msgUnsubCommentsVo.getComments(),
				msgUnsubCommentsVo.getAddTime()
			};
		
		String sql = "INSERT INTO MsgUnsubComments (" +
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
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
