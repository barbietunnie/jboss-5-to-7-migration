package com.es.dao.smtp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.data.constant.CodeType;
import com.es.data.constant.StatusId;
import com.es.vo.comm.SmtpServerVo;

@Component("smtpServerDao")
public class SmtpServerDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public SmtpServerVo getByPrimaryKey(String serverName) {
		String sql = "select * from Smtp_Server where ServerName=?";
		Object[] parms = new Object[] {serverName};
		try {
			SmtpServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SmtpServerVo> getAll(boolean onlyActive, Boolean isSecure) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from Smtp_Server ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		if (isSecure!=null) {
			keys.add(isSecure?CodeType.YES.getValue():CodeType.NO.getValue());
			if (sql.indexOf("where")>0) {
				sql += " and UseSsl=? ";
			}
			else {
				sql += " where UseSsl=? ";
			}
		}

		sql += " order by ServerName ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}
	
	public List<SmtpServerVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from Smtp_Server ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public List<SmtpServerVo> getByServerType(String serverType, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(serverType);
		String sql = "select * from Smtp_Server where ServerType=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by ServerName ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, keys.toArray(),
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}
	
	public List<SmtpServerVo> getBySslFlag(boolean useSSL, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(useSSL ? CodeType.YES.getValue() : CodeType.NO.getValue());
		String sql = "select * from Smtp_Server where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}

	public List<SmtpServerVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(useSSL ? CodeType.YES.getValue() : CodeType.NO.getValue());
		String sql = "select * from Smtp_Server where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId limit 1 ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}

	public int update(SmtpServerVo smtpConnVo) {
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
		
		String sql = "update Smtp_Server set " +
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
		String sql = "delete from Smtp_Server where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(SmtpServerVo smtpConnVo) {
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
		
		String sql = "INSERT INTO Smtp_Server (" +
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
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
