package com.legacytojava.message.dao.inbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.outbox.MsgStreamVo;

@Component("msgStreamDao")
public class MsgStreamJdbcDao implements MsgStreamDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class MsgStreamMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgStreamVo msgStreamVo = new MsgStreamVo();

			msgStreamVo.setMsgId(rs.getLong("MsgId"));
			msgStreamVo.setFromAddrId((Long)rs.getObject("FromAddrId"));
			msgStreamVo.setToAddrId((Long)rs.getObject("ToAddrId"));
			msgStreamVo.setMsgSubject(rs.getString("MsgSubject"));
			msgStreamVo.setAddTime(rs.getTimestamp("AddTime"));
			msgStreamVo.setMsgStream(rs.getBytes("MsgStream"));
			
			return msgStreamVo;
		}
	}

	public MsgStreamVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"MsgStream where msgid=? ";
		
		Object[] parms = new Object[] {msgId+""};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgStreamMapper());
		if (list.size()>0)
			return (MsgStreamVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgStreamVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			"from " +
				"MsgStream where fromAddrId=? ";
		
		Object[] parms = new Object[] {fromAddrId+""};
		List<MsgStreamVo> list = (List<MsgStreamVo>)getJdbcTemplate().query(sql, parms, new MsgStreamMapper());
		return list;
	}
	
	public MsgStreamVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"MsgStream where msgid = (select max(MsgId) from MsgStream) ";
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, new MsgStreamMapper());
		if (list.size()>0)
			return (MsgStreamVo)list.get(0);
		else
			return null;
	}
	
	public int update(MsgStreamVo msgStreamVo) {
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		fields.add(msgStreamVo.getMsgId()+"");
		
		String sql =
			"update MsgStream set " +
				"FromAddrId=?, " +
				"ToAddrId=?, " +
				"MsgSubject=?, " +
				"AddTime=?, " +
				"MsgStream=? " +
			" where " +
				" msgid=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from MsgStream where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgStreamVo msgStreamVo) {
		String sql = 
			"INSERT INTO MsgStream (" +
				"MsgId, " +
				"FromAddrId, " +
				"ToAddrId, " +
				"MsgSubject, " +
				"AddTime, " +
				"MsgStream " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgStreamVo.getMsgId()+"");
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
