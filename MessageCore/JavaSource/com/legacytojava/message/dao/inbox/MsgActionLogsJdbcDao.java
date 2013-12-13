package com.legacytojava.message.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.inbox.MsgActionLogsVo;

@Component("msgActionLogsDao")
public class MsgActionLogsJdbcDao extends AbstractDao implements MsgActionLogsDao {
	
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
		try {
			MsgActionLogsVo vo = getJdbcTemplate().queryForObject(sql, fields.toArray(),
					new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgActionLogsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgActionLogs where msgId=? " +
			" order by msgRefId ";
		Object[] parms = new Object[] {msgId+""};
		List<MsgActionLogsVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
		return list;
	}
	
	public List<MsgActionLogsVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgActionLogs where leadMsgId=? " +
			" order by addrTime";
		Object[] parms = new Object[] {leadMsgId+""};
		List<MsgActionLogsVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
		return list;
	}
	
	public int update(MsgActionLogsVo msgActionLogsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionLogsVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgActionLogs", msgActionLogsVo);
		if (msgActionLogsVo.getMsgRefId() == null) {
			sql += " and MsgRefId is null ";
		}
		else {
			sql += " and MsgRefId=:msgRfcId ";
		}
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		Timestamp addTime = new Timestamp(System.currentTimeMillis());
		msgActionLogsVo.setAddTime(addTime);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionLogsVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgActionLogs", msgActionLogsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
