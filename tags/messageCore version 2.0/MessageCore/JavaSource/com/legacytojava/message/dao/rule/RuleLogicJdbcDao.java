package com.legacytojava.message.dao.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bo.rule.RuleBase;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.rule.RuleLogicVo;

@Component("ruleLogicDao")
public class RuleLogicJdbcDao implements RuleLogicDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}
	
	static final class RuleLogicMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			RuleLogicVo ruleLogicVo = new RuleLogicVo();
			
			ruleLogicVo.setRowId(rs.getInt("RowId"));
			ruleLogicVo.setRuleName(rs.getString("RuleName"));
			ruleLogicVo.setRuleSeq(rs.getInt("RuleSeq"));
			ruleLogicVo.setRuleType(rs.getString("RuleType"));
			ruleLogicVo.setStatusId(rs.getString("StatusId"));
			ruleLogicVo.setStartTime(rs.getTimestamp("StartTime"));
			ruleLogicVo.setMailType(rs.getString("MailType"));
			ruleLogicVo.setRuleCategory(rs.getString("RuleCategory"));
			ruleLogicVo.setIsSubRule(rs.getString("IsSubRule"));
			ruleLogicVo.setBuiltInRule(rs.getString("BuiltInRule"));
			ruleLogicVo.setDescription(rs.getString("Description"));
			ruleLogicVo.setSubRuleCount(rs.getInt("SubRuleCount"));
			// for updates that change rule name
			ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
			ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
			
			return ruleLogicVo;
		}
	}

	public RuleLogicVo getByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = 
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
				" left outer join RuleSubRuleMap s on r.RuleName=s.RuleName " +
			" where r.ruleName=? and r.RuleSeq=? " +
			" group by " +
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
		
		Object[] parms = new Object[] {ruleName};
		
		List<?> list = getJdbcTemplate().query(sql, parms, new RuleLogicMapper());
		if (list.size()>0)
			return (RuleLogicVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<RuleLogicVo> getByRuleName(String ruleName) {
		String sql = 
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
				" left outer join RuleSubRuleMap s on r.RuleName=s.RuleName " +
			" where r.ruleName=? " +
			" group by " +
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
				"r.Description " +
			" order by r.RuleSeq ";
		
		Object[] parms = new Object[] {ruleName};
		
		List<?> list = getJdbcTemplate().query(sql, parms, new RuleLogicMapper());
		return (List<RuleLogicVo>) list;
	}
	
	public int getNextRuleSequence() {
		String sql = 
			"select max(RuleSeq) from RuleLogic";

		int nextSeq = getJdbcTemplate().queryForInt(sql);
		return (nextSeq + 1);
	}
	
	@SuppressWarnings("unchecked")
	public List<RuleLogicVo> getActiveRules() {
		String sql = 
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
				" left outer join RuleSubRuleMap s on r.RuleName=s.RuleName " +
			" where r.statusId=? and r.startTime<=? " +
			" group by " +
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
				"r.Description " +
			" order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		Object[] parms = new Object[] {StatusIdCode.ACTIVE, new Timestamp(System.currentTimeMillis())};
		List<RuleLogicVo> list = (List<RuleLogicVo>)getJdbcTemplate().query(sql, parms, new RuleLogicMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<RuleLogicVo> getAll(boolean builtInRule) {
		String sql = 
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
		
		if (builtInRule) {
			sql += " where r.BuiltInRule=? and r.IsSubRule!='" + Constants.YES_CODE + "' ";
		}
		else {
			sql += " where r.BuiltInRule!=? ";
		}
		sql += " group by " +
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
		sql += " order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(Constants.YES_CODE);
		List<RuleLogicVo> list = (List<RuleLogicVo>)getJdbcTemplate().query(sql, fields.toArray(), new RuleLogicMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<RuleLogicVo> getAllSubRules(boolean excludeBuiltIn) {
		String sql = 
			"select *, 0 as SubRuleCount " +
			" from RuleLogic " +
				" where IsSubRule='" + Constants.YES_CODE + "' ";
		if (excludeBuiltIn) {
			sql += " and BuiltInRule!='" + Constants.YES_CODE + "' ";
		}
		
		List<RuleLogicVo> list = (List<RuleLogicVo>)getJdbcTemplate().query(sql, new RuleLogicMapper());
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
			"update RuleLogic set " +
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
			"delete from RuleLogic where RuleName=? and RuleSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleName);
		fields.add(ruleSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleLogicVo ruleLogicVo) {
		String sql = 
			"INSERT INTO RuleLogic (" +
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
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
