package com.legacytojava.message.dao.user;

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
import com.legacytojava.message.vo.UserVo;

@Component("userDao")
public class UserJdbcDao implements UserDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class UserMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			UserVo userVo = new UserVo();
			
			userVo.setRowId(rs.getInt("RowId"));
			userVo.setUserId(rs.getString("UserId"));
			userVo.setPassword(rs.getString("Password"));
			userVo.setSessionId(rs.getString("SessionId"));
			userVo.setFirstName(rs.getString("FirstName"));
			userVo.setLastName(rs.getString("LastName"));
			userVo.setMiddleInit(rs.getString("MiddleInit"));
			userVo.setCreateTime(rs.getTimestamp("CreateTime"));
			userVo.setLastVisitTime(rs.getTimestamp("LastVisitTime"));
			userVo.setHits(rs.getInt("Hits"));
			userVo.setStatusId(rs.getString("StatusId"));
			userVo.setRole(rs.getString("Role"));
			userVo.setEmailAddr(rs.getString("EmailAddr"));
			userVo.setDefaultFolder(rs.getString("DefaultFolder"));
			userVo.setDefaultRuleName(rs.getString("DefaultRuleName"));
			userVo.setDefaultToAddr(rs.getString("DefaultToAddr"));
			userVo.setClientId(rs.getString("ClientId"));
			
			return userVo;
		}
	}
	
	public UserVo getByPrimaryKey(String userId) {
		String sql = "select * from Users where UserId=?";
		Object[] parms = new Object[] {userId};
		List<?> list = getJdbcTemplate().query(sql, parms, new UserMapper());
		if (list.size()>0) {
			return (UserVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public UserVo getForLogin(String userId, String password) {
		String sql = "select * from Users where UserId=? and Password=?";
		Object[] parms = new Object[] {userId, password};
		List<?> list = getJdbcTemplate().query(sql, parms, new UserMapper());
		if (list.size()>0) {
			return (UserVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<UserVo> getAll(boolean onlyActive) {
		
		String sql = "select * from Users ";
		if (onlyActive) {
			sql += " where StatusId='" + StatusIdCode.ACTIVE + "'";
		}
		List<UserVo> list = getJdbcTemplate().query(sql, new UserMapper());
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
				userVo.getClientId(),
				userVo.getRowId()
				};
		
		String sql = "update Users set " +
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
			"ClientId=?" +
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
		
		String sql = "update Users set " +
			"SessionId=?," +
			"LastVisitTime=?," +
			"Hits=?" +
			" where RowId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String userId) {
		String sql = "delete from Users where UserId=?";
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
				userVo.getClientId()
			};
		
		String sql = "INSERT INTO Users (" +
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
			"ClientId" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				", ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		userVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
