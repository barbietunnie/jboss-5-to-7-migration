package com.es.dao.user;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.StatusId;
import com.es.vo.comm.UserDataVo;

@Component("userDataDao")
public class UserDataDao extends AbstractDao {
	
	public UserDataVo getByPrimaryKey(String userId) {
		String sql = "select * from User_Data where UserId=?";
		Object[] parms = new Object[] {userId};
		try {
			UserDataVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<UserDataVo>(UserDataVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public UserDataVo getForLogin(String userId, String password) {
		String sql = "select * from User_Data where UserId=? and Password=?";
		Object[] parms = new Object[] {userId, password};
		List<UserDataVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<UserDataVo>(UserDataVo.class));
		if (list.size()>0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<UserDataVo> getAll(boolean onlyActive) {
		
		String sql = "select * from User_Data ";
		if (onlyActive) {
			sql += " where StatusId='" + StatusId.ACTIVE.getValue() + "'";
		}
		List<UserDataVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<UserDataVo>(UserDataVo.class));
		return list;
	}
	
	public int update(UserDataVo userVo) {
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
	
	public int update4Web(UserDataVo userVo) {
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
	
	public int insert(UserDataVo userVo) {
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
	
}
