package com.legacytojava.message.dao.action;

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

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.action.MsgActionVo;

@Component("msgActionDao")
public class MsgActionJdbcDao implements MsgActionDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	static final class MsgActionMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgActionVo msgActionVo = new MsgActionVo();
			
			msgActionVo.setRowId(rs.getInt("RowId"));
			msgActionVo.setRuleName(rs.getString("RuleName"));
			msgActionVo.setActionSeq(rs.getInt("ActionSeq"));
			msgActionVo.setStartTime(rs.getTimestamp("StartTime"));
			msgActionVo.setClientId(rs.getString("ClientId"));
			msgActionVo.setActionId(rs.getString("ActionId"));
			msgActionVo.setStatusId(rs.getString("StatusId"));
			msgActionVo.setDataTypeValues(rs.getString("DataTypeValues"));
			
			msgActionVo.setProcessBeanId(rs.getString("ProcessBeanId"));
			msgActionVo.setProcessClassName(rs.getString("ProcessClassName"));
			msgActionVo.setDataType(rs.getString("DataType"));
			return msgActionVo;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgActionVo> getByRuleName(String ruleName) {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
			" where a.ActionId = b.ActionId and ruleName=? " +
			" order by actionSeq, clientId, startTime";
		
		Object[] parms = new Object[] {ruleName};
		List<MsgActionVo> list = (List<MsgActionVo>)getJdbcTemplate().query(sql, parms, new MsgActionMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public MsgActionVo getByPrimaryKey(int rowId) {
		String sql = 
			"select a.*, b.ProcessBeanId, b.ProcessClassName, b.DataType " +
			" from MsgAction a, MsgActionDetail b " +
			" where a.ActionId = b.ActionId and RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		List<MsgActionVo> list = (List<MsgActionVo>)getJdbcTemplate().query(sql, parms, new MsgActionMapper());
		if (list != null && list.size() > 0)
			return (MsgActionVo) list.get(0);
		else
			return null;
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
		@SuppressWarnings("unchecked")
		List<MsgActionVo> list = (List<MsgActionVo>)getJdbcTemplate().query(sql, parms, new MsgActionMapper());
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
		
		@SuppressWarnings("unchecked")
		List<MsgActionVo> list = (List<MsgActionVo>)getJdbcTemplate().query(sql, new MsgActionMapper());
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
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), new MsgActionMapper());
		if (list.size()>0)
			return (MsgActionVo)list.get(0);
		else
			return null;
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
		@SuppressWarnings("unchecked")
		List<MsgActionVo> list =  (List<MsgActionVo>)getJdbcTemplate().query(sql, parms, new MsgActionMapper());
		if (list.size() > 0)
			return (MsgActionVo) list.get(0);
		else
			return null;
	}
	
	public synchronized int update(MsgActionVo msgActionVo) {
		Object[] parms = {
				msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(),
				msgActionVo.getStartTime(),
				msgActionVo.getClientId(),
				msgActionVo.getActionId(),
				msgActionVo.getStatusId(),
				msgActionVo.getDataTypeValues(),
				msgActionVo.getRowId()
				};
		
		String sql = "update MsgAction set " +
			"ruleName=?, " +
			"actionSeq=?, " +
			"startTime=?, " +
			"clientId=?, " +
			"actionId=?, " +
			"statusId=?, " +
			"dataTypeValues=? " +
			" where rowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
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
		Object[] parms = {
				msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(),
				msgActionVo.getStartTime(),
				msgActionVo.getClientId(),
				msgActionVo.getActionId(),
				msgActionVo.getStatusId(),
				msgActionVo.getDataTypeValues()
			};
		
		String sql = 
			"INSERT INTO MsgAction ( " +
			"ruleName, " +
			"actionSeq," +
			"startTime," +
			"clientId," +
			"actionId," +
			"statusId," +
			"dataTypeValues " +
			") VALUES (?, ?, ?, ?, ?, ?, ?)";
		int rowsInserted = getJdbcTemplate().update(sql, parms);
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
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
