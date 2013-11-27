package com.es.dao.action;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.action.RuleDataTypeVo;

@Component("ruleDataTypeDao")
public class RuleDataTypeDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public RuleDataTypeVo getByTypeValuePair(String type, String value) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Data_Type where DataType=? and DataTypeValue=? ";
		
		Object[] parms = new Object[] {type, value};
		
		List<RuleDataTypeVo> list = getJdbcTemplate().query(sql, parms, new BeanPropertyRowMapper<RuleDataTypeVo>(RuleDataTypeVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public RuleDataTypeVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Data_Type where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		
		try {
			RuleDataTypeVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleDataTypeVo>(RuleDataTypeVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleDataTypeVo> getByDataType(String dataType) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Data_Type where DataType=? " +
			" order by DataTypeValue asc ";
		
		Object[] parms = new Object[] {dataType};
		List<RuleDataTypeVo> list = getJdbcTemplate().query(sql, parms, new BeanPropertyRowMapper<RuleDataTypeVo>(RuleDataTypeVo.class));
		return list;
	}
	
	public List<String> getDataTypes() {
		String sql = 
			"select distinct(DataType) " +
			"from " +
				"Rule_Data_Type " +
			" order by DataType asc ";
		
		List<String> list = (List<String>)getJdbcTemplate().queryForList(sql, String.class);
		return list;
	}
	
	public int update(RuleDataTypeVo msgDataTypeVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgDataTypeVo.getDataType());
		fields.add(msgDataTypeVo.getDataTypeValue());
		fields.add(msgDataTypeVo.getMiscProperties());
		fields.add(msgDataTypeVo.getRowId());
		
		String sql =
			"update Rule_Data_Type set " +
				"DataType=?, " +
				"DataTypeValue=?, " +
				"MiscProperties=? "+
			" where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from Rule_Data_Type where RowId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(rowId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByDataType(String dataType) {
		String sql = 
			"delete from Rule_Data_Type where DataType=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(dataType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RuleDataTypeVo msgDataTypeVo) {
		String sql = 
			"INSERT INTO Rule_Data_Type (" +
			"DataType, " +
			"DataTypeValue, " +
			"MiscProperties " +
			") VALUES (" +
				" ?, ?, ? " +
				")";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgDataTypeVo.getDataType());
		fields.add(msgDataTypeVo.getDataTypeValue());
		fields.add(msgDataTypeVo.getMiscProperties());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		msgDataTypeVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
