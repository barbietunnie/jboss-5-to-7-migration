package com.es.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.template.MsgSourceVo;

@Component("msgSourceDao")
public class MsgSourceDao extends AbstractDao {
	
	public MsgSourceVo getByPrimaryKey(String msgSourceId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Source where msgSourceId=? ";
		
		Object[] parms = new Object[] {msgSourceId};
		try {
			MsgSourceVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgSourceVo>(MsgSourceVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgSourceVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Source where fromAddrId=? ";
		Object[] parms = new Object[] {Long.valueOf(fromAddrId)};
		List<MsgSourceVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgSourceVo>(MsgSourceVo.class));
		return list;
	}
	
	public List<MsgSourceVo> getAll() {
		String sql = 
				"select * " +
				" from " +
					" Msg_Source ";
		List<MsgSourceVo> list = getJdbcTemplate().query(sql,  new Object[] {}, 
				new BeanPropertyRowMapper<MsgSourceVo>(MsgSourceVo.class));
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
		fields.add(msgSourceVo.getTemplateDataId());
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
			"update Msg_Source set " +
				"MsgSourceId=?, " +
				"Description=?, " +
				"StatusId=?, " +
				"FromAddrId=?, " +
				"ReplyToAddrId=?, " +
				"TemplateDataId=?, " +
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
			"delete from Msg_Source where msgSourceId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgSourceId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByFromAddrId(long fromAddrId) {
		String sql = 
			"delete from Msg_Source where fromAddrId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(Long.valueOf(fromAddrId));
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgSourceVo msgSourceVo) {
		String sql = 
			"INSERT INTO Msg_Source (" +
			"MsgSourceId, " +
			"Description, " +
			"StatusId, " +
			"FromAddrId, " +
			"ReplyToAddrId, " +
			"TemplateDataId, " +
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
				" ?, ?, ?, ?, ? ,?, ? " +
				")";
		
		msgSourceVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgSourceVo.getMsgSourceId());
		fields.add(msgSourceVo.getDescription());
		fields.add(msgSourceVo.getStatusId());
		fields.add(msgSourceVo.getFromAddrId());
		fields.add(msgSourceVo.getReplyToAddrId());
		fields.add(msgSourceVo.getTemplateDataId());
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
}
