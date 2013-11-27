package com.es.dao.outbox;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.outbox.RenderObjectVo;

@Component("renderObjectDao")
public class RenderObjectDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public RenderObjectVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"Render_Object where RenderId=? and variableName=? ";
		
		Object[] parms = new Object[] {renderId, variableName};
		try {
			RenderObjectVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RenderObjectVo>(RenderObjectVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RenderObjectVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" Render_Object where RenderId=? " +
			" order by variableName";
		Object[] parms = new Object[] {renderId};
		List<RenderObjectVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderObjectVo>(RenderObjectVo.class));
		return list;
	}
	
	public int update(RenderObjectVo renderVariableVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderVariableVo.getVariableFormat());
		fields.add(renderVariableVo.getVariableType());
		fields.add(renderVariableVo.getVariableValue());
		fields.add(renderVariableVo.getRenderId());
		fields.add(renderVariableVo.getVariableName());
		
		String sql =
			"update Render_Object set " +
				"VariableFormat=?, " +
				"VariableType=?, " +
				"VariableValue=? " +
			" where " +
				" RenderId=? and VariableName=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String variableName) {
		String sql = 
			"delete from Render_Object where renderId=? and variableName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from Render_Object where renderId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderObjectVo renderVariableVo) {
		String sql = 
			"INSERT INTO Render_Object (" +
				"RenderId, " +
				"VariableName, " +
				"VariableFormat, " +
				"VariableType, " +
				"VariableValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderVariableVo.getRenderId());
		fields.add(renderVariableVo.getVariableName());
		fields.add(renderVariableVo.getVariableFormat());
		fields.add(renderVariableVo.getVariableType());
		fields.add(renderVariableVo.getVariableValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}