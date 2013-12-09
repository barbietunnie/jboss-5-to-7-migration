package com.es.dao.user;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
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
			userVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(userVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("User_Data", userVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
			userVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(userVo);
		
		String sql = MetaDataUtil.buildInsertStatement("User_Data", userVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		userVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
}
