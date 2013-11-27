package com.es.dao.user;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.data.constant.StatusId;
import com.es.vo.comm.UserVo;

@Component("userDao")
public class UserDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public UserVo getByPrimaryKey(String userId) {
		String sql = "select * from User_Data where UserId=?";
		Object[] parms = new Object[] {userId};
		try {
			UserVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<UserVo>(UserVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public UserVo getForLogin(String userId, String password) {
		String sql = "select * from User_Data where UserId=? and Password=?";
		Object[] parms = new Object[] {userId, password};
		List<UserVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<UserVo>(UserVo.class));
		if (list.size()>0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<UserVo> getAll(boolean onlyActive) {
		
		String sql = "select * from User_Data ";
		if (onlyActive) {
			sql += " where StatusId='" + StatusId.ACTIVE.getValue() + "'";
		}
		List<UserVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<UserVo>(UserVo.class));
		return list;
	}
	
	public int update(UserVo userVo) {
		if (userVo.getCreateTime()==null) {
			userVo.setCreateTime(new Timestamp(new java.util.Date().getTime()));
		}
		Object[] parms = {
				userVo.getUserId(),
				userVo.getPassword(),
				userVo.getSessionId(),
				userVo.getFirstName(),
				userVo.getLastName(),
				userVo.getMiddleInit(),
				userVo.getCreateTime(),
				userVo.getLastVisitTime(),
				userVo.getHits(),
				userVo.getStatusId(),
				userVo.getRole(),
				userVo.getEmailAddr(),
				userVo.getDefaultFolder(),
				userVo.getDefaultRuleName(),
				userVo.getDefaultToAddr(),
				userVo.getSenderId(),
				userVo.getRowId()
				};
		
		String sql = "update User_Data set " +
			"UserId=?," +
			"Password=?," +
			"SessionId=?," +
			"FirstName=?," +
			"LastName=?," +
			"MiddleInit=?," +
			"CreateTime=?," +
			"LastVisitTime=?," +
			"Hits=?," +
			"StatusId=?," +
			"Role=?," +
			"EmailAddr=?," +
			"DefaultFolder=?," +
			"DefaultRuleName=?," +
			"DefaultToAddr=?," +
			"SenderId=?" +
			" where RowId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int update4Web(UserVo userVo) {
		Object[] parms = {
				userVo.getSessionId(),
				userVo.getLastVisitTime(),
				userVo.getHits(),
				userVo.getRowId()
				};
		
		String sql = "update User_Data set " +
			"SessionId=?," +
			"LastVisitTime=?," +
			"Hits=?" +
			" where RowId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String userId) {
		String sql = "delete from User_Data where UserId=?";
		Object[] parms = new Object[] {userId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(UserVo userVo) {
		if (userVo.getCreateTime()==null) {
			userVo.setCreateTime(new Timestamp(new java.util.Date().getTime()));
		}
		Object[] parms = {
				userVo.getUserId(),
				userVo.getPassword(),
				userVo.getSessionId(),
				userVo.getFirstName(),
				userVo.getLastName(),
				userVo.getMiddleInit(),
				userVo.getCreateTime(),
				userVo.getLastVisitTime(),
				userVo.getHits(),
				userVo.getStatusId(),
				userVo.getRole(),
				userVo.getEmailAddr(),
				userVo.getDefaultFolder(),
				userVo.getDefaultRuleName(),
				userVo.getDefaultToAddr(),
				userVo.getSenderId()
			};
		
		String sql = "INSERT INTO User_Data (" +
			"UserId," +
			"Password," +
			"SessionId," +
			"FirstName," +
			"LastName," +
			"MiddleInit," +
			"CreateTime," +
			"LastVisitTime," +
			"Hits," +
			"StatusId," +
			"Role," +
			"EmailAddr," +
			"DefaultFolder," +
			"DefaultRuleName," +
			"DefaultToAddr," +
			"SenderId" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				", ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		userVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
