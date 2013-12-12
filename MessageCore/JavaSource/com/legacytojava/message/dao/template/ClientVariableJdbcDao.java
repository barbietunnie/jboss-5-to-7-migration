package com.legacytojava.message.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.template.ClientVariableVo;

@Component("clientVariableDao")
public class ClientVariableJdbcDao extends AbstractDao implements ClientVariableDao {
	
	private static final HashMap<String, List<ClientVariableVo>> 
		currentVariablesCache = new HashMap<String, List<ClientVariableVo>>();
	
	public ClientVariableVo getByPrimaryKey(String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"ClientVariable where clientId=? and variableName=? ";
		
		Object[] parms;
		if (startTime!=null) {
			sql += " and startTime=? ";
			parms = new Object[] {clientId,variableName,startTime};
		}
		else {
			sql += " and startTime is null ";
			parms = new Object[] {clientId,variableName};
		}
		try {
			ClientVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public ClientVariableVo getByBestMatch(String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"ClientVariable where clientId=? and variableName=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(clientId);
		keys.add(variableName);
		if (startTime!=null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		sql += " order by startTime desc ";
		
		Object[] parms = keys.toArray();
		List<ClientVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public List<ClientVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" ClientVariable where variableName=? " +
			" order by clientId, startTime asc ";
		Object[] parms = new Object[] {variableName};
		List<ClientVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
		return list;
	}
	
	public List<ClientVariableVo> getCurrentByClientId(String clientId) {
		if (!currentVariablesCache.containsKey(clientId)) {
			String sql = 
				"select * " +
					" from ClientVariable a " +
					" inner join ( " +
					"  select b.clientid, b.variablename, max(b.starttime) as maxtime " +
					"   from ClientVariable b " +
					"   where b.statusid=? and b.starttime<=? " +
					"    and b.clientid=? " +
					"   group by b.variablename " +
					" ) as c " +
					"  on a.variablename=c.variablename and a.starttime=c.maxtime " +
					"    and a.clientid=c.clientid " +
					" order by a.rowId asc ";
			Object[] parms = new Object[] {StatusIdCode.ACTIVE,
					new Timestamp(System.currentTimeMillis()), clientId};
			List<ClientVariableVo> list = getJdbcTemplate().query(sql, parms, 
					new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
			currentVariablesCache.put(clientId, list);
		}
		
		List<ClientVariableVo> list = currentVariablesCache.get(clientId);
		return list;
	}
	
	public int update(ClientVariableVo clientVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("ClientVariable", clientVariableVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted>0)
			currentVariablesCache.remove(clientVariableVo.getClientId());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"delete from ClientVariable where clientId=? and variableName=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientId);
		fields.add(variableName);
		if (startTime!=null) {
			sql += " and startTime=? ";
			fields.add(startTime);
		}
		else {
			sql += " and startTime is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.remove(clientId);
		return rowsDeleted;
	}
	
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from ClientVariable where variableName=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.clear();
		return rowsDeleted;
	}
	
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from ClientVariable where clientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.remove(clientId);
		return rowsDeleted;
	}
	
	public int insert(ClientVariableVo clientVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("ClientVariable", clientVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		clientVariableVo.setRowId(retrieveRowId());
		if (rowsInserted>0)
			currentVariablesCache.remove(clientVariableVo.getClientId());
		return rowsInserted;
	}
	
}
