package com.legacytojava.message.dao.emailaddr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.vo.emailaddr.EmailVariableVo;

@Component("emailVariableDao")
public class EmailVariableJdbcDao implements EmailVariableDao {
	static final Logger logger = Logger.getLogger(EmailVariableJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate; 
	}

	private NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (this.namedParameterJdbcTemplate == null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(mysqlDataSource);
		}
		return this.namedParameterJdbcTemplate; 
	}

	private static final class EmailVariableMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			EmailVariableVo emailVariableVo = new EmailVariableVo();
			
			emailVariableVo.setRowId(rs.getInt("RowId"));
			emailVariableVo.setVariableName(rs.getString("VariableName"));
			emailVariableVo.setVariableType(rs.getString("VariableType"));
			emailVariableVo.setTableName(rs.getString("TableName"));
			emailVariableVo.setColumnName(rs.getString("ColumnName"));
			emailVariableVo.setStatusId(rs.getString("StatusId"));
			emailVariableVo.setIsBuiltIn(rs.getString("IsBuiltIn"));
			emailVariableVo.setDefaultValue(rs.getString("DefaultValue"));
			emailVariableVo.setVariableQuery(rs.getString("VariableQuery"));
			emailVariableVo.setVariableProc(rs.getString("VariableProc"));
			
			return emailVariableVo;
		}
	}
	
	public EmailVariableVo getByName(String variableName) {
		String sql = "select * from EmailVariable where VariableName=:variableName";
		SqlParameterSource namedParameters = new MapSqlParameterSource("variableName", variableName);
		List<?> list = (List<?>)getNamedParameterJdbcTemplate().query(sql, namedParameters, new EmailVariableMapper());
		if (list.size()>0) {
			return (EmailVariableVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAll() {
		String sql = "select * from EmailVariable " +
		" order by RowId";
		List<EmailVariableVo> list = (List<EmailVariableVo>)getJdbcTemplate().query(sql, new EmailVariableMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAllForTrial() {
		String sql = "select * from EmailVariable " +
		" order by RowId" +
		" limit 50";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(50);
		getJdbcTemplate().setMaxRows(50);
		List<EmailVariableVo> list = (List<EmailVariableVo>)getJdbcTemplate().query(sql, new EmailVariableMapper());
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAllCustomVariables() {
		String sql = "select * from EmailVariable " +
			" where IsBuiltIn!='" + Constants.YES_CODE + "' " +
			" order by RowId";
		List<EmailVariableVo> list = (List<EmailVariableVo>)getJdbcTemplate().query(sql, new EmailVariableMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAllBuiltinVariables() {
		String sql = "select * from EmailVariable " +
			" where IsBuiltIn='" + Constants.YES_CODE + "' " +
			" order by RowId";
		List<EmailVariableVo> list = (List<EmailVariableVo>)getJdbcTemplate().query(sql, new EmailVariableMapper());
		return list;
	}
	
	/**
	 * returns query result as string or null if not found.
	 */
	public String getByQuery(String query, long addrId) {
		Object[] parms = new Object[] {addrId};
		List<String> list = (List<String>)getJdbcTemplate().queryForList(query, parms, String.class);
		if (list.size() == 0) return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			String item = list.get(i);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(item);
		}
		return sb.toString();
	}
	
	public int update(EmailVariableVo emailVariableVo) {

		String sql = "update EmailVariable set " +
			"VariableName=:variableName," +
			"VariableType=:variableType," +
			"TableName=:tableName," +
			"ColumnName=:columnName," +
			"StatusId=:statusId," +
			"IsBuiltIn=:isBuiltIn," +
 			"DefaultValue=:defaultValue," +
			"VariableQuery=:variableQuery," +
			"VariableProc=:variableProc " +
			" where RowId=:rowId";
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailVariableVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		
		return rowsUpadted;
	}
	
	public int deleteByName(String variableName) {
		String sql = "delete from EmailVariable where VariableName=:variableName";
		Map<String,?> namedParameters = Collections.singletonMap("variableName", variableName);
		int rowsDeleted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsDeleted;
	}
	
	public int insert(EmailVariableVo emailVariableVo) {
		String sql = "INSERT INTO EmailVariable (" +
			"VariableName," +
			"VariableType," +
			"TableName," +
			"ColumnName," +
			"StatusId," +
			"IsBuiltIn," +
			"DefaultValue," +
			"VariableQuery," +
			"VariableProc " +
			") VALUES (" +
				" :variableName, :variableType, :tableName, :columnName, :statusId, :isBuiltIn, :defaultValue, :variableQuery, :variableProc " +
				")";
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		emailVariableVo.setRowId(retrieveRowId());
		return rowsInserted;
	}

	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
