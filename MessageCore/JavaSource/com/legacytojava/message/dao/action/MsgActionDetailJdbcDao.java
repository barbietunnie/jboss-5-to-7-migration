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

import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.action.MsgActionDetailVo;

@Component("msgActionDetailDao")
public class MsgActionDetailJdbcDao implements MsgActionDetailDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	static final class MsgActionDetailMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgActionDetailVo msgActionDetailVo = new MsgActionDetailVo();
			
			msgActionDetailVo.setRowId(rs.getInt("RowId"));
			msgActionDetailVo.setActionId(rs.getString("ActionId"));
			msgActionDetailVo.setDescription(rs.getString("Description"));
			msgActionDetailVo.setProcessBeanId(rs.getString("ProcessBeanId"));
			msgActionDetailVo.setProcessClassName(rs.getString("ProcessClassName"));
			msgActionDetailVo.setDataType(rs.getString("DataType"));
			msgActionDetailVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			msgActionDetailVo.setUpdtUserId(rs.getString("UpdtUserId"));
			msgActionDetailVo.setOrigUpdtTime(msgActionDetailVo.getUpdtTime());
			return msgActionDetailVo;
		}
	}

	public MsgActionDetailVo getByActionId(String actionId) {
		String sql = 
			"select * " +
			"from " +
				"MsgActionDetail where actionId=? ";
		
		Object[] parms = new Object[] {actionId};
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgActionDetailMapper());
		if (list.size()>0)
			return (MsgActionDetailVo)list.get(0);
		else
			return null;
	}
	
	public MsgActionDetailVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * " +
			"from " +
				"MsgActionDetail where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgActionDetailMapper());
		if (list.size()>0)
			return (MsgActionDetailVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgActionDetailVo> getAll() {
		String sql = 
			"select * " +
			" from " +
				" MsgActionDetail " +
			" order by actionId asc ";
		List<MsgActionDetailVo> list = (List<MsgActionDetailVo>)getJdbcTemplate().query(sql, new MsgActionDetailMapper());
		return list;
	}
	
	public List<String> getActionIds() {
		String sql = 
			"select distinct(ActionId) from MsgActionDetail " +
			" order by ActionId";
		
		List<String> list = (List<String>)getJdbcTemplate().queryForList(sql, String.class);
		return list;
	}
	
	public synchronized int update(MsgActionDetailVo msgActionDetailVo) {
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
			"update MsgActionDetail set " +
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
			"delete from MsgActionDetail where actionId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(actionId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from MsgActionDetail where RowId=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(rowId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(MsgActionDetailVo msgActionDetailVo) {
		String sql = 
			"INSERT INTO MsgActionDetail (" +
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
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
