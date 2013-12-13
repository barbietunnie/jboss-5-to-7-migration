package com.legacytojava.message.dao.outbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.outbox.RenderVariableVo;

@Component("renderVariableDao")
public class RenderVariableJdbcDao extends AbstractDao implements RenderVariableDao {
	
	public RenderVariableVo getByPrimaryKey(long renderId, String variableName) {
		String sql = 
			"select * " +
			"from " +
				"RenderVariable where RenderId=? and variableName=? ";
		
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
				" RenderVariable where RenderId=? " +
			" order by variableName";
		Object[] parms = new Object[] {renderId};
		List<RenderVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderVariableVo>(RenderVariableVo.class));
		return list;
	}
	
	public int update(RenderVariableVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("RenderVariable", renderVariableVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String variableName) {
		String sql = 
			"delete from RenderVariable where renderId=? and variableName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long msgId) {
		String sql = 
			"delete from RenderVariable where renderId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderVariableVo renderVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("RenderVariable", renderVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
