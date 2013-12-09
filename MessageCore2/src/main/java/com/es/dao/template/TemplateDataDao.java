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
import com.es.vo.template.TemplateDataVo;

@Component("templateDataDao")
public class TemplateDataDao extends AbstractDao {
	
	public TemplateDataVo getByPrimaryKey(String templateId, String senderId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"Template_Data where templateId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		if (senderId!=null) {
			sql += " and senderId=? ";
			keys.add(senderId);
		}
		else {
			sql += " and senderId is null ";
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
			TemplateDataVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<TemplateDataVo>(TemplateDataVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public TemplateDataVo getByBestMatch(String templateId, String senderId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"Template_Data where templateId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		if (senderId==null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and (senderId=? or senderId is null) ";
			keys.add(senderId);
		}
		if (startTime==null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		
		sql += " order by senderId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<TemplateDataVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateDataVo>(TemplateDataVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public List<TemplateDataVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" Template_Data where templateId=? " +
			" order by senderId, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<TemplateDataVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateDataVo>(TemplateDataVo.class));
		return list;
	}
	
	public List<TemplateDataVo> getBySenderId(String senderId) {
		String sql = 
			"select * " +
			" from " +
				" Template_Data where senderId=? " +
			" order by templateId, startTime asc ";
		Object[] parms = new Object[] {senderId};
		List<TemplateDataVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateDataVo>(TemplateDataVo.class));
		return list;
	}
	
	public int update(TemplateDataVo templateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(templateVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Template_Data", templateVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String templateId, String senderId, Timestamp startTime) {
		String sql = 
			"delete from Template_Data where templateId=? and senderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		fields.add(senderId);
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
			"delete from Template_Data where templateId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteBySenderId(String senderId) {
		String sql = 
			"delete from Template_Data where senderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(senderId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(TemplateDataVo templateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(templateVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Template_Data", templateVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		templateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
}
