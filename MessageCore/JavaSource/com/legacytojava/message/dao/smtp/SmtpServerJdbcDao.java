package com.legacytojava.message.dao.smtp;

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

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.SmtpConnVo;

@Component("smtpServerDao")
public class SmtpServerJdbcDao implements SmtpServerDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class SmtpServerMapper implements RowMapper<SmtpConnVo> {
		
		public SmtpConnVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			SmtpConnVo smtpConnVo = new SmtpConnVo();
			
			smtpConnVo.setRowId(rs.getInt("RowId"));
			smtpConnVo.setSmtpHost(rs.getString("SmtpHost"));
			smtpConnVo.setSmtpPort(rs.getInt("SmtpPort"));
			smtpConnVo.setServerName(rs.getString("ServerName"));
			smtpConnVo.setDescription(rs.getString("Description"));
			smtpConnVo.setUseSsl(rs.getString("UseSsl"));
			smtpConnVo.setUseAuth(rs.getString("UseAuth"));
			smtpConnVo.setUserId(rs.getString("UserId"));
			smtpConnVo.setUserPswd(rs.getString("UserPswd"));
			smtpConnVo.setPersistence(rs.getString("Persistence"));
			smtpConnVo.setStatusId(rs.getString("StatusId"));
			smtpConnVo.setServerType(rs.getString("ServerType"));
			smtpConnVo.setThreads(rs.getInt("Threads"));
			smtpConnVo.setRetries(rs.getInt("Retries"));
			smtpConnVo.setRetryFreq(rs.getInt("RetryFreq"));
			smtpConnVo.setAlertAfter((Integer)rs.getObject("AlertAfter"));
			smtpConnVo.setAlertLevel(rs.getString("AlertLevel"));
			smtpConnVo.setMessageCount(rs.getInt("MessageCount"));
			smtpConnVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			smtpConnVo.setUpdtUserId(rs.getString("UpdtUserId"));
			smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
			return smtpConnVo;
		}
	}
	
	public SmtpConnVo getByPrimaryKey(String serverName) {
		String sql = "select * from SmtpServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		List<?> list = getJdbcTemplate().query(sql, parms, new SmtpServerMapper());
		if (list.size()>0) {
			return (SmtpConnVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<SmtpConnVo> getAll(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from SmtpServers ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by ServerName ";
		List<SmtpConnVo> list = (List<SmtpConnVo>)getJdbcTemplate().query(sql, keys.toArray(), new SmtpServerMapper());
		return list;
	}
	
	public List<SmtpConnVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from SmtpServers ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by RowId limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<SmtpConnVo> list = (List<SmtpConnVo>)getJdbcTemplate().query(sql, keys.toArray(), new SmtpServerMapper());
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public List<SmtpConnVo> getByServerType(String serverType, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(serverType);
		String sql = "select * from SmtpServers where ServerType=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by ServerName ";
		List<SmtpConnVo> list = (List<SmtpConnVo>)getJdbcTemplate().query(sql, keys.toArray(), new SmtpServerMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<SmtpConnVo> getBySslFlag(boolean useSSL, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(useSSL ? Constants.YES : Constants.NO);
		String sql = "select * from SmtpServers where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by RowId ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), new SmtpServerMapper());
		return (List<SmtpConnVo>) list;
	}

	@SuppressWarnings("unchecked")
	public List<SmtpConnVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(useSSL ? Constants.YES : Constants.NO);
		String sql = "select * from SmtpServers where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by RowId limit 1 ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), new SmtpServerMapper());
		return (List<SmtpConnVo>) list;
	}

	public int update(SmtpConnVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(smtpConnVo.getServerName());
		keys.add(smtpConnVo.getSmtpHost());
		keys.add(smtpConnVo.getSmtpPort());
		keys.add(smtpConnVo.getDescription());
		keys.add(smtpConnVo.getUseSsl());
		keys.add(smtpConnVo.getUseAuth());
		keys.add(smtpConnVo.getUserId());
		keys.add(smtpConnVo.getUserPswd());
		keys.add(smtpConnVo.getPersistence());
		keys.add(smtpConnVo.getStatusId());
		keys.add(smtpConnVo.getServerType());
		keys.add(smtpConnVo.getThreads());
		keys.add(smtpConnVo.getRetries());
		keys.add(smtpConnVo.getRetryFreq());
		keys.add(smtpConnVo.getAlertAfter());
		keys.add(smtpConnVo.getAlertLevel());
		keys.add(smtpConnVo.getMessageCount());
		keys.add(smtpConnVo.getUpdtTime());
		keys.add(smtpConnVo.getUpdtUserId());
		keys.add(smtpConnVo.getRowId());
		
		String sql = "update SmtpServers set " +
			"ServerName=?," +
			"SmtpHost=?," +
			"SmtpPort=?," +
			"Description=?," +
			"UseSsl=?," +
			"UseAuth=?," +
			"UserId=?," +
			"UserPswd=?," +
			"Persistence=?," +
			"StatusId=?," +
			"ServerType=?," +
			"Threads=?," +
			"Retries=?," +
			"RetryFreq=?," +
			"AlertAfter=?," +
			"AlertLevel=?," +
			"MessageCount=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where RowId=?";
		
		if (smtpConnVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(smtpConnVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from SmtpServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(SmtpConnVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				smtpConnVo.getSmtpHost(),
				smtpConnVo.getSmtpPort(),
				smtpConnVo.getServerName(),
				smtpConnVo.getDescription(),
				smtpConnVo.getUseSsl(),
				smtpConnVo.getUseAuth(),
				smtpConnVo.getUserId(),
				smtpConnVo.getUserPswd(),
				smtpConnVo.getPersistence(),
				smtpConnVo.getStatusId(),
				smtpConnVo.getServerType(),
				smtpConnVo.getThreads(),
				smtpConnVo.getRetries(),
				smtpConnVo.getRetryFreq(),
				smtpConnVo.getAlertAfter(),
				smtpConnVo.getAlertLevel(),
				smtpConnVo.getMessageCount(),
				smtpConnVo.getUpdtTime(),
				smtpConnVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO SmtpServers (" +
			"SmtpHost," +
			"SmtpPort," +
			"ServerName," +
			"Description," +
			"UseSsl," +
			"UseAuth," +
			"UserId," +
			"UserPswd," +
			"Persistence," +
			"StatusId," +
			"ServerType," +
			"Threads," +
			"Retries," +
			"RetryFreq," +
			"AlertAfter," +
			"AlertLevel," +
			"MessageCount," +
			"UpdtTime," +
			"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		smtpConnVo.setRowId(retrieveRowId());
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
