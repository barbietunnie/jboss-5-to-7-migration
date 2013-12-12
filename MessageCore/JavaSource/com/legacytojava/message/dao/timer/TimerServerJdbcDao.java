package com.legacytojava.message.dao.timer;

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
import com.legacytojava.message.vo.TimerServerVo;

@Component("timerServerDao")
public class TimerServerJdbcDao extends AbstractDao implements TimerServerDao {
	
	public TimerServerVo getByPrimaryKey(String serverName) {
		String sql = "select * from TimerServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		try {
			TimerServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<TimerServerVo>(TimerServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<TimerServerVo> getAll(boolean onlyActive) {
		
		String sql = "select * from TimerServers ";
		if (onlyActive) {
			sql += " where StatusId='" + StatusIdCode.ACTIVE + "'";
		}
		List<TimerServerVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<TimerServerVo>(TimerServerVo.class));
		return list;
	}
	
	public int update(TimerServerVo timerServerVo) {
		if (timerServerVo.getUpdtTime()==null) {
			timerServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(timerServerVo);
		String sql = MetaDataUtil.buildUpdateStatement("TimerServers", timerServerVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from TimerServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(TimerServerVo timerServerVo) {
		if (timerServerVo.getUpdtTime()==null) {
			timerServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(timerServerVo);
		String sql = MetaDataUtil.buildInsertStatement("TimerServers", timerServerVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		timerServerVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
