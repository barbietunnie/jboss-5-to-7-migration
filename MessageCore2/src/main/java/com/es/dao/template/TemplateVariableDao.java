package com.es.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.template.TemplateVariableVo;

@Component("templateVariableDao")
public class TemplateVariableDao extends AbstractDao {
	
	private static final Map<String, List<TemplateVariableVo>> currentVariablesCache = new HashMap<String, List<TemplateVariableVo>>();
	
	public TemplateVariableVo getByPrimaryKey(String templateId, String senderId,
			String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"Template_Variable where templateId=? and variableName=? ";
		
		List<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		keys.add(variableName);
		if (senderId==null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and senderId=? ";
			keys.add(senderId);
		}
		if (startTime==null) {
			sql += " and startTime is null ";
		}
		else {
			sql += " and startTime=? ";
			keys.add(startTime);
		}
		
		Object[] parms = keys.toArray();
		try {
			TemplateVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public TemplateVariableVo getByBestMatch(String templateId, String senderId,
			String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"Template_Variable where templateId=? and variableName=? ";
		
		List<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		keys.add(variableName);
		if (senderId==null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and (senderId=? or senderId is null) ";
			keys.add(senderId);
		}
		if (startTime!=null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		sql += " order by senderId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		if (list.size()>0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<TemplateVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" Template_Variable where variableName=? " +
			" order by templateId, senderId, startTime asc ";
		Object[] parms = new Object[] {variableName};
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		return list;
	}
	
	public List<TemplateVariableVo> getBySenderId(String senderId) {
		String sql = 
			"select * " +
			" from " +
				" Template_Variable where senderId=? " +
			" order by templateId, variableName, startTime ";
		Object[] parms = new Object[] { senderId };
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		return list;
	}
	
	public List<TemplateVariableVo> getCurrentByTemplateId(String templateId, String senderId) {
		if (!currentVariablesCache.containsKey(templateId+"."+senderId)) {
			String sql = 
				"select * " +
				" from Template_Variable as a " +
				" inner join ( " +
				"  select b.templateid, b.senderid, b.variablename, max(b.starttime) as maxtime " +
				"   from Template_Variable b " +
				"   where b.statusid=? and b.starttime<=? " +
				"    and b.templateid=? and b.senderid=? " +
				"   group by b.templateid, b.senderid, b.variablename " +
				" ) as c " +
				"  on a.variablename=c.variablename and a.starttime=c.maxtime " +
				"    and a.templateid=c.templateid and a.senderid=c.senderid " +
				" order by a.variableName asc ";
			Object[] parms = new Object[] { StatusId.ACTIVE.getValue(),
					new Timestamp(new java.util.Date().getTime()), templateId, senderId };
			List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
					new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
			currentVariablesCache.put(templateId+"."+senderId, list);
		}
		
		List<TemplateVariableVo> list = currentVariablesCache.get(templateId+"."+senderId);
		return list;
	}
	
	public List<TemplateVariableVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" Template_Variable where templateId=? " +
			" order by senderId, variableName, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		return list;
	}
	
	public int update(TemplateVariableVo templateVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(templateVariableVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Template_Variable", templateVariableVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted>0) {
			currentVariablesCache.remove(templateVariableVo.getTemplateId() + "."
					+ templateVariableVo.getSenderId());
		}
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String templateId, String senderId, String variableName,
			Timestamp startTime) {
		String sql = 
			"delete from Template_Variable where templateId=? and senderId=? and variableName=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		fields.add(senderId);
		fields.add(variableName);
		if (startTime!=null) {
			sql += " and startTime=? ";
			fields.add(startTime);
		}
		else {
			sql += " and startTime is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0) {
			currentVariablesCache.remove(templateId+"."+senderId);
		}
		return rowsDeleted;
	}
	
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from Template_Variable where variableName=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	public int deleteBySenderId(String senderId) {
		String sql = 
			"delete from Template_Variable where senderId=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(senderId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from Template_Variable where templateId=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	public int insert(TemplateVariableVo templateVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(templateVariableVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Template_Variable", templateVariableVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		templateVariableVo.setRowId(retrieveRowId());
		if (rowsInserted>0) {
			currentVariablesCache.remove(templateVariableVo.getTemplateId() + "."
					+ templateVariableVo.getSenderId());
		}
		return rowsInserted;
	}
	
}
