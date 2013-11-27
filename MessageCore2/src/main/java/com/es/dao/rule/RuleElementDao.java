package com.es.dao.rule;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.dao.sender.ReloadFlagsDao;
import com.es.vo.rule.RuleElementVo;

@Component("ruleElementDao")
public class RuleElementDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

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
		
		ArrayList<Object> fields = new ArrayList<Object>();
		
		fields.add(ruleElementVo.getRuleName());
		fields.add(ruleElementVo.getElementSeq());
		fields.add(ruleElementVo.getDataName());
		fields.add(ruleElementVo.getHeaderName());
		fields.add(ruleElementVo.getCriteria());
		fields.add(ruleElementVo.getCaseSensitive());
		fields.add(ruleElementVo.getTargetText());
		fields.add(ruleElementVo.getTargetProc());
		fields.add(ruleElementVo.getExclusions());
		fields.add(ruleElementVo.getExclListProc());
		fields.add(ruleElementVo.getDelimiter());
		fields.add(ruleElementVo.getRowId());
		
		String sql =
			"update Rule_Element set " +
				"RuleName=?, " +
				"ElementSeq=?, " +
				"DataName=?, " +
				"HeaderName=?, " +
				"Criteria=?, " +
				"CaseSensitive=?, " +
				"TargetText=?, " +
				"TargetProc=?, " +
				"Exclusions=?, " +
				"ExclListProc=?, " +
				"Delimiter=? " +
			" where " +
				" RowId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"delete from Rule_Element where ruleName=? and elementSeq=?";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		fields.add(elementSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from Rule_Element where ruleName=?";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleElementVo ruleElementVo) {
		String sql = 
			"INSERT INTO Rule_Element (" +
			"RuleName, " +
			"ElementSeq, " +
			"DataName, " +
			"HeaderName, " +
			"Criteria, " +
			"CaseSensitive, " +
			"TargetText, " +
			"TargetProc, " +
			"Exclusions, " +
			"ExclListProc, " +
			"Delimiter " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				",?)";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleElementVo.getRuleName());
		fields.add(ruleElementVo.getElementSeq()+"");
		fields.add(ruleElementVo.getDataName());
		fields.add(ruleElementVo.getHeaderName());
		fields.add(ruleElementVo.getCriteria());
		fields.add(ruleElementVo.getCaseSensitive());
		fields.add(ruleElementVo.getTargetText());
		fields.add(ruleElementVo.getTargetProc());
		fields.add(ruleElementVo.getExclusions());
		fields.add(ruleElementVo.getExclListProc());
		fields.add(ruleElementVo.getDelimiter());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
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

	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
