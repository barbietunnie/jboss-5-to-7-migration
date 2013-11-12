package com.legacytojava.message.dao.template;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.template.TemplateVariableVo;

@Component("templateVariableDao")
public class TemplateVariableJdbcDao implements TemplateVariableDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}
	
	private static final HashMap<String, List<?>> currentVariablesCache = new HashMap<String, List<?>>();
	
	private static final class TemplateVariableMapper implements RowMapper<TemplateVariableVo> {
		
		public TemplateVariableVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			TemplateVariableVo templateVariableVo = new TemplateVariableVo();
			
			templateVariableVo.setRowId(rs.getInt("RowId"));
			templateVariableVo.setTemplateId(rs.getString("TemplateId"));
			templateVariableVo.setClientId(rs.getString("ClientId"));
			templateVariableVo.setVariableName(rs.getString("VariableName"));
			templateVariableVo.setStartTime(rs.getTimestamp("StartTime"));
			templateVariableVo.setVariableValue(rs.getString("VariableValue"));
			templateVariableVo.setVariableFormat(rs.getString("VariableFormat"));
			templateVariableVo.setVariableType(rs.getString("VariableType"));
			templateVariableVo.setStatusId(rs.getString("StatusId"));
			templateVariableVo.setAllowOverride(rs.getString("AllowOverride"));
			templateVariableVo.setRequired(rs.getString("Required"));
			
			return templateVariableVo;
		}
	}

	public TemplateVariableVo getByPrimaryKey(String templateId, String clientId,
			String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"TemplateVariable where templateId=? and variableName=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		keys.add(variableName);
		if (clientId==null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and clientId=? ";
			keys.add(clientId);
		}
		if (startTime==null) {
			sql += " and startTime is null ";
		}
		else {
			sql += " and startTime=? ";
			keys.add(startTime);
		}
		
		Object[] parms = keys.toArray();
		List<?> list = getJdbcTemplate().query(sql, parms, new TemplateVariableMapper());
		if (list.size()>0)
			return (TemplateVariableVo)list.get(0);
		else
			return null;
	}
	
	public TemplateVariableVo getByBestMatch(String templateId, String clientId,
			String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"TemplateVariable where templateId=? and variableName=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(templateId);
		keys.add(variableName);
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
		List<?> list = getJdbcTemplate().query(sql, parms, new TemplateVariableMapper());
		if (list.size()>0)
			return (TemplateVariableVo)list.get(0);
		else
			return null;
	}
	
	public List<TemplateVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" TemplateVariable where variableName=? " +
			" order by templateId, clientId, startTime asc ";
		Object[] parms = new Object[] {variableName};
		List<TemplateVariableVo> list =  (List<TemplateVariableVo>)getJdbcTemplate().query(sql, parms, new TemplateVariableMapper());
		return list;
	}
	
	public List<TemplateVariableVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" TemplateVariable where clientId=? " +
			" order by templateId, variableName, startTime ";
		Object[] parms = new Object[] { clientId };
		List<TemplateVariableVo> list =  (List<TemplateVariableVo>)getJdbcTemplate().query(sql, parms, new TemplateVariableMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<TemplateVariableVo> getCurrentByTemplateId(String templateId, String clientId) {
		if (!currentVariablesCache.containsKey(templateId+"."+clientId)) {
			String sql = 
				"select * " +
				" from TemplateVariable as a " +
				" inner join ( " +
				"  select b.templateid, b.clientid, b.variablename, max(b.starttime) as maxtime " +
				"   from TemplateVariable b " +
				"   where b.statusid=? and b.starttime<=? " +
				"    and b.templateid=? and b.clientid=? " +
				"   group by b.templateid, b.clientid, b.variablename " +
				" ) as c " +
				"  on a.variablename=c.variablename and a.starttime=c.maxtime " +
				"    and a.templateid=c.templateid and a.clientid=c.clientid " +
				" order by a.variableName asc ";
			Object[] parms = new Object[] { StatusIdCode.ACTIVE,
					new Timestamp(new java.util.Date().getTime()), templateId, clientId };
			List<TemplateVariableVo> list =  (List<TemplateVariableVo>)getJdbcTemplate().query(sql, parms, new TemplateVariableMapper());
			currentVariablesCache.put(templateId+"."+clientId, list);
		}
		
		List<TemplateVariableVo> list = (List<TemplateVariableVo>)currentVariablesCache.get(templateId+"."+clientId);
		return list;
	}
	
	public List<TemplateVariableVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" TemplateVariable where templateId=? " +
			" order by clientId, variableName, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<TemplateVariableVo> list =  (List<TemplateVariableVo>)getJdbcTemplate().query(sql, parms, new TemplateVariableMapper());
		return list;
	}
	
	public int update(TemplateVariableVo templateVariableVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateVariableVo.getTemplateId());
		fields.add(templateVariableVo.getClientId());
		fields.add(templateVariableVo.getVariableName());
		fields.add(templateVariableVo.getStartTime());
		fields.add(templateVariableVo.getVariableValue());
		fields.add(templateVariableVo.getVariableFormat());
		fields.add(templateVariableVo.getVariableType());
		fields.add(templateVariableVo.getStatusId());
		fields.add(templateVariableVo.getAllowOverride());
		fields.add(templateVariableVo.getRequired());
		fields.add(templateVariableVo.getRowId());
		
		String sql =
			"update TemplateVariable set " +
				"TemplateId=?, " +
				"ClientId=?, " +
				"VariableName=?, " +
				"StartTime=?, " +
				"VariableValue=?, " +
				"VariableFormat=?, " +
				"VariableType=?, " +
				"StatusId=?, " +
				"AllowOverride=?, " +
				"Required=? " +
			"where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted>0) {
			currentVariablesCache.remove(templateVariableVo.getTemplateId() + "."
					+ templateVariableVo.getClientId());
		}
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String templateId, String clientId, String variableName,
			Timestamp startTime) {
		String sql = 
			"delete from TemplateVariable where templateId=? and clientId=? and variableName=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		fields.add(clientId);
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
			currentVariablesCache.remove(templateId+"."+clientId);
		}
		return rowsDeleted;
	}
	
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from TemplateVariable where variableName=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.clear();
		return rowsDeleted;
	}
	
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from TemplateVariable where clientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.clear();
		return rowsDeleted;
	}
	
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from TemplateVariable where templateId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.clear();
		return rowsDeleted;
	}
	
	public int insert(TemplateVariableVo templateVariableVo) {
		String sql = 
			"INSERT INTO TemplateVariable (" +
			"TemplateId, " +
			"ClientId, " +
			"VariableName, " +
			"StartTime, " +
			"VariableValue, " +
			"VariableFormat, " +
			"VariableType, " +
			"StatusId, " +
			"AllowOverride, " +
			"Required " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ? ,?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(templateVariableVo.getTemplateId());
		fields.add(templateVariableVo.getClientId());
		fields.add(templateVariableVo.getVariableName());
		fields.add(templateVariableVo.getStartTime());
		fields.add(templateVariableVo.getVariableValue());
		fields.add(templateVariableVo.getVariableFormat());
		fields.add(templateVariableVo.getVariableType());
		fields.add(templateVariableVo.getStatusId());
		fields.add(templateVariableVo.getAllowOverride());
		fields.add(templateVariableVo.getRequired());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		templateVariableVo.setRowId(retrieveRowId());
		if (rowsInserted>0) {
			currentVariablesCache.remove(templateVariableVo.getTemplateId() + "."
					+ templateVariableVo.getClientId());
		}
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
