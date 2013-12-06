package com.es.dao.outbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.outbox.RenderVariableVo;

@Component("renderVariableDao")
public class RenderVariableDao extends AbstractDao {
	
	public RenderVariableVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"Render_Variable where RenderId=? and variableName=? ";
		
		Object[] parms = new Object[] {renderId+"", variableName};
		try {
			RenderVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RenderVariableVo>(RenderVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RenderVariableVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" Render_Variable where RenderId=? " +
			" order by variableName";
		Object[] parms = new Object[] {renderId};
		List<RenderVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderVariableVo>(RenderVariableVo.class));
		return list;
	}
	
	public int update(RenderVariableVo renderVariableVo) {
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(renderVariableVo.getVariableFormat());
		fields.add(renderVariableVo.getVariableType());
		fields.add(renderVariableVo.getVariableValue());
		fields.add(renderVariableVo.getRenderId()+"");
		fields.add(renderVariableVo.getVariableName());
		
		String sql =
			"update Render_Variable set " +
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
			"delete from Render_Variable where renderId=? and variableName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from Render_Variable where renderId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderVariableVo renderVariableVo) {
		String sql = 
			"INSERT INTO Render_Variable (" +
				"RenderId, " +
				"VariableName, " +
				"VariableFormat, " +
				"VariableType, " +
				"VariableValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(renderVariableVo.getRenderId()+"");
		fields.add(renderVariableVo.getVariableName());
		fields.add(renderVariableVo.getVariableFormat());
		fields.add(renderVariableVo.getVariableType());
		fields.add(renderVariableVo.getVariableValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
}
