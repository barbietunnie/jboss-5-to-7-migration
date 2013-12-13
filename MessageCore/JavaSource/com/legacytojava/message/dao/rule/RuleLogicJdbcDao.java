package com.legacytojava.message.dao.rule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bo.rule.RuleBase;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.rule.RuleLogicVo;

@Component("ruleLogicDao")
public class RuleLogicJdbcDao extends AbstractDao implements RuleLogicDao {
	
	private String getSelectClause() {
		String select = 
				"select " +
					"r.RowId, " +
					"r.RuleName, " +
					"r.RuleSeq, " +
					"r.RuleType, " +
					"r.StatusId, " +
					"r.StartTime, " +
					"r.MailType, " +
					"r.RuleCategory, " +
					"r.IsSubRule, " +
					"r.BuiltinRule, " +
					"r.Description, " +
					"count(s.SubRuleName) as SubRuleCount " +
				" from RuleLogic r " +
					" left outer join RuleSubRuleMap s on r.RuleName=s.RuleName ";
		return select;
	}
	
	private String getGroupByClause() {
		String groupBy = " group by " +
				"r.RowId, " +
				"r.RuleName, " +
				"r.RuleSeq, " +
				"r.RuleType, " +
				"r.StatusId, " +
				"r.StartTime, " +
				"r.MailType, " +
				"r.RuleCategory, " +
				"r.IsSubRule, " +
				"r.BuiltinRule, " +
				"r.Description ";
		return groupBy;
	}

	public RuleLogicVo getByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = getSelectClause() +
			" where r.ruleName=? and r.RuleSeq=? " +
			getGroupByClause();
		
		Object[] parms = new Object[] {ruleName};
		try {
			RuleLogicVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleLogicVo> getByRuleName(String ruleName) {
		String sql = getSelectClause() +
			" where r.ruleName=? " +
			getGroupByClause() +
			" order by r.RuleSeq ";
		
		Object[] parms = new Object[] {ruleName};
		
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public int getNextRuleSequence() {
		String sql = 
			"select max(RuleSeq) from RuleLogic";

		int nextSeq = getJdbcTemplate().queryForInt(sql);
		return (nextSeq + 1);
	}
	
	public List<RuleLogicVo> getActiveRules() {
		String sql = getSelectClause() +
			" where r.statusId=? and r.startTime<=? " +
			getGroupByClause() +
			" order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		Object[] parms = new Object[] {StatusIdCode.ACTIVE, new Timestamp(System.currentTimeMillis())};
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public List<RuleLogicVo> getAll(boolean builtInRule) {
		String sql = getSelectClause();
		
		if (builtInRule) {
			sql += " where r.BuiltInRule=? and r.IsSubRule!='" + Constants.YES_CODE + "' ";
		}
		else {
			sql += " where r.BuiltInRule!=? ";
		}
		sql += getGroupByClause();
		sql += " order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(Constants.YES_CODE);
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, fields.toArray(), 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public List<RuleLogicVo> getAllSubRules(boolean excludeBuiltIn) {
		String sql = 
			"select *, 0 as SubRuleCount " +
			" from RuleLogic " +
				" where IsSubRule='" + Constants.YES_CODE + "' ";
		if (excludeBuiltIn) {
			sql += " and BuiltInRule!='" + Constants.YES_CODE + "' ";
		}
		
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public List<String> getBuiltinRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) " +
			" from RuleLogic " +
			" where BuiltInRule=? and IsSubRule!=? and RuleCategory=? " +
			" order by RuleName ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(Constants.YES_CODE);
		fields.add(Constants.YES_CODE);
		fields.add(RuleBase.MAIN_RULE);
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	public List<String> getCustomRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) " +
			" from RuleLogic " +
			" where BuiltInRule!=? and IsSubRule!=? and RuleCategory=? " +
			" order by RuleName ";

		ArrayList<String> fields = new ArrayList<String>();
		fields.add(Constants.YES_CODE);
		fields.add(Constants.YES_CODE);
		fields.add(RuleBase.MAIN_RULE);
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	public synchronized int update(RuleLogicVo ruleLogicVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleLogicVo);
		String sql = MetaDataUtil.buildUpdateStatement("RuleLogic", ruleLogicVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
		ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = 
			"delete from RuleLogic where RuleName=? and RuleSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleName);
		fields.add(ruleSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleLogicVo ruleLogicVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleLogicVo);
		String sql = MetaDataUtil.buildInsertStatement("RuleLogic", ruleLogicVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleLogicVo.setRowId(retrieveRowId());
		ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
		ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
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
