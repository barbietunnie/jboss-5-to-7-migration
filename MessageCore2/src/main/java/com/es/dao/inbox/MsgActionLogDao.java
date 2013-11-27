package com.es.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.inbox.MsgActionLogVo;

@Component("msgActionLogDao")
public class MsgActionLogDao {
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MsgActionLogVo getByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Action_Log where msgId=? ";
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and msgRefId is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msgRefId=? ";
		}
		
		try {
			MsgActionLogVo vo = getJdbcTemplate().queryForObject(sql, fields.toArray(),
					new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgActionLogVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Action_Log where msgId=? " +
			" order by msgRefId ";
		Object[] parms = new Object[] {msgId+""};
		List<MsgActionLogVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
		return list;
	}
	
	public List<MsgActionLogVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Action_Log where leadMsgId=? " +
			" order by addrTime";
		Object[] parms = new Object[] {leadMsgId+""};
		List<MsgActionLogVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
		return list;
	}
	
	public int update(MsgActionLogVo msgActionLogsVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionLogsVo.getLeadMsgId());
		fields.add(msgActionLogsVo.getActionBo());
		fields.add(msgActionLogsVo.getParameters());
		fields.add(msgActionLogsVo.getAddTime());
		fields.add(msgActionLogsVo.getMsgId());
		
		String sql =
			"update Msg_Action_Log set " +
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
			"delete from Msg_Action_Log where msgId=? ";
		
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
			"delete from Msg_Action_Log where msgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByLeadMsgId(long leadMsgId) {
		String sql = 
			"delete from Msg_Action_Log where leadMsgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(leadMsgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgActionLogVo msgActionLogsVo) {
		Timestamp addTime = new Timestamp(new java.util.Date().getTime());
		msgActionLogsVo.setAddTime(addTime);
		
		String sql = 
			"INSERT INTO Msg_Action_Log (" +
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
