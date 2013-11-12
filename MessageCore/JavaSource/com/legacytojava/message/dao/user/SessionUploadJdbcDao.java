package com.legacytojava.message.dao.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.SessionUploadVo;

@Component("sessionUploadDao")
public class SessionUploadJdbcDao implements SessionUploadDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class SessionUploadMapper implements RowMapper<SessionUploadVo> {
		
		public SessionUploadVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			SessionUploadVo sessVo = new SessionUploadVo();
			
			sessVo.setSessionId(rs.getString("SessionId"));
			sessVo.setSessionSeq(rs.getInt("SessionSeq"));
			sessVo.setFileName(rs.getString("FileName"));
			sessVo.setContentType(rs.getString("ContentType"));
			sessVo.setUserId(rs.getString("UserId"));
			sessVo.setCreateTime(rs.getTimestamp("CreateTime"));
			sessVo.setSessionValue(rs.getBytes("SessionValue"));
			
			return sessVo;
		}
	}
	
	public SessionUploadVo getByPrimaryKey(String sessionId, int sessionSeq) {
		String sql = "select * from SessionUploads where SessionId=? and sessionSeq=?";
		Object[] parms = new Object[] {sessionId, sessionSeq};
		List<?> list = getJdbcTemplate().query(sql, parms, new SessionUploadMapper());
		if (list.size()>0) {
			return (SessionUploadVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<SessionUploadVo> getBySessionId(String sessionId) {
		String sql = "select * from SessionUploads where SessionId=?";
		Object[] parms = new Object[] {sessionId};
		List<SessionUploadVo> list = getJdbcTemplate().query(sql, parms, new SessionUploadMapper());
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
		String sql = "select * from SessionUploads where UserId=?";
		Object[] parms = new Object[] {userId};
		List<SessionUploadVo> list = getJdbcTemplate().query(sql, parms, new SessionUploadMapper());
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
		
		String sql = "update SessionUploads set " +
			"FileName=?," +
			"ContentType=?," +
			"UserId=?," +
			"SessionValue=?" +
			" where SessionId=? and SessionSeq=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String sessionId, int sessionSeq) {
		String sql = "delete from SessionUploads where SessionId=? and SessionSeq=?";
		Object[] parms = new Object[] {sessionId, sessionSeq};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	public int deleteBySessionId(String sessionId) {
		String sql = "delete from SessionUploads where SessionId=?";
		Object[] parms = new Object[] {sessionId,};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByUserId(String userId) {
		String sql = "delete from SessionUploads where UserId=?";
		Object[] parms = new Object[] {userId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteExpired(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes); // roll back time
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		String sql = "delete from SessionUploads where CreateTime<?";
		Object[] parms = new Object[] {now};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;	
	}
	
	public int deleteAll() {
		String sql = "delete from SessionUploads";
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
		
		String sql = "INSERT INTO SessionUploads (" +
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
		String lastSeq = "select max(SessionSeq) from SessionUploads where SessionId = '"
				+ sessVo.getSessionId() + "'";
		int sessSeq = getJdbcTemplate().queryForInt(lastSeq) + 1;
		Object[] parms = {
				sessVo.getSessionId(),
				sessSeq,
				sessVo.getFileName(),
				sessVo.getContentType(),
				sessVo.getUserId(),
				sessVo.getSessionValue()
			};
		
		String sql = "INSERT INTO SessionUploads (" +
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
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
