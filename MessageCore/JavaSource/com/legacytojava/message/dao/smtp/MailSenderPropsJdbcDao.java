package com.legacytojava.message.dao.smtp;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.MailSenderVo;

@Component("mailSenderPropsDao")
public class MailSenderPropsJdbcDao extends AbstractDao implements MailSenderPropsDao {
	
	public MailSenderVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * from MailSenderProps where rowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			MailSenderVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailSenderVo>(MailSenderVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailSenderVo> getAll() {
		
		String sql = "select * from MailSenderProps ";
		List<MailSenderVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MailSenderVo>(MailSenderVo.class));
		return list;
	}
	
	public int update(MailSenderVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailSenderVo);
		String sql = MetaDataUtil.buildUpdateStatement("MailSenderProps", mailSenderVo);
		if (mailSenderVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from MailSenderProps where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailSenderVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailSenderVo);
		String sql = MetaDataUtil.buildInsertStatement("MailSenderProps", mailSenderVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailSenderVo.setRowId(retrieveRowId());
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsInserted;
	}
}
