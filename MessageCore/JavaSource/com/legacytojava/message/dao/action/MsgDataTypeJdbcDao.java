package com.legacytojava.message.dao.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.action.MsgDataTypeVo;

@Component("msgDataTypeDao")
public class MsgDataTypeJdbcDao extends AbstractDao implements MsgDataTypeDao {
	
	public MsgDataTypeVo getByTypeValuePair(String type, String value) {
		String sql = 
			"select * " +
			"from " +
				"MsgDataType where DataType=? and DataTypeValue=? ";
		
		Object[] parms = new Object[] {type, value};
		try {
			MsgDataTypeVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgDataTypeVo>(MsgDataTypeVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgDataTypeVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * " +
			"from " +
				"MsgDataType where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		try {
			MsgDataTypeVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgDataTypeVo>(MsgDataTypeVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgDataTypeVo> getByDataType(String dataType) {
		String sql = 
			"select * " +
			"from " +
				"MsgDataType where DataType=? " +
			" order by DataTypeValue asc ";
		
		Object[] parms = new Object[] {dataType};
		List<MsgDataTypeVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgDataTypeVo>(MsgDataTypeVo.class));
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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgDataTypeVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgDataType", msgDataTypeVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgDataTypeVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgDataType", msgDataTypeVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgDataTypeVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
