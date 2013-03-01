package com.legacytojava.message.dao.template;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.template.BodyTemplateVo;

@Component("bodyTemplateDao")
public class BodyTemplateJdbcDao implements BodyTemplateDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}
	
	private static final class BodyTemplateMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			BodyTemplateVo bodyTemplateVo = new BodyTemplateVo();
			
			
			bodyTemplateVo.setRowId(rs.getInt("RowId"));
			bodyTemplateVo.setTemplateId(rs.getString("TemplateId"));
			bodyTemplateVo.setClientId(rs.getString("ClientId"));
			bodyTemplateVo.setStartTime(rs.getTimestamp("StartTime"));
			bodyTemplateVo.setDescription(rs.getString("Description"));
			bodyTemplateVo.setStatusId(rs.getString("StatusId"));
			bodyTemplateVo.setTemplateValue(rs.getString("TemplateValue"));
			bodyTemplateVo.setContentType(rs.getString("ContentType"));
			
			return bodyTemplateVo;
		}
	}

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
		List<?> list = getJdbcTemplate().query(sql, parms, new BodyTemplateMapper());
		if (list.size()>0)
			return (BodyTemplateVo)list.get(0);
		else
			return null;
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
		List<?> list = getJdbcTemplate().query(sql, parms, new BodyTemplateMapper());
		if (list.size()>0)
			return (BodyTemplateVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<BodyTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" BodyTemplate where templateId=? " +
			" order by clientId, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<BodyTemplateVo> list = (List<BodyTemplateVo>)getJdbcTemplate().query(sql, parms, new BodyTemplateMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<BodyTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" BodyTemplate where clientId=? " +
			" order by templateId, startTime asc ";
		Object[] parms = new Object[] {clientId};
		List<BodyTemplateVo> list = (List<BodyTemplateVo>)getJdbcTemplate().query(sql, parms, new BodyTemplateMapper());
		return list;
	}
	
	public int update(BodyTemplateVo bodyTemplateVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(bodyTemplateVo.getTemplateId());
		fields.add(bodyTemplateVo.getClientId());
		fields.add(bodyTemplateVo.getStartTime());
		fields.add(bodyTemplateVo.getDescription());
		fields.add(bodyTemplateVo.getStatusId());
		fields.add(bodyTemplateVo.getTemplateValue());
		fields.add(bodyTemplateVo.getContentType());
		fields.add(bodyTemplateVo.getRowId());
		
		String sql =
			"update BodyTemplate set " +
				"TemplateId=?, " +
				"ClientId=?, " +
				"StartTime=?, " +
				"Description=?, " +
				"StatusId=?, " +
				"TemplateValue=?, " +
				"ContentType=? " +
			"where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
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
		String sql = 
			"INSERT INTO BodyTemplate (" +
			"TemplateId, " +
			"ClientId, " +
			"StartTime, " +
			"Description, " +
			"StatusId, " +
			"TemplateValue, " +
			"ContentType" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(bodyTemplateVo.getTemplateId());
		fields.add(bodyTemplateVo.getClientId());
		fields.add(bodyTemplateVo.getStartTime());
		fields.add(bodyTemplateVo.getDescription());
		fields.add(bodyTemplateVo.getStatusId());
		fields.add(bodyTemplateVo.getTemplateValue());
		fields.add(bodyTemplateVo.getContentType());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		bodyTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
