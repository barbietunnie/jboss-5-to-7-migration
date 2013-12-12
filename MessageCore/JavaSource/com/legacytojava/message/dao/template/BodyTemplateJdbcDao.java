package com.legacytojava.message.dao.template;

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
import com.legacytojava.message.vo.template.BodyTemplateVo;

@Component("bodyTemplateDao")
public class BodyTemplateJdbcDao extends AbstractDao implements BodyTemplateDao {
	
	public BodyTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"BodyTemplate where templateId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		if (clientId!=null) {
			sql += " and clientId=? ";
			keys.add(clientId);
		}
		else {
			sql += " and clientId is null ";
		}
		if (startTime!=null) {
			sql += " and startTime=? ";
			keys.add(startTime);
		}
		else {
			sql += " startTime is null ";
		}
		
		Object[] parms = keys.toArray();
		try {
			BodyTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public BodyTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"BodyTemplate where templateId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		if (clientId==null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and (clientId=? or clientId is null) ";
			keys.add(clientId);
		}
		if (startTime==null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		
		sql += " order by clientId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public List<BodyTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" BodyTemplate where templateId=? " +
			" order by clientId, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		return list;
	}
	
	public List<BodyTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" BodyTemplate where clientId=? " +
			" order by templateId, startTime asc ";
		Object[] parms = new Object[] {clientId};
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		return list;
	}
	
	public int update(BodyTemplateVo bodyTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(bodyTemplateVo);
		String sql = MetaDataUtil.buildUpdateStatement("BodyTemplate", bodyTemplateVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"delete from BodyTemplate where templateId=? and clientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		fields.add(clientId);
		if (startTime!=null) {
			sql += " and startTime=? ";
			fields.add(startTime);
		}
		else {
			sql += " and startTime is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from BodyTemplate where templateId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from BodyTemplate where clientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(BodyTemplateVo bodyTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(bodyTemplateVo);
		String sql = MetaDataUtil.buildInsertStatement("BodyTemplate", bodyTemplateVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		bodyTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
}
