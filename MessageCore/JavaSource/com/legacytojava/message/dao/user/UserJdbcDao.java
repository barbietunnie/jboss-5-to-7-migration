package com.legacytojava.message.dao.user;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.UserVo;

@Component("userDao")
public class UserJdbcDao extends AbstractDao implements UserDao {
	
	public UserVo getByPrimaryKey(String userId) {
		String sql = "select * from Users where UserId=?";
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
		String sql = "select * from Users where UserId=? and Password=?";
		Object[] parms = new Object[] {userId, password};
		try {
			UserVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<UserVo>(UserVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<UserVo> getAll(boolean onlyActive) {
		
		String sql = "select * from Users ";
		if (onlyActive) {
			sql += " where StatusId='" + StatusIdCode.ACTIVE + "'";
		}
		List<UserVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<UserVo>(UserVo.class));
		return list;
	}
	
	public int update(UserVo userVo) {
		if (userVo.getCreateTime()==null) {
			userVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(userVo);
		String sql = MetaDataUtil.buildUpdateStatement("Users", userVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
			userVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(userVo);
		String sql = MetaDataUtil.buildInsertStatement("Users", userVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		userVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
