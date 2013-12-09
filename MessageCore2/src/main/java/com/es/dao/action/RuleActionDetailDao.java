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
	
	public synchronized int update(RuleActionDetailVo msgActionDetailVo) {
		msgActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionDetailVo);
		
		String sql =
			"update Rule_Action_Detail set " +
				"ActionId=:actionId, " +
				"Description=:description, " +
				"ProcessBeanId=:processBeanId, " +
				"ProcessClassName=:processClassName, " +
				"DataType=:dataType, " +
				"UpdtTime=:updtTime, " +
				"UpdtUserId=:updtUserId " +
			" where " +
				" RowId=:rowId ";
		
		if (msgActionDetailVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgActionDetailVo.setOrigUpdtTime(msgActionDetailVo.getUpdtTime());
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
	
	public synchronized int insert(RuleActionDetailVo msgActionDetailVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionDetailVo);
		String sql = 
			"INSERT INTO Rule_Action_Detail (" +
			"ActionId, " +
			"Description, " +
			"ProcessBeanId, " +
			"ProcessClassName, " +
			"DataType, " +
			"UpdtTime, " +
			"UpdtUserId " +
			") VALUES (" +
				" :actionId, :description, :processBeanId, :processClassName, :dataType, :updtTime, :updtUserId " +
				")";
		
		msgActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgActionDetailVo.setRowId(retrieveRowId());
		msgActionDetailVo.setOrigUpdtTime(msgActionDetailVo.getUpdtTime());
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
