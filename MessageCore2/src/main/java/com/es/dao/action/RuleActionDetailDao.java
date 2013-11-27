package com.es.dao.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.dao.sender.ReloadFlagsDao;
import com.es.vo.action.RuleActionDetailVo;

@Component("msgActionDetailDao")
public class RuleActionDetailDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public RuleActionDetailVo getByActionId(String actionId) {
		String sql = 
			"select * " +
			"from " +
				"Rule_Action_Detail where actionId=? ";
		
		Object[] parms = new Object[] {actionId};
		
		List<RuleActionDetailVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleActionDetailVo>(RuleActionDetailVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
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
		msgActionDetailVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionDetailVo.getActionId());
		fields.add(msgActionDetailVo.getDescription());
		fields.add(msgActionDetailVo.getProcessBeanId());
		fields.add(msgActionDetailVo.getProcessClassName());
		fields.add(msgActionDetailVo.getDataType());
		fields.add(msgActionDetailVo.getUpdtTime());
		fields.add(msgActionDetailVo.getUpdtUserId());
		fields.add(msgActionDetailVo.getRowId());
		
		String sql =
			"update Rule_Action_Detail set " +
				"ActionId=?, " +
				"Description=?, " +
				"ProcessBeanId=?, " +
				"ProcessClassName=?, " +
				"DataType=?, " +
				"UpdtTime=?, " +
				"UpdtUserId=? " +
			" where " +
				" RowId=? ";
		
		if (msgActionDetailVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgActionDetailVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
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
				" ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		msgActionDetailVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionDetailVo.getActionId());
		fields.add(msgActionDetailVo.getDescription());
		fields.add(msgActionDetailVo.getProcessBeanId());
		fields.add(msgActionDetailVo.getProcessClassName());
		fields.add(msgActionDetailVo.getDataType());
		fields.add(msgActionDetailVo.getUpdtTime());
		fields.add(msgActionDetailVo.getUpdtUserId());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
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
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
