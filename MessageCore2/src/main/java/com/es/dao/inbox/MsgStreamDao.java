package com.es.dao.inbox;

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
import com.es.vo.outbox.MsgStreamVo;

@Component("msgStreamDao")
public class MsgStreamDao extends AbstractDao {
	
	public MsgStreamVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Stream where msgid=? ";
		
		Object[] parms = new Object[] {msgId};
		try {
			MsgStreamVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgStreamVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Stream where fromAddrId=? ";
		
		Object[] parms = new Object[] {fromAddrId};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	public MsgStreamVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Stream where msgid = (select max(MsgId) from Msg_Stream) ";
		
		MsgStreamVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return vo;
	}
	
	public int update(MsgStreamVo msgStreamVo) {
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgStreamVo);
		String sql = MetaDataUtil.buildUpdateStatement("Msg_Stream", msgStreamVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from Msg_Stream where msgid=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgStreamVo msgStreamVo) {
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgStreamVo);
		String sql = MetaDataUtil.buildInsertStatement("Msg_Stream", msgStreamVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
