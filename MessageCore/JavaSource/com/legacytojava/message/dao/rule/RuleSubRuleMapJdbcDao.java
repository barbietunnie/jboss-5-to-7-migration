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
import com.legacytojava.message.vo.rule.RuleSubRuleMapVo;

@Component("ruleSubRuleMapDao")
public class RuleSubRuleMapJdbcDao extends AbstractDao implements RuleSubRuleMapDao {
	
	public RuleSubRuleMapVo getByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"select * " +
			"from " +
				"RuleSubRuleMap where ruleName=? and subRuleName=? ";
		
		Object[] parms = new Object[] {ruleName, subRuleName};
		try {
			RuleSubRuleMapVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleSubRuleMapVo>(RuleSubRuleMapVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleSubRuleMapVo> getByRuleName(String ruleName) {
		String sql = 
			"select * " +
			" from " +
				" RuleSubRuleMap where ruleName=? " +
			" order by subRuleSeq asc ";
		
		Object[] parms = new Object[] {ruleName};
		List<RuleSubRuleMapVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleSubRuleMapVo>(RuleSubRuleMapVo.class));
		return list;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"delete from RuleSubRuleMap where ruleName=? and subRuleName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		fields.add(subRuleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from RuleSubRuleMap where ruleName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int update(RuleSubRuleMapVo ruleSubRuleMapVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleSubRuleMapVo);
		String sql = MetaDataUtil.buildUpdateStatement("RuleSubRuleMap", ruleSubRuleMapVo);
		int rowsUpdated = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpdated;
	}
	
	public synchronized int insert(RuleSubRuleMapVo ruleSubRuleMapVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleSubRuleMapVo);
		String sql = MetaDataUtil.buildInsertStatement("RuleSubRuleMap", ruleSubRuleMapVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleSubRuleMapVo.setRowId(retrieveRowId());
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
