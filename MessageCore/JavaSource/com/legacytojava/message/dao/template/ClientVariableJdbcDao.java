package com.legacytojava.message.dao.template;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.template.ClientVariableVo;

@Component("clientVariableDao")
public class ClientVariableJdbcDao implements ClientVariableDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private static final HashMap<String, List<?>> currentVariablesCache = new HashMap<String, List<?>>();
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class ClientVariableMapper implements RowMapper<ClientVariableVo> {
		
		public ClientVariableVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClientVariableVo clientVariableVo = new ClientVariableVo();
			
			clientVariableVo.setRowId(rs.getInt("RowId"));
			clientVariableVo.setClientId(rs.getString("ClientId"));
			clientVariableVo.setVariableName(rs.getString("VariableName"));
			clientVariableVo.setStartTime(rs.getTimestamp("StartTime"));
			clientVariableVo.setVariableValue(rs.getString("VariableValue"));
			clientVariableVo.setVariableFormat(rs.getString("VariableFormat"));
			clientVariableVo.setVariableType(rs.getString("VariableType"));
			clientVariableVo.setStatusId(rs.getString("StatusId"));
			clientVariableVo.setAllowOverride(rs.getString("AllowOverride"));
			clientVariableVo.setRequired(rs.getString("Required"));
			
			return clientVariableVo;
		}
	}

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
		
		List<?> list = getJdbcTemplate().query(sql, parms, new ClientVariableMapper());
		if (list.size()>0)
			return (ClientVariableVo)list.get(0);
		else
			return null;
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
		List<?> list = getJdbcTemplate().query(sql, parms, new ClientVariableMapper());
		if (list.size()>0)
			return (ClientVariableVo)list.get(0);
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
		List<ClientVariableVo> list = (List<ClientVariableVo>)getJdbcTemplate().query(sql, parms, new ClientVariableMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
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
					new Timestamp(new java.util.Date().getTime()), clientId};
			List<ClientVariableVo> list = (List<ClientVariableVo>)getJdbcTemplate().query(sql, parms, new ClientVariableMapper());
			currentVariablesCache.put(clientId, list);
		}
		
		List<ClientVariableVo> list = (List<ClientVariableVo>)currentVariablesCache.get(clientId);
		return list;
	}
	
	public int update(ClientVariableVo clientVariableVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientVariableVo.getClientId());
		fields.add(clientVariableVo.getVariableName());
		fields.add(clientVariableVo.getStartTime());
		fields.add(clientVariableVo.getVariableValue());
		fields.add(clientVariableVo.getVariableFormat());
		fields.add(clientVariableVo.getVariableType());
		fields.add(clientVariableVo.getStatusId());
		fields.add(clientVariableVo.getAllowOverride());
		fields.add(clientVariableVo.getRequired());
		fields.add(clientVariableVo.getRowId());
		
		String sql =
			"update ClientVariable set " +
				"ClientId=?, " +
				"VariableName=?, " +
				"StartTime=?, " +
				"VariableValue=?, " +
				"VariableFormat=?, " +
				"VariableType=?, " +
				"StatusId=?, " +
				"AllowOverride=?, " +
				"Required=? " +
			"where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
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
		String sql = 
			"INSERT INTO ClientVariable (" +
			"ClientId, " +
			"VariableName, " +
			"StartTime, " +
			"VariableValue, " +
			"VariableFormat, " +
			"VariableType, " +
			"StatusId, " +
			"AllowOverride, " +
			"Required " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? ,?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(clientVariableVo.getClientId());
		fields.add(clientVariableVo.getVariableName());
		fields.add(clientVariableVo.getStartTime());
		fields.add(clientVariableVo.getVariableValue());
		fields.add(clientVariableVo.getVariableFormat());
		fields.add(clientVariableVo.getVariableType());
		fields.add(clientVariableVo.getStatusId());
		fields.add(clientVariableVo.getAllowOverride());
		fields.add(clientVariableVo.getRequired());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		clientVariableVo.setRowId(retrieveRowId());
		if (rowsInserted>0)
			currentVariablesCache.remove(clientVariableVo.getClientId());
		return rowsInserted;
	}

	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
	
}
