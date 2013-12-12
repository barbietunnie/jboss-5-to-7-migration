package com.es.dao.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.dao.sender.ReloadFlagsDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.rule.RuleElementVo;

@Component("ruleElementDao")
public class RuleElementDao extends AbstractDao {
	
	public RuleElementVo getByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"select * " +
			"from Rule_Element " +
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
				" Rule_Element " +
			" order by ruleName asc, elementSeq asc ";
		List<RuleElementVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
		return list;
	}
	
	public List<RuleElementVo> getByRuleName(String ruleName) {
		String sql = 
			"select * " +
			" from " +
				" Rule_Element " +
				" where ruleName = ? " +
			" order by elementSeq asc ";
		Object[] parms = new Object[] { ruleName };
		List<RuleElementVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
		return list;
	}
	
	public synchronized int update(RuleElementVo ruleElementVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleElementVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Rule_Element", ruleElementVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"delete from Rule_Element where ruleName=? and elementSeq=?";
		
		List<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		fields.add(String.valueOf(elementSeq));
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from Rule_Element where ruleName=?";
		
		List<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleElementVo ruleElementVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleElementVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Rule_Element", ruleElementVo);

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
