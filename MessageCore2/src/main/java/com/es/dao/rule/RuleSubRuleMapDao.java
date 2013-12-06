package com.es.dao.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.dao.sender.ReloadFlagsDao;
import com.es.vo.rule.RuleSubRuleMapVo;

@Component("ruleSubRuleMapDao")
public class RuleSubRuleMapDao extends AbstractDao {
	
	public RuleSubRuleMapVo getByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Subrule_Map where ruleName=? and subRuleName=? ";
		
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
				" Rule_Subrule_Map where ruleName=? " +
			" order by subRuleSeq asc ";
		
		Object[] parms = new Object[] {ruleName};
		List<RuleSubRuleMapVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleSubRuleMapVo>(RuleSubRuleMapVo.class));
		return list;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"delete from Rule_Subrule_Map where ruleName=? and subRuleName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		fields.add(subRuleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from Rule_Subrule_Map where ruleName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int update(RuleSubRuleMapVo ruleSubRuleMapVo) {
		String sql = 
			"update Rule_Subrule_Map set " +
			"SubRuleSeq=?, " +
			"RuleName=?, " +
			"SubRuleName=? " +
			" where" +
				" RowId=?";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleSubRuleMapVo.getSubRuleSeq());
		fields.add(ruleSubRuleMapVo.getRuleName());
		fields.add(ruleSubRuleMapVo.getSubRuleName());
		fields.add(ruleSubRuleMapVo.getRowId());
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsUpdated;
	}
	
	public synchronized int insert(RuleSubRuleMapVo ruleSubRuleMapVo) {
		String sql = 
			"INSERT INTO Rule_Subrule_Map (" +
			"RuleName, " +
			"SubRuleName, " +
			"SubRuleSeq " +
			") VALUES (" +
				" ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleSubRuleMapVo.getRuleName());
		fields.add(ruleSubRuleMapVo.getSubRuleName());
		fields.add(ruleSubRuleMapVo.getSubRuleSeq());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
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
