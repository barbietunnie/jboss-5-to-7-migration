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

import com.legacytojava.message.vo.inbox.MsgActionLogsVo;

@Component("msgActionLogsDao")
public class MsgActionLogsJdbcDao implements MsgActionLogsDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class MsgActionLogsMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgActionLogsVo msgActionLogsVo = new MsgActionLogsVo();
			
			msgActionLogsVo.setMsgId(rs.getLong("MsgId"));
			msgActionLogsVo.setMsgRefId((Long)rs.getObject("MsgRefId"));
			msgActionLogsVo.setLeadMsgId(rs.getLong("LeadMsgId"));
			msgActionLogsVo.setActionBo(rs.getString("ActionBo"));
			msgActionLogsVo.setParameters(rs.getString("Parameters"));
			msgActionLogsVo.setAddTime(rs.getTimestamp("AddTime"));
			
			return msgActionLogsVo;
		}
	}

	public MsgActionLogsVo getByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"select * " +
			"from " +
				"MsgActionLogs where msgId=? ";
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and msgRefId is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msgRefId=? ";
		}
		
		List<?> list = (List<?>) getJdbcTemplate().query(sql, fields.toArray(),
				new MsgActionLogsMapper());
		if (list.size() > 0)
			return (MsgActionLogsVo) list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgActionLogsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgActionLogs where msgId=? " +
			" order by msgRefId ";
		Object[] parms = new Object[] {msgId+""};
		List<MsgActionLogsVo> list =  (List<MsgActionLogsVo>)getJdbcTemplate().query(sql, parms, new MsgActionLogsMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgActionLogsVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgActionLogs where leadMsgId=? " +
			" order by addrTime";
		Object[] parms = new Object[] {leadMsgId+""};
		List<MsgActionLogsVo> list =  (List<MsgActionLogsVo>)getJdbcTemplate().query(sql, parms, new MsgActionLogsMapper());
		return list;
	}
	
	public int update(MsgActionLogsVo msgActionLogsVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionLogsVo.getLeadMsgId());
		fields.add(msgActionLogsVo.getActionBo());
		fields.add(msgActionLogsVo.getParameters());
		fields.add(msgActionLogsVo.getAddTime());
		fields.add(msgActionLogsVo.getMsgId());
		
		String sql =
			"update MsgActionLogs set " +
				"LeadMsgId=?, " +
				"ActionBo=?, " +
				"Parameters=?, " +
				"AddTime=? " +
			" where " +
				" MsgId=? ";
		if (msgActionLogsVo.getMsgRefId() == null) {
			sql += "and MsgRefId is null ";
		}
		else {
			fields.add(msgActionLogsVo.getMsgRefId());
			sql += "and MsgRefId=? ";
		}
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"delete from MsgActionLogs where msgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and MsgRefId is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msgRefId=? ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from MsgActionLogs where msgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByLeadMsgId(long leadMsgId) {
		String sql = 
			"delete from MsgActionLogs where leadMsgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(leadMsgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgActionLogsVo msgActionLogsVo) {
		Timestamp addTime = new Timestamp(new java.util.Date().getTime());
		msgActionLogsVo.setAddTime(addTime);
		
		String sql = 
			"INSERT INTO MsgActionLogs (" +
			"MsgId, " +
			"MsgRefId, " +
			"LeadMsgId, " +
			"ActionBo, " +
			"Parameters, " +
			"AddTime " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionLogsVo.getMsgId());
		fields.add(msgActionLogsVo.getMsgRefId());
		fields.add(msgActionLogsVo.getLeadMsgId());
		fields.add(msgActionLogsVo.getActionBo());
		fields.add(msgActionLogsVo.getParameters());
		fields.add(msgActionLogsVo.getAddTime());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
