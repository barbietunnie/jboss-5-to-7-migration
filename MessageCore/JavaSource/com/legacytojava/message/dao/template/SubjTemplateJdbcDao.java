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
import com.legacytojava.message.vo.template.SubjTemplateVo;

@Component("subjTemplateDao")
public class SubjTemplateJdbcDao extends AbstractDao implements SubjTemplateDao {

	public SubjTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"SubjTemplate where templateId=? and clientId=? ";
		
		Object[] parms;
		if (startTime!=null) {
			sql += " and startTime<=? ";
			parms = new Object[] {templateId,clientId,startTime};
		}
		else {
			sql += " and startTime is null ";
			parms = new Object[] {templateId,clientId};
		}
		sql += " order by startTime asc ";
		try {
			SubjTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public SubjTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"SubjTemplate where templateId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		if (clientId==null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and (clientId=? or clientId is null) ";
			keys.add(clientId);
		}
		if (startTime!=null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		sql += " order by clientId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<SubjTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public List<SubjTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" SubjTemplate where templateId=? " +
			" order by clientId, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<SubjTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
		return list;
	}
	
	public List<SubjTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" SubjTemplate where clientId=? " +
			" order by templateId, startTime asc ";
		Object[] parms = new Object[] {clientId};
		List<SubjTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
		return list;
	}
	
	public int update(SubjTemplateVo subjTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subjTemplateVo);
		String sql = MetaDataUtil.buildUpdateStatement("SubjTemplate", subjTemplateVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"delete from SubjTemplate where templateId=? and clientId=? ";
		
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
			"delete from SubjTemplate where templateId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from SubjTemplate where clientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(SubjTemplateVo subjTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subjTemplateVo);
		String sql = MetaDataUtil.buildInsertStatement("SubjTemplate", subjTemplateVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		subjTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
