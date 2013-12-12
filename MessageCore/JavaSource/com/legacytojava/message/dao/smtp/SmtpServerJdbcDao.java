package com.legacytojava.message.dao.smtp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.SmtpConnVo;

@Component("smtpServerDao")
public class SmtpServerJdbcDao extends AbstractDao implements SmtpServerDao {
	
	public SmtpConnVo getByPrimaryKey(String serverName) {
		String sql = "select * from SmtpServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		try {
			SmtpConnVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SmtpConnVo> getAll(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from SmtpServers ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by ServerName ";
		List<SmtpConnVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return list;
	}
	
	public List<SmtpConnVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from SmtpServers ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by RowId limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<SmtpConnVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public List<SmtpConnVo> getByServerType(String serverType, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(serverType);
		String sql = "select * from SmtpServers where ServerType=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by ServerName ";
		List<SmtpConnVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<SmtpConnVo> getBySslFlag(boolean useSSL, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(useSSL ? Constants.YES : Constants.NO);
		String sql = "select * from SmtpServers where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by RowId ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return (List<SmtpConnVo>) list;
	}

	@SuppressWarnings("unchecked")
	public List<SmtpConnVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		keys.add(useSSL ? Constants.YES : Constants.NO);
		String sql = "select * from SmtpServers where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			keys.add(StatusIdCode.ACTIVE);
		}
		sql += " order by RowId limit 1 ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return (List<SmtpConnVo>) list;
	}

	public int update(SmtpConnVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(smtpConnVo);
		String sql = MetaDataUtil.buildUpdateStatement("SmtpServers", smtpConnVo);
		if (smtpConnVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from SmtpServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(SmtpConnVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(smtpConnVo);
		String sql = MetaDataUtil.buildInsertStatement("SmtpServers", smtpConnVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		smtpConnVo.setRowId(retrieveRowId());
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsInserted;
	}
}
