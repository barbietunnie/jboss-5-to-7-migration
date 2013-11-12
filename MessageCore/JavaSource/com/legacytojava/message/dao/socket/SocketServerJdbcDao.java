package com.legacytojava.message.dao.socket;

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

import com.legacytojava.message.vo.SocketServerVo;

@Component("socketServerDao")
public class SocketServerJdbcDao implements SocketServerDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class SocketMapper implements RowMapper<SocketServerVo> {
		
		public SocketServerVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			SocketServerVo socketServerVo = new SocketServerVo();
			
			socketServerVo.setRowId(rs.getInt("RowId"));
			socketServerVo.setServerName(rs.getString("ServerName"));
			socketServerVo.setSocketPort(rs.getInt("SocketPort"));
			socketServerVo.setInteractive(rs.getString("Interactive"));
			socketServerVo.setServerTimeout(rs.getInt("ServerTimeout"));
			socketServerVo.setSocketTimeout(rs.getInt("SocketTimeout"));
			socketServerVo.setTimeoutUnit(rs.getString("TimeoutUnit"));
			socketServerVo.setConnections(rs.getInt("Connections"));
			socketServerVo.setPriority(rs.getString("Priority"));
			socketServerVo.setStatusId(rs.getString("StatusId"));
			socketServerVo.setProcessorName(rs.getString("ProcessorName"));
			socketServerVo.setMessageCount(rs.getInt("MessageCount"));
			socketServerVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			socketServerVo.setUpdtUserId(rs.getString("UpdtUserId"));
			socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
			return socketServerVo;
		}
	}
	
	public SocketServerVo getByPrimaryKey(String serverName) {
		String sql = "select * from SocketServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		List<?> list = getJdbcTemplate().query(sql, parms, new SocketMapper());
		if (list.size()>0) {
			return (SocketServerVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<SocketServerVo> getAll(boolean onlyActive) {
		
		String sql = "select * from SocketServers ";
		if (onlyActive) {
			sql += " where StatusId='A'";
		}
		List<SocketServerVo> list = getJdbcTemplate().query(sql, new SocketMapper());
		return list;
	}
	
	public int update(SocketServerVo socketServerVo) {
		socketServerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(socketServerVo.getServerName());
		keys.add(socketServerVo.getSocketPort());
		keys.add(socketServerVo.getInteractive());
		keys.add(socketServerVo.getServerTimeout());
		keys.add(socketServerVo.getSocketTimeout());
		keys.add(socketServerVo.getTimeoutUnit());
		keys.add(socketServerVo.getConnections());
		keys.add(socketServerVo.getPriority());
		keys.add(socketServerVo.getStatusId());
		keys.add(socketServerVo.getProcessorName());
		keys.add(socketServerVo.getMessageCount());
		keys.add(socketServerVo.getUpdtTime());
		keys.add(socketServerVo.getUpdtUserId());
		keys.add(socketServerVo.getRowId());
		
		String sql = "update SocketServers set " +
			"ServerName=?," +
			"SocketPort=?," +
			"Interactive=?," +
			"ServerTimeout=?," +
			"SocketTimeout=?," +
			"TimeoutUnit=?," +
			"Connections=?," +
			"Priority=?," +
			"StatusId=?," +
			"ProcessorName=?," +
			"MessageCount=?," +
			"UpdtTime=?," +
			"UpdtUserId=?" +
			" where RowId=?";
		
		if (socketServerVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(socketServerVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from SocketServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(SocketServerVo socketServerVo) {
		socketServerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				socketServerVo.getServerName(),
				socketServerVo.getSocketPort(),
				socketServerVo.getInteractive(),
				socketServerVo.getServerTimeout(),
				socketServerVo.getSocketTimeout(),
				socketServerVo.getTimeoutUnit(),
				socketServerVo.getConnections(),
				socketServerVo.getPriority(),
				socketServerVo.getStatusId(),
				socketServerVo.getProcessorName(),
				socketServerVo.getMessageCount(),
				socketServerVo.getUpdtTime(),
				socketServerVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO SocketServers (" +
			"ServerName," +
			"SocketPort," +
			"Interactive," +
			"ServerTimeout," +
			"SocketTimeout," +
			"TimeoutUnit," +
			"Connections," +
			"Priority," +
			"StatusId," +
			"ProcessorName," +
			"MessageCount," +
			"UpdtTime," +
			"UpdtUserId" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		socketServerVo.setRowId(retrieveRowId());
		socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
