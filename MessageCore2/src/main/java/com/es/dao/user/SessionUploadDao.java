package com.es.dao.user;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.SessionUploadVo;

@Component("sessionUploadDao")
public class SessionUploadDao extends AbstractDao {
	
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
		if (sessVo.getCreateTime() == null) {
			sessVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(sessVo);
		String sql = MetaDataUtil.buildUpdateStatement("Session_Upload", sessVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		sessVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(sessVo);
		String sql = MetaDataUtil.buildInsertStatement("Session_Upload", sessVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
	public int insertLast(SessionUploadVo sessVo) {
		String lastSeq = "select max(SessionSeq) from Session_Upload where SessionId = '"
				+ sessVo.getSessionId() + "'";
		int sessSeq = getJdbcTemplate().queryForObject(lastSeq, Integer.class) + 1;
		sessVo.setSessionSeq(sessSeq);
		return insert(sessVo);
	}
	
}
