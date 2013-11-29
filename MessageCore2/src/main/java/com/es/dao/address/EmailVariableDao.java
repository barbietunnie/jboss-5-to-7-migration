package com.es.dao.address;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.data.constant.CodeType;
import com.es.vo.address.EmailVariableVo;

@Component("emailVariableDao")
public class EmailVariableDao {
	static final Logger logger = Logger.getLogger(EmailVariableDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate; 
	}

	private NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (this.namedParameterJdbcTemplate == null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(msgDataSource);
		}
		return this.namedParameterJdbcTemplate; 
	}

	public EmailVariableVo getByName(String variableName) {
		String sql = "select * from Email_Variable where VariableName=:variableName";
		SqlParameterSource namedParameters = new MapSqlParameterSource("variableName", variableName);
		try {
			EmailVariableVo vo = getNamedParameterJdbcTemplate().queryForObject(sql, namedParameters,
					new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<EmailVariableVo> getAll() {
		String sql = "select * from Email_Variable " +
		" order by RowId";
		List<EmailVariableVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		return list;
	}
	
	public List<EmailVariableVo> getAllForTrial() {
		String sql = "select * from Email_Variable " +
		" order by RowId" +
		" limit 50";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(50);
		getJdbcTemplate().setMaxRows(50);
		List<EmailVariableVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public List<EmailVariableVo> getAllCustomVariables() {
		String sql = "select * from Email_Variable " +
			" where IsBuiltIn!='" + CodeType.YES_CODE.getValue() + "' " +
			" order by RowId";
		List<EmailVariableVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		return list;
	}
	
	public List<EmailVariableVo> getAllBuiltinVariables() {
		String sql = "select * from Email_Variable " +
			" where IsBuiltIn='" + CodeType.YES_CODE.getValue() + "' " +
			" order by RowId";
		List<EmailVariableVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		return list;
	}
	
	/**
	 * returns query result as string or null if not found.
	 */
	public String getByQuery(String query, long addrId) {
		Object[] parms = new Object[] {addrId};
		List<String> list = getJdbcTemplate().queryForList(query, parms, String.class);
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

		String sql = "update Email_Variable set " +
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
		String sql = "delete from Email_Variable where VariableName=:variableName";
		Map<String,?> namedParameters = Collections.singletonMap("variableName", variableName);
		int rowsDeleted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsDeleted;
	}
	
	public int insert(EmailVariableVo emailVariableVo) {
		String sql = "INSERT INTO Email_Variable (" +
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
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
