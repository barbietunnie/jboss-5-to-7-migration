package com.legacytojava.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.inbox.RfcFieldsVo;

@Component("rfcFieldsDao")
public class RfcFieldsJdbcDao extends AbstractDao implements RfcFieldsDao {
	
	public RfcFieldsVo getByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"select * " +
			"from " +
				"RfcFields where msgid=? and rfcType=? ";
		
		Object[] parms = new Object[] {msgId+"",rfcType};
		try {
			RfcFieldsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RfcFieldsVo>(RfcFieldsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RfcFieldsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" RfcFields where msgId=? " +
			" order by rfcType";
		Object[] parms = new Object[] {msgId+""};
		List<RfcFieldsVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RfcFieldsVo>(RfcFieldsVo.class));
		return list;
	}
	
	public int update(RfcFieldsVo rfcFieldsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(rfcFieldsVo);
		String sql = MetaDataUtil.buildUpdateStatement("RfcFields", rfcFieldsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"delete from RfcFields where msgid=? and rfcType=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		fields.add(rfcType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from RfcFields where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RfcFieldsVo rfcFieldsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(rfcFieldsVo);
		String sql = MetaDataUtil.buildInsertStatement("RfcFields", rfcFieldsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
