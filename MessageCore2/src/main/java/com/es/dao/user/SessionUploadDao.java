package com.es.dao.user;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.comm.SessionUploadVo;

@Component("sessionUploadDao")
public class SessionUploadDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public SessionUploadVo getByPrimaryKey(String sessionId, int sessionSeq) {
		String sql = "select * from Session_Upload where SessionId=? and sessionSeq=?";
		Object[] parms = new Object[] {sessionId, sessionSeq};
		try {
			SessionUploadVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SessionUploadVo>(SessionUploadVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SessionUploadVo> getBySessionId(String sessionId) {
		String sql = "select * from Session_Upload where SessionId=?";
		Object[] parms = new Object[] {sessionId};
		List<SessionUploadVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SessionUploadVo>(SessionUploadVo.class));
		return list;
	}
	
	/**
	 * SessionValue (blob) is not returned from this method. But
	 * SessionUploadVo.fileSize is populated with file size.
	 */
	public List<SessionUploadVo> getBySessionId4Web(String sessionId) {
		List<SessionUploadVo> list = getBySessionId(sessionId);
		for (int i = 0; i < list.size(); i++) {
			SessionUploadVo vo = list.get(i);
			if (vo.getSessionValue() != null) {
				vo.setFileSize(vo.getSessionValue().length);
				vo.setSessionValue(null);
			}
			else {
				vo.setFileSize(0);
			}
		}
		return list;
	}
	
	public List<SessionUploadVo> getByUserId(String userId) {
		String sql = "select * from Session_Upload where UserId=?";
		Object[] parms = new Object[] {userId};
		List<SessionUploadVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SessionUploadVo>(SessionUploadVo.class));
		return list;
	}
	
	public int update(SessionUploadVo sessVo) {
		Object[] parms = {
				sessVo.getFileName(),
				sessVo.getContentType(),
				sessVo.getUserId(),
				sessVo.getSessionValue(),
				sessVo.getSessionId(),
				sessVo.getSessionSeq()
				};
		
		String sql = "update Session_Upload set " +
			"FileName=?," +
			"ContentType=?," +
			"UserId=?," +
			"SessionValue=?" +
			" where SessionId=? and SessionSeq=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String sessionId, int sessionSeq) {
		String sql = "delete from Session_Upload where SessionId=? and SessionSeq=?";
		Object[] parms = new Object[] {sessionId, sessionSeq};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	public int deleteBySessionId(String sessionId) {
		String sql = "delete from Session_Upload where SessionId=?";
		Object[] parms = new Object[] {sessionId,};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByUserId(String userId) {
		String sql = "delete from Session_Upload where UserId=?";
		Object[] parms = new Object[] {userId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteExpired(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes); // roll back time
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		String sql = "delete from Session_Upload where CreateTime<?";
		Object[] parms = new Object[] {now};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;	
	}
	
	public int deleteAll() {
		String sql = "delete from Session_Upload";
		int rowsDeleted = getJdbcTemplate().update(sql);
		return rowsDeleted;	
	}

	public int insert(SessionUploadVo sessVo) {
		Object[] parms = {
				sessVo.getSessionId(),
				sessVo.getSessionSeq(),
				sessVo.getFileName(),
				sessVo.getContentType(),
				sessVo.getUserId(),
				sessVo.getSessionValue()
			};
		
		String sql = "INSERT INTO Session_Upload (" +
			"SessionId," +
			"SessionSeq," +
			"FileName," +
			"ContentType," +
			"UserId," +
			"CreateTime," +
			"SessionValue" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, current_timestamp, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		return rowsInserted;
	}
	
	public int insertLast(SessionUploadVo sessVo) {
		String lastSeq = "select max(SessionSeq) from Session_Upload where SessionId = '"
				+ sessVo.getSessionId() + "'";
		int sessSeq = getJdbcTemplate().queryForObject(lastSeq, Integer.class) + 1;
		Object[] parms = {
				sessVo.getSessionId(),
				sessSeq,
				sessVo.getFileName(),
				sessVo.getContentType(),
				sessVo.getUserId(),
				sessVo.getSessionValue()
			};
		
		String sql = "INSERT INTO Session_Upload (" +
			"SessionId," +
			"SessionSeq," +
			"FileName," +
			"ContentType," +
			"UserId," +
			"CreateTime," +
			"SessionValue" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, current_timestamp, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
