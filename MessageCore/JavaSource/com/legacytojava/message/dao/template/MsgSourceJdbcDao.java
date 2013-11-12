package com.legacytojava.message.dao.template;

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

import com.legacytojava.message.vo.template.MsgSourceVo;

@Component("msgSourceDao")
public class MsgSourceJdbcDao implements MsgSourceDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}
	
	private static final class MsgSourceMapper implements RowMapper<MsgSourceVo> {
		
		public MsgSourceVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgSourceVo msgSourceVo = new MsgSourceVo();
			
			msgSourceVo.setRowId(rs.getInt("RowId"));
			msgSourceVo.setMsgSourceId(rs.getString("MsgSourceId"));
			msgSourceVo.setDescription(rs.getString("Description"));
			msgSourceVo.setStatusId(rs.getString("StatusId"));
			msgSourceVo.setFromAddrId((Long)rs.getObject("FromAddrId"));
			msgSourceVo.setReplyToAddrId((Long)rs.getObject("ReplyToAddrId"));
			msgSourceVo.setSubjTemplateId(rs.getString("SubjTemplateId"));
			msgSourceVo.setBodyTemplateId(rs.getString("BodyTemplateId"));
			msgSourceVo.setTemplateVariableId(rs.getString("TemplateVariableId"));
			msgSourceVo.setExcludingIdToken(rs.getString("ExcludingIdToken"));
			msgSourceVo.setCarrierCode(rs.getString("CarrierCode"));
			msgSourceVo.setAllowOverride(rs.getString("AllowOverride"));
			msgSourceVo.setSaveMsgStream(rs.getString("SaveMsgStream"));
			msgSourceVo.setArchiveInd(rs.getString("ArchiveInd"));
			msgSourceVo.setPurgeAfter((Integer)rs.getObject("PurgeAfter"));
			msgSourceVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			msgSourceVo.setUpdtUserId(rs.getString("UpdtUserId"));
			msgSourceVo.setOrigUpdtTime(msgSourceVo.getUpdtTime());
			return msgSourceVo;
		}
	}

	public MsgSourceVo getByPrimaryKey(String msgSourceId) {
		String sql = 
			"select * " +
			"from " +
				"MsgSource where msgSourceId=? ";
		
		Object[] parms = new Object[] {msgSourceId};
		
		List<?> list = getJdbcTemplate().query(sql, parms, new MsgSourceMapper());
		if (list.size()>0)
			return (MsgSourceVo)list.get(0);
		else
			return null;
	}
	
	public List<MsgSourceVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			" from " +
				" MsgSource where fromAddrId=? ";
		Object[] parms = new Object[] {Long.valueOf(fromAddrId)};
		List<MsgSourceVo> list = (List<MsgSourceVo>)getJdbcTemplate().query(sql, parms, new MsgSourceMapper());
		return list;
	}
	
	public int update(MsgSourceVo msgSourceVo) {
		msgSourceVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgSourceVo.getMsgSourceId());
		fields.add(msgSourceVo.getDescription());
		fields.add(msgSourceVo.getStatusId());
		fields.add(msgSourceVo.getFromAddrId());
		fields.add(msgSourceVo.getReplyToAddrId());
		fields.add(msgSourceVo.getSubjTemplateId());
		fields.add(msgSourceVo.getBodyTemplateId());
		fields.add(msgSourceVo.getTemplateVariableId());
		fields.add(msgSourceVo.getExcludingIdToken());
		fields.add(msgSourceVo.getCarrierCode());
		fields.add(msgSourceVo.getAllowOverride());
		fields.add(msgSourceVo.getSaveMsgStream());
		fields.add(msgSourceVo.getArchiveInd());
		fields.add(msgSourceVo.getPurgeAfter());
		fields.add(msgSourceVo.getUpdtTime());
		fields.add(msgSourceVo.getUpdtUserId());
		fields.add(msgSourceVo.getRowId());
		
		String sql =
			"update MsgSource set " +
				"MsgSourceId=?, " +
				"Description=?, " +
				"StatusId=?, " +
				"FromAddrId=?, " +
				"ReplyToAddrId=?, " +
				"SubjTemplateId=?, " +
				"BodyTemplateId=?, " +
				"TemplateVariableId=?, " +
				"ExcludingIdToken=?, " +
				"CarrierCode=?, " +
				"AllowOverride=?, " +
				"SaveMsgStream=?, " +
				"ArchiveInd=?, " +
				"PurgeAfter=?, " +
				"UpdtTime=?, " +
				"UpdtUserId=? " +
			"where " +
				" RowId=? ";
		
		if (msgSourceVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgSourceVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		msgSourceVo.setOrigUpdtTime(msgSourceVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String msgSourceId) {
		String sql = 
			"delete from MsgSource where msgSourceId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgSourceId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByFromAddrId(long fromAddrId) {
		String sql = 
			"delete from MsgSource where fromAddrId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(Long.valueOf(fromAddrId));
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgSourceVo msgSourceVo) {
		String sql = 
			"INSERT INTO MsgSource (" +
			"MsgSourceId, " +
			"Description, " +
			"StatusId, " +
			"FromAddrId, " +
			"ReplyToAddrId, " +
			"SubjTemplateId, " +
			"BodyTemplateId, " +
			"TemplateVariableId, " +
			"ExcludingIdToken, " +
			"CarrierCode, " +
			"AllowOverride, " +
			"SaveMsgStream, " +
			"ArchiveInd, " +
			"PurgeAfter, " +
			"UpdtTime, " +
			"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ? ,?, ?, ?, " +
				" ?, ?, ?, ?, ? ,?, ?, ? " +
				")";
		
		msgSourceVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgSourceVo.getMsgSourceId());
		fields.add(msgSourceVo.getDescription());
		fields.add(msgSourceVo.getStatusId());
		fields.add(msgSourceVo.getFromAddrId());
		fields.add(msgSourceVo.getReplyToAddrId());
		fields.add(msgSourceVo.getSubjTemplateId());
		fields.add(msgSourceVo.getBodyTemplateId());
		fields.add(msgSourceVo.getTemplateVariableId());
		fields.add(msgSourceVo.getExcludingIdToken());
		fields.add(msgSourceVo.getCarrierCode());
		fields.add(msgSourceVo.getAllowOverride());
		fields.add(msgSourceVo.getSaveMsgStream());
		fields.add(msgSourceVo.getArchiveInd());
		fields.add(msgSourceVo.getPurgeAfter());
		fields.add(msgSourceVo.getUpdtTime());
		fields.add(msgSourceVo.getUpdtUserId());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		msgSourceVo.setRowId(retrieveRowId());
		msgSourceVo.setOrigUpdtTime(msgSourceVo.getUpdtTime());
		return rowsInserted;
	}

	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
