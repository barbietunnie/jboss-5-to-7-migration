package com.legacytojava.message.dao.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.rule.RuleElementVo;

@Component("ruleElementDao")
public class RuleElementJdbcDao implements RuleElementDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	static final class RuleElementMapper implements RowMapper<RuleElementVo> {
		
		public RuleElementVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RuleElementVo ruleElementVo = new RuleElementVo();
			
			ruleElementVo.setRowId(rs.getInt("RowId"));
			ruleElementVo.setRuleName(rs.getString("RuleName"));
			ruleElementVo.setElementSeq(rs.getInt("ElementSeq"));
			ruleElementVo.setDataName(rs.getString("DataName"));
			ruleElementVo.setHeaderName(rs.getString("HeaderName"));
			ruleElementVo.setCriteria(rs.getString("Criteria"));
			ruleElementVo.setCaseSensitive(rs.getString("CaseSensitive"));
			ruleElementVo.setTargetText(rs.getString("TargetText"));
			ruleElementVo.setTargetProc(rs.getString("TargetProc"));
			ruleElementVo.setExclusions(rs.getString("Exclusions"));
			ruleElementVo.setExclListProc(rs.getString("ExclListProc"));
			ruleElementVo.setDelimiter(rs.getString("Delimiter"));
			
			return ruleElementVo;
		}
	}

	public RuleElementVo getByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"select * " +
			"from RuleElement " +
				" where ruleName=? and elementSeq=?";
		
		Object[] parms = new Object[] {ruleName, elementSeq};
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new RuleElementMapper());
		if (list.size()>0)
			return (RuleElementVo)list.get(0);
		else
			return null;
	}
	
	public List<RuleElementVo> getAll() {
		String sql = 
			"select * " +
			" from " +
				" RuleElement " +
			" order by ruleName asc, elementSeq asc ";
		List<RuleElementVo> list = (List<RuleElementVo>)getJdbcTemplate().query(sql, new RuleElementMapper());
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
		List<RuleElementVo> list = (List<RuleElementVo>)getJdbcTemplate().query(sql, parms, new RuleElementMapper());
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
			"update RuleElement set " +
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
		String sql = 
			"INSERT INTO RuleElement (" +
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
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
