package com.legacytojava.message.dao.timer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.TimerServerVo;

@Component("timerServerDao")
public class TimerServerJdbcDao implements TimerServerDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class TimerMapper implements RowMapper<TimerServerVo> {
		
		public TimerServerVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			TimerServerVo timerServerVo = new TimerServerVo();
			
			timerServerVo.setRowId(rs.getInt("RowId"));
			timerServerVo.setServerName(rs.getString("ServerName"));
			timerServerVo.setTimerInterval(rs.getInt("TimerInterval"));
			timerServerVo.setTimerIntervalUnit(rs.getString("TimerIntervalUnit"));
			timerServerVo.setInitialDelay(rs.getInt("InitialDelay"));
			timerServerVo.setStartTime(rs.getTimestamp("StartTime"));
			timerServerVo.setThreads(rs.getInt("Threads"));
			timerServerVo.setStatusId(rs.getString("StatusId"));
			timerServerVo.setProcessorName(rs.getString("ProcessorName"));
			timerServerVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			timerServerVo.setUpdtUserId(rs.getString("UpdtUserId"));
			
			return timerServerVo;
		}
	}
	
	public TimerServerVo getByPrimaryKey(String serverName) {
		String sql = "select * from TimerServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		List<?> list = getJdbcTemplate().query(sql, parms, new TimerMapper());
		if (list.size()>0) {
			return (TimerServerVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<TimerServerVo> getAll(boolean onlyActive) {
		
		String sql = "select * from TimerServers ";
		if (onlyActive) {
			sql += " where StatusId='" + StatusIdCode.ACTIVE + "'";
		}
		List<TimerServerVo> list = (List<TimerServerVo>)getJdbcTemplate().query(sql, new TimerMapper());
		return list;
	}
	
	public int update(TimerServerVo timerServerVo) {
		if (timerServerVo.getUpdtTime()==null) {
			timerServerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		}
		Object[] parms = {
				timerServerVo.getServerName(),
				timerServerVo.getTimerInterval(),
				timerServerVo.getTimerIntervalUnit(),
				timerServerVo.getInitialDelay(),
				timerServerVo.getStartTime(),
				timerServerVo.getThreads(),
				timerServerVo.getStatusId(),
				timerServerVo.getProcessorName(),
				timerServerVo.getUpdtTime(),
				timerServerVo.getUpdtUserId(),
				timerServerVo.getRowId()
				};
		
		String sql = "update TimerServers set " +
			"ServerName=?," +
			"TimerInterval=?," +
			"TimerIntervalUnit=?," +
			"InitialDelay=?," +
			"StartTime=?," +
			"Threads=?," +
			"StatusId=?," +
			"ProcessorName=?," +
			"UpdtTime=?," +
			"UpdtUserId=?" +
			" where RowId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from TimerServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(TimerServerVo timerServerVo) {
		if (timerServerVo.getUpdtTime()==null) {
			timerServerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		}
		Object[] parms = {
				timerServerVo.getServerName(),
				timerServerVo.getTimerInterval(),
				timerServerVo.getTimerIntervalUnit(),
				timerServerVo.getInitialDelay(),
				timerServerVo.getStartTime(),
				timerServerVo.getThreads(),
				timerServerVo.getStatusId(),
				timerServerVo.getProcessorName(),
				timerServerVo.getUpdtTime(),
				timerServerVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO TimerServers (" +
			"ServerName," +
			"TimerInterval," +
			"TimerIntervalUnit," +
			"InitialDelay," +
			"StartTime," +
			"Threads," +
			"StatusId," +
			"ProcessorName," +
			"UpdtTime," +
			"UpdtUserId" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		timerServerVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
