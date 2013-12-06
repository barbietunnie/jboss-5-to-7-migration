package com.es.dao.abst;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractDao {

	@Autowired
	protected DataSource msgDataSource;

	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	protected JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate; 
	}

	protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (this.namedParameterJdbcTemplate == null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(msgDataSource);
		}
		return this.namedParameterJdbcTemplate; 
	}

	protected final int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected final String getRowIdSql() {
		return "select last_insert_id()";
	}
}
