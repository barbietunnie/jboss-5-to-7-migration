package com.es.dao.smtp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.MailServerType;
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.SmtpServerVo;

@Component("smtpServerDao")
public class SmtpServerDao extends AbstractDao {
	
	public SmtpServerVo getByPrimaryKey(String serverName) {
		String sql = "select * from Smtp_Server where ServerName=?";
		Object[] parms = new Object[] {serverName};
		try {
			SmtpServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SmtpServerVo> getAll(boolean onlyActive, Boolean isSecure) {
		List<String> params = new ArrayList<String>();
		String sql = "select * from Smtp_Server ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			params.add(StatusId.ACTIVE.getValue());
		}
		if (isSecure!=null) {
			params.add(isSecure?CodeType.YES.getValue():CodeType.NO.getValue());
			if (sql.indexOf("where")>0) {
				sql += " and UseSsl=? ";
			}
			else {
				sql += " where UseSsl=? ";
			}
		}

		sql += " order by ServerName ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, params.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}
	
	public List<SmtpServerVo> getAllForTrial(boolean onlyActive) {
		List<String> params = new ArrayList<String>();
		String sql = "select * from Smtp_Server ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			params.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, params.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public List<SmtpServerVo> getByServerType(MailServerType serverType, boolean onlyActive) {
		List<String> params = new ArrayList<String>();
		params.add(serverType.getValue());
		String sql = "select * from Smtp_Server where ServerType=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			params.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by ServerName ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, params.toArray(),
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}
	
	public List<SmtpServerVo> getBySslFlag(boolean useSSL, boolean onlyActive) {
		List<String> params = new ArrayList<String>();
		params.add(useSSL ? CodeType.YES.getValue() : CodeType.NO.getValue());
		String sql = "select * from Smtp_Server where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			params.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, params.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}

	public List<SmtpServerVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive) {
		List<String> params = new ArrayList<String>();
		params.add(useSSL ? CodeType.YES.getValue() : CodeType.NO.getValue());
		String sql = "select * from Smtp_Server where UseSsl=?";
		if (onlyActive) {
			sql += " and StatusId=? ";
			params.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId limit 1 ";
		List<SmtpServerVo> list = getJdbcTemplate().query(sql, params.toArray(), 
				new BeanPropertyRowMapper<SmtpServerVo>(SmtpServerVo.class));
		return list;
	}

	public int update(SmtpServerVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(smtpConnVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Smtp_Server", smtpConnVo);
		if (smtpConnVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from Smtp_Server where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(SmtpServerVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(smtpConnVo);

		String sql = MetaDataUtil.buildInsertStatement("Smtp_Server", smtpConnVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		smtpConnVo.setRowId(retrieveRowId());
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsInserted;
	}
	
}
