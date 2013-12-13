package com.legacytojava.message.dao.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.action.MsgActionVo;

@Component("msgActionDao")
public class MsgActionJdbcDao extends AbstractDao implements MsgActionDao {
	
	public List<MsgActionVo> getByRuleName(String ruleName) {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
			" where a.ActionId = b.ActionId and ruleName=? " +
			" order by actionSeq, clientId, startTime";
		
		Object[] parms = new Object[] {ruleName};
		List<MsgActionVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionVo>(MsgActionVo.class));
		return list;
	}
	
	public MsgActionVo getByPrimaryKey(int rowId) {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
			" where a.ActionId = b.ActionId and RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		try {
			MsgActionVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgActionVo>(MsgActionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgActionVo> getByBestMatch(String ruleName, Timestamp startTime, String clientId) {
		if (startTime == null)
			startTime = new Timestamp(new java.util.Date().getTime());
		
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
				" where a.ActionId = b.ActionId " +
				" and RuleName=? and StartTime<=? and StatusId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(startTime);
		keys.add(StatusIdCode.ACTIVE);
		if (clientId == null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and (clientId=? or clientId is null) ";
			keys.add(clientId);
		}
		sql += " order by actionSeq, clientId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<MsgActionVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionVo>(MsgActionVo.class));
		// remove duplicates
		list = removeDuplicates(list);
		return list;
	}
	
	private List<MsgActionVo> removeDuplicates(List<MsgActionVo> list) {
		int actionSeq = -1;
		List<MsgActionVo> listnew = new ArrayList<MsgActionVo>();
		for (int i = 0; i < list.size(); i++) {
			MsgActionVo vo = list.get(i);
			if (vo.getActionSeq() != actionSeq) {
				actionSeq = vo.getActionSeq();
				listnew.add(vo);
			}
		}
		return listnew;
	}

	public List<MsgActionVo> getAll() {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
			" where a.ActionId = b.ActionId " +
			" order by actionSeq";
		
		List<MsgActionVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgActionVo>(MsgActionVo.class));
		return list;	
	}
	
	public MsgActionVo getByUniqueKey(String ruleName, int actionSeq, Timestamp startTime,
			String clientId) {
		List<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(actionSeq);
		keys.add(startTime);
		
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
			" where a.ActionId = b.ActionId " +
			" and ruleName=? and actionSeq=? and startTime=? ";
		
		if (clientId == null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and clientId=? ";
			keys.add(clientId);
		}
		try {
			MsgActionVo vo = getJdbcTemplate().queryForObject(sql, keys.toArray(), 
					new BeanPropertyRowMapper<MsgActionVo>(MsgActionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgActionVo getMostCurrent(String ruleName, int actionSeq, String clientId) {
		Timestamp startTime = new Timestamp(new java.util.Date().getTime());
		
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
				" where a.ActionId = b.ActionId " +
				" and RuleName=? and ActionSeq=? and StartTime<=? and StatusId=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(actionSeq);
		keys.add(startTime);
		keys.add(StatusIdCode.ACTIVE);
		if (clientId == null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and (clientId=? or clientId is null) ";
			keys.add(clientId);
		}
		sql += " order by clientId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<MsgActionVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionVo>(MsgActionVo.class));
		if (list.size() > 0)
			return list.get(0);
		else
			return null;
	}
	
	public synchronized int update(MsgActionVo msgActionVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgAction", msgActionVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpadted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from MsgAction where ruleName=?";
		
		Object[] parms = new Object[] {ruleName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from MsgAction where rowId=?";
		
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByUniqueKey(String ruleName, int actionSeq, Timestamp startTime,
			String clientId) {
		List<Object> keys = new ArrayList<Object>();
		keys.add(ruleName);
		keys.add(actionSeq);
		keys.add(startTime);
		
		String sql = 
			"delete from MsgAction " +
			" where ruleName=? and actionSeq=? and startTime=? ";
		
		if (clientId == null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and clientId=? ";
			keys.add(clientId);
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, keys.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(MsgActionVo msgActionVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgAction", msgActionVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgActionVo.setRowId(retrieveRowId());
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
