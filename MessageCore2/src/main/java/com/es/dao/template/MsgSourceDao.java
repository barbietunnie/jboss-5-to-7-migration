package com.es.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
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
		msgSourceVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgSourceVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Msg_Source", msgSourceVo);
		
		if (msgSourceVo.getOrigUpdtTime() != null) {
			// optimistic locking
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		msgSourceVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgSourceVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Msg_Source", msgSourceVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgSourceVo.setRowId(retrieveRowId());
		msgSourceVo.setOrigUpdtTime(msgSourceVo.getUpdtTime());
		return rowsInserted;
	}
}
