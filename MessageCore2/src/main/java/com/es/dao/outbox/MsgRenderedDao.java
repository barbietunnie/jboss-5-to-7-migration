package com.es.dao.outbox;

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
import com.es.vo.outbox.MsgRenderedVo;

@Component("msgRenderedDao")
public class MsgRenderedDao extends AbstractDao {
	
	public MsgRenderedVo getByPrimaryKey(long renderId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rendered where renderId=? ";
		
		Object[] parms = new Object[] {renderId};
		try {
			MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
			return  vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgRenderedVo getFirstRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rendered where renderId=(select min(RenderId) from Msg_Rendered) ";
		
		MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return vo;
	}
	
	public MsgRenderedVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rendered where renderId=(select max(RenderId) from Msg_Rendered) ";
		
		MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return vo;
	}
	
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Rendered where msgSourceId=? " +
			" order by renderId";
		Object[] parms = new Object[] {msgSourceId};
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return list;
	}
	
	
	public int update(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRenderedVo);
		String sql = MetaDataUtil.buildUpdateStatement("Msg_Rendered", msgRenderedVo);
		if (msgRenderedVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long renderId) {
		String sql = 
			"delete from Msg_Rendered where renderId=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRenderedVo);
		String sql = MetaDataUtil.buildInsertStatement("Msg_Rendered", msgRenderedVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgRenderedVo.setRenderId(getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class));
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsInserted;
	}
	
}
