package com.es.dao.rule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.dao.sender.ReloadFlagsDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.RuleCategory;
import com.es.data.constant.StatusId;
import com.es.vo.rule.RuleLogicVo;

@Component("ruleLogicDao")
public class RuleLogicDao extends AbstractDao {
	
	private String selectCluse = "select " +
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
		" from Rule_Logic r " +
			" left outer join Rule_SubRule_Map s on r.RuleName=s.RuleName ";
	
	private String groupByCluse = " group by " +
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
	
	public RuleLogicVo getByPrimaryKey(String ruleName) {
		String sql = 
			"select r.* from Rule_Logic r " +
			" where r.ruleName=? ";
		
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
	
	public RuleLogicVo getByRuleName(String ruleName) {
		String sql = 
			"select r.* from Rule_Logic r "  +
			" where r.ruleName=? ";
		
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
	
	public int getNextRuleSequence() {
		String sql = 
			"select max(RuleSeq) from RuleLogic";

		int nextSeq = getJdbcTemplate().queryForObject(sql, Integer.class);
		return (nextSeq + 1);
	}
	
	public List<RuleLogicVo> getActiveRules() {
		String sql = 
			selectCluse +
			" where r.statusId=? and r.startTime<=? " +
			groupByCluse +
			" order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		Object[] parms = new Object[] {StatusId.ACTIVE.getValue(), new Timestamp(System.currentTimeMillis())};
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public List<RuleLogicVo> getAll(boolean builtInRule) {
		String sql = 
			selectCluse;
		
		if (builtInRule) {
			sql += " where r.BuiltInRule=? and r.IsSubRule!='" + CodeType.YES_CODE.getValue() + "' ";
		}
		else {
			sql += " where r.BuiltInRule!=? ";
		}
		sql += groupByCluse;
		sql += " order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(CodeType.YES_CODE.getValue());
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, fields.toArray(), 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public List<RuleLogicVo> getAllSubRules(boolean excludeBuiltIn) {
		String sql = 
			"select *, 0 as SubRuleCount " +
			" from Rule_Logic " +
				" where IsSubRule='" + CodeType.YES_CODE.getValue() + "' ";
		if (excludeBuiltIn) {
			sql += " and BuiltInRule!='" + CodeType.YES_CODE.getValue() + "' ";
		}
		
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	public List<String> getBuiltinRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) " +
			" from Rule_Logic " +
			" where BuiltInRule=? and IsSubRule!=? and RuleCategory=? " +
			" order by RuleName ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(CodeType.YES_CODE.getValue());
		fields.add(CodeType.YES_CODE.getValue());
		fields.add(RuleCategory.MAIN_RULE.getValue());
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	public List<String> getCustomRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) " +
			" from Rule_Logic " +
			" where BuiltInRule!=? and IsSubRule!=? and RuleCategory=? " +
			" order by RuleName ";

		ArrayList<String> fields = new ArrayList<String>();
		fields.add(CodeType.YES_CODE.getValue());
		fields.add(CodeType.YES_CODE.getValue());
		fields.add(RuleCategory.MAIN_RULE.getValue());
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	public synchronized int update(RuleLogicVo ruleLogicVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleLogicVo.getRuleName());
		fields.add(Integer.valueOf(ruleLogicVo.getRuleSeq()));
		fields.add(ruleLogicVo.getRuleType());
		fields.add(ruleLogicVo.getStatusId());
		fields.add(ruleLogicVo.getStartTime());
		fields.add(ruleLogicVo.getMailType());
		fields.add(ruleLogicVo.getRuleCategory());
		fields.add(ruleLogicVo.getIsSubRule());
		fields.add(ruleLogicVo.getBuiltInRule());
		fields.add(ruleLogicVo.getDescription());
//		String origRuleName = ruleLogicVo.getOrigRuleName();
//		if (StringUtil.isEmpty(origRuleName)) {
//			fields.add(ruleLogicVo.getRuleName());
//		}
//		else { // Original rule name is valued.
//			fields.add(ruleLogicVo.getOrigRuleName());
//		}
//		if (ruleLogicVo.getOrigRuleSeq() > -1) {
//			fields.add(ruleLogicVo.getOrigRuleSeq());
//		}
//		else {
//			fields.add(ruleLogicVo.getRuleSeq());
//		}
		fields.add(ruleLogicVo.getRowId());
		
		String sql =
			"update Rule_Logic set " +
				"RuleName=?, " +
				"RuleSeq=?, " +
				"RuleType=?, " +
				"StatusId=?, " +
				"StartTime=?, " +
				"MailType=?, " +
				"RuleCategory=?, " +
				"IsSubRule=?, " +
				"BuiltInRule=?, " +
				"Description=? " +
			" where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
		ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = 
			"delete from Rule_Logic where RuleName=? and RuleSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleName);
		fields.add(ruleSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleLogicVo ruleLogicVo) {
		String sql = 
			"INSERT INTO Rule_Logic (" +
			"RuleName, " +
			"RuleSeq, " +
			"RuleType, " +
			"StatusId, " +
			"StartTime, " +
			"MailType, " +
			"RuleCategory, " +
			"IsSubRule, " +
			"BuiltInRule, " +
			"Description " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleLogicVo.getRuleName());
		fields.add(Integer.valueOf(ruleLogicVo.getRuleSeq()));
		fields.add(ruleLogicVo.getRuleType());
		fields.add(ruleLogicVo.getStatusId());
		fields.add(ruleLogicVo.getStartTime());
		fields.add(ruleLogicVo.getMailType());
		fields.add(ruleLogicVo.getRuleCategory());
		fields.add(ruleLogicVo.getIsSubRule());
		fields.add(ruleLogicVo.getBuiltInRule());
		fields.add(ruleLogicVo.getDescription());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
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
