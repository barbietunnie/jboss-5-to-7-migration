package com.legacytojava.message.dao.outbox;

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
import com.legacytojava.message.vo.outbox.MsgRenderedVo;

@Component("msgRenderedDao")
public class MsgRenderedJdbcDao extends AbstractDao implements MsgRenderedDao {
	
	public MsgRenderedVo getByPrimaryKey(long renderId) {
		String sql = 
			"select * " +
			"from " +
				"MsgRendered where renderId=? ";
		
		Object[] parms = new Object[] {renderId};
		try {
			MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgRenderedVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"MsgRendered where renderId=(select max(RenderId) from MsgRendered) ";
		
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId) {
		String sql = 
			"select * " +
			" from " +
				" MsgRendered where msgSourceId=? " +
			" order by renderId";
		Object[] parms = new Object[] {msgSourceId};
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return list;
	}
	
	
	public int update(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRenderedVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgRendered", msgRenderedVo);
		if (msgRenderedVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long renderId) {
		String sql = 
			"delete from MsgRendered where renderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRenderedVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgRendered", msgRenderedVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgRenderedVo.setRenderId(getJdbcTemplate().queryForInt(getRowIdSql()));
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsInserted;
	}
}
