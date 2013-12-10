package com.es.dao.action;

import java.sql.Timestamp;
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
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.action.RuleActionVo;

@Component("ruleActionDao")
public class RuleActionDao extends AbstractDao {
	
	public List<RuleActionVo> getByRuleName(String ruleName) {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from Rule_Action a, Rule_Action_Detail b " +
			" where a.ActionId = b.ActionId and ruleName=? " +
			" order by actionSeq, senderId, startTime";
		
		Object[] parms = new Object[] {ruleName};
		List<RuleActionVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleActionVo>(RuleActionVo.class));
		return list;
	}
	
	public RuleActionVo getByPrimaryKey(int rowId) {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from Rule_Action a, Rule_Action_Detail b " +
			" where a.ActionId = b.ActionId and RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		try {
			RuleActionVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleActionVo>(RuleActionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleActionVo> getByBestMatch(String ruleName, Timestamp startTime, String senderId) {
		if (startTime == null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from Rule_Action a, Rule_Action_Detail b " +
				" where a.ActionId = b.ActionId " +
				" and RuleName=? and StartTime<=? and StatusId=? ";
		
		List<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(startTime);
		keys.add(StatusId.ACTIVE.getValue());
		if (senderId == null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and (senderId=? or senderId is null) ";
			keys.add(senderId);
		}
		sql += " order by actionSeq, senderId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<RuleActionVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleActionVo>(RuleActionVo.class));
		// remove duplicates
		list = removeDuplicates(list);
		return list;
	}
	
	private List<RuleActionVo> removeDuplicates(List<RuleActionVo> list) {
		int actionSeq = -1;
		List<RuleActionVo> listnew = new ArrayList<RuleActionVo>();
		for (int i = 0; i < list.size(); i++) {
			RuleActionVo vo = list.get(i);
			if (vo.getActionSeq() != actionSeq) {
				actionSeq = vo.getActionSeq();
				listnew.add(vo);
			}
		}
		return listnew;
	}

	public List<RuleActionVo> getAll() {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from Rule_Action a, Rule_Action_Detail b " +
			" where a.ActionId = b.ActionId " +
			" order by actionSeq";
		
		List<RuleActionVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<RuleActionVo>(RuleActionVo.class));
		return list;	
	}
	
	public RuleActionVo getByUniqueKey(String ruleName, int actionSeq, Timestamp startTime,
			String senderId) {
		List<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(actionSeq);
		keys.add(startTime);
		
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from Rule_Action a, Rule_Action_Detail b " +
			" where a.ActionId = b.ActionId " +
			" and ruleName=? and actionSeq=? and startTime=? ";
		
		if (senderId == null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and senderId=? ";
			keys.add(senderId);
		}
		try {
			RuleActionVo vo = getJdbcTemplate().queryForObject(sql, keys.toArray(), 
					new BeanPropertyRowMapper<RuleActionVo>(RuleActionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public RuleActionVo getMostCurrent(String ruleName, int actionSeq, String senderId) {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from Rule_Action a, Rule_Action_Detail b " +
				" where a.ActionId = b.ActionId " +
				" and RuleName=? and ActionSeq=? and StartTime<=? and StatusId=? ";
		
		List<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(actionSeq);
		keys.add(startTime);
		keys.add(StatusId.ACTIVE.getValue());
		if (senderId == null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and (senderId=? or senderId is null) ";
			keys.add(senderId);
		}
		sql += " order by senderId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<RuleActionVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleActionVo>(RuleActionVo.class));
		if (list.size() > 0) {
			return (RuleActionVo) list.get(0);
		}
		else {
			return null;
		}
	}
	
	public synchronized int update(RuleActionVo ruleActionVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleActionVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Rule_Action", ruleActionVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from Rule_Action where ruleName=?";
		
		Object[] parms = new Object[] {ruleName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from Rule_Action where rowId=?";
		
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByUniqueKey(String ruleName, int actionSeq, Timestamp startTime,
			String senderId) {
		List<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(actionSeq);
		keys.add(startTime);
		
		String sql = 
			"delete from Rule_Action " +
			" where ruleName=? and actionSeq=? and startTime=? ";
		
		if (senderId == null) {
			sql += " and senderId is null ";
		}
		else {
			sql += " and senderId=? ";
			keys.add(senderId);
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, keys.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleActionVo ruleActionVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleActionVo);

		String sql = MetaDataUtil.buildInsertStatement("Rule_Action", ruleActionVo);

		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleActionVo.setRowId(retrieveRowId());
		updateReloadFlags();
		return rowsInserted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateActionReloadFlag();
	}

	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
	
}
