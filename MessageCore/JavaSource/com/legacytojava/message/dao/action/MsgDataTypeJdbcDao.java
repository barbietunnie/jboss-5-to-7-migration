package com.legacytojava.message.dao.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.action.MsgDataTypeVo;

@Component("msgDataTypeDao")
public class MsgDataTypeJdbcDao implements MsgDataTypeDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	static final class MsgDataTypeMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgDataTypeVo msgDataTypeVo = new MsgDataTypeVo();
			
			msgDataTypeVo.setRowId(rs.getInt("RowId"));
			msgDataTypeVo.setDataType(rs.getString("DataType"));
			msgDataTypeVo.setDataTypeValue(rs.getString("DataTypeValue"));
			msgDataTypeVo.setMiscProperties(rs.getString("MiscProperties"));
			
			return msgDataTypeVo;
		}
	}

	public MsgDataTypeVo getByTypeValuePair(String type, String value) {
		String sql = 
			"select * " +
			"from " +
				"MsgDataType where DataType=? and DataTypeValue=? ";
		
		Object[] parms = new Object[] {type, value};
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgDataTypeMapper());
		if (list.size()>0)
			return (MsgDataTypeVo)list.get(0);
		else
			return null;
	}
	
	public MsgDataTypeVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * " +
			"from " +
				"MsgDataType where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgDataTypeMapper());
		if (list.size()>0)
			return (MsgDataTypeVo)list.get(0);
		else
			return null;
	}
	
	public List<MsgDataTypeVo> getByDataType(String dataType) {
		String sql = 
			"select * " +
			"from " +
				"MsgDataType where DataType=? " +
			" order by DataTypeValue asc ";
		
		Object[] parms = new Object[] {dataType};
		@SuppressWarnings("unchecked")
		List<MsgDataTypeVo> list = (List<MsgDataTypeVo>)getJdbcTemplate().query(sql, parms, new MsgDataTypeMapper());
		return list;
	}
	
	public List<String> getDataTypes() {
		String sql = 
			"select distinct(DataType) " +
			"from " +
				"MsgDataType " +
			" order by DataType asc ";
		
		List<String> list = (List<String>)getJdbcTemplate().queryForList(sql, String.class);
		return list;
	}
	
	public int update(MsgDataTypeVo msgDataTypeVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgDataTypeVo.getDataType());
		fields.add(msgDataTypeVo.getDataTypeValue());
		fields.add(msgDataTypeVo.getMiscProperties());
		fields.add(msgDataTypeVo.getRowId());
		
		String sql =
			"update MsgDataType set " +
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
			"delete from MsgDataType where RowId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(rowId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByDataType(String dataType) {
		String sql = 
			"delete from MsgDataType where DataType=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(dataType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgDataTypeVo msgDataTypeVo) {
		String sql = 
			"INSERT INTO MsgDataType (" +
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
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
