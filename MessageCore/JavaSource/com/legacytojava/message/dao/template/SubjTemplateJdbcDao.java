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

import com.legacytojava.message.vo.template.SubjTemplateVo;

@Component("subjTemplateDao")
public class SubjTemplateJdbcDao implements SubjTemplateDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}
	
	private static final class SubjTemplateMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			SubjTemplateVo subjTemplateVo = new SubjTemplateVo();
			
			subjTemplateVo.setRowId(rs.getInt("RowId"));
			subjTemplateVo.setTemplateId(rs.getString("TemplateId"));
			subjTemplateVo.setClientId(rs.getString("ClientId"));
			subjTemplateVo.setStartTime(rs.getTimestamp("StartTime"));
			subjTemplateVo.setDescription(rs.getString("Description"));
			subjTemplateVo.setStatusId(rs.getString("StatusId"));
			subjTemplateVo.setTemplateValue(rs.getString("TemplateValue"));
			
			return subjTemplateVo;
		}
	}

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
		
		List<?> list = getJdbcTemplate().query(sql, parms, new SubjTemplateMapper());
		if (list.size()>0)
			return (SubjTemplateVo)list.get(0);
		else
			return null;
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
		List<?> list = getJdbcTemplate().query(sql, parms, new SubjTemplateMapper());
		if (list.size()>0)
			return (SubjTemplateVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubjTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" SubjTemplate where templateId=? " +
			" order by clientId, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<SubjTemplateVo> list = (List<SubjTemplateVo>)getJdbcTemplate().query(sql, parms, new SubjTemplateMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubjTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" SubjTemplate where clientId=? " +
			" order by templateId, startTime asc ";
		Object[] parms = new Object[] {clientId};
		List<SubjTemplateVo> list = (List<SubjTemplateVo>)getJdbcTemplate().query(sql, parms, new SubjTemplateMapper());
		return list;
	}
	
	public int update(SubjTemplateVo subjTemplateVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(subjTemplateVo.getTemplateId());
		fields.add(subjTemplateVo.getClientId());
		fields.add(subjTemplateVo.getStartTime());
		fields.add(subjTemplateVo.getDescription());
		fields.add(subjTemplateVo.getStatusId());
		fields.add(subjTemplateVo.getTemplateValue());
		fields.add(subjTemplateVo.getRowId());
		
		String sql =
			"update SubjTemplate set " +
				"TemplateId=?, " +
				"ClientId=?, " +
				"StartTime=?, " +
				"Description=?, " +
				"StatusId=?, " +
				"TemplateValue=? " +
			"where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
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
		String sql = 
			"INSERT INTO SubjTemplate (" +
			"TemplateId, " +
			"ClientId, " +
			"StartTime, " +
			"Description, " +
			"StatusId, " +
			"TemplateValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(subjTemplateVo.getTemplateId());
		fields.add(subjTemplateVo.getClientId());
		fields.add(subjTemplateVo.getStartTime());
		fields.add(subjTemplateVo.getDescription());
		fields.add(subjTemplateVo.getStatusId());
		fields.add(subjTemplateVo.getTemplateValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		subjTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
