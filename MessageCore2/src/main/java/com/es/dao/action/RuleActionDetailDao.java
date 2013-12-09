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
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.action.RuleActionDetailVo;

@Component("msgActionDetailDao")
public class RuleActionDetailDao extends AbstractDao {
	
	public RuleActionDetailVo getByActionId(String actionId) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Action_Detail where actionId=? ";
		
		Object[] parms = new Object[] {actionId};
		try {
			RuleActionDetailVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleActionDetailVo>(RuleActionDetailVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public RuleActionDetailVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Action_Detail where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		
		try {
			RuleActionDetailVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleActionDetailVo>(RuleActionDetailVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleActionDetailVo> getAll() {
		String sql = 
			"select * " +
			" from " +
				" Rule_Action_Detail " +
			" order by actionId asc ";
		List<RuleActionDetailVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<RuleActionDetailVo>(RuleActionDetailVo.class));
		return list;
	}
	
	public List<String> getActionIds() {
		String sql = 
			"select distinct(ActionId) from Rule_Action_Detail " +
			" order by ActionId";
		
		List<String> list = (List<String>)getJdbcTemplate().queryForList(sql, String.class);
		return list;
	}
	
	public synchronized int update(RuleActionDetailVo ruleActionDetailVo) {
		ruleActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleActionDetailVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Rule_Action_Detail", ruleActionDetailVo);
		
		if (ruleActionDetailVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleActionDetailVo.setOrigUpdtTime(ruleActionDetailVo.getUpdtTime());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByActionId(String actionId) {
		String sql = 
			"delete from Rule_Action_Detail where actionId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(actionId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from Rule_Action_Detail where RowId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(rowId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(RuleActionDetailVo ruleActionDetailVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleActionDetailVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Rule_Action_Detail", ruleActionDetailVo);
		
		ruleActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleActionDetailVo.setRowId(retrieveRowId());
		ruleActionDetailVo.setOrigUpdtTime(ruleActionDetailVo.getUpdtTime());
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
