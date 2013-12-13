package com.legacytojava.message.dao.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.rule.RuleElementVo;

@Component("ruleElementDao")
public class RuleElementJdbcDao extends AbstractDao implements RuleElementDao {
	
	public RuleElementVo getByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"select * " +
			"from RuleElement " +
				" where ruleName=? and elementSeq=?";
		
		Object[] parms = new Object[] {ruleName, elementSeq};
		try {
			RuleElementVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleElementVo> getAll() {
		String sql = 
			"select * " +
			" from " +
				" RuleElement " +
			" order by ruleName asc, elementSeq asc ";
		List<RuleElementVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
		return list;
	}
	
	public List<RuleElementVo> getByRuleName(String ruleName) {
		String sql = 
			"select * " +
			" from " +
				" RuleElement " +
				" where ruleName = ? " +
			" order by elementSeq asc ";
		Object[] parms = new Object[] { ruleName };
		List<RuleElementVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
		return list;
	}
	
	public synchronized int update(RuleElementVo ruleElementVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleElementVo);
		String sql = MetaDataUtil.buildUpdateStatement("RuleElement", ruleElementVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"delete from RuleElement where ruleName=? and elementSeq=?";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		fields.add(elementSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from RuleElement where ruleName=?";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleElementVo ruleElementVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleElementVo);
		String sql = MetaDataUtil.buildInsertStatement("RuleElement", ruleElementVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleElementVo.setRowId(retrieveRowId());
		updateReloadFlags();
		return rowsInserted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateRuleReloadFlag();
	}

	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
}
