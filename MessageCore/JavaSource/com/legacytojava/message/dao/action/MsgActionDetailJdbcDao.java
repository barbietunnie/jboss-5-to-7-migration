package com.legacytojava.message.dao.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.action.MsgActionDetailVo;

@Component("msgActionDetailDao")
public class MsgActionDetailJdbcDao extends AbstractDao implements MsgActionDetailDao {
	
	static final class MsgActionDetailMapper implements RowMapper<MsgActionDetailVo> {
		
		public MsgActionDetailVo mapRow(ResultSet rs, int rowNum) throws SQLException {
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
			"select *, UpdtTime as OrigUpdtTime " +
			"from " +
				"MsgActionDetail where actionId=? ";
		
		Object[] parms = new Object[] {actionId};
		try {
			MsgActionDetailVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgActionDetailVo>(MsgActionDetailVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgActionDetailVo getByPrimaryKey(int rowId) {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime " +
			"from " +
				"MsgActionDetail where RowId=? ";
		
		Object[] parms = new Object[] {rowId};
		
		try {
			MsgActionDetailVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgActionDetailVo>(MsgActionDetailVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgActionDetailVo> getAll() {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime " +
			" from " +
				" MsgActionDetail " +
			" order by actionId asc ";
		List<MsgActionDetailVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgActionDetailVo>(MsgActionDetailVo.class));
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
		msgActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionDetailVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgActionDetail", msgActionDetailVo);
		if (msgActionDetailVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		msgActionDetailVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionDetailVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgActionDetail", msgActionDetailVo);
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
