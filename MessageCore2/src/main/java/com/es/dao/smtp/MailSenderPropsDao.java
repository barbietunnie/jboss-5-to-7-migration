package com.es.dao.smtp;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.MailSenderPropsVo;

@Component("mailSenderPropsDao")
public class MailSenderPropsDao extends AbstractDao {
	
	public MailSenderPropsVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * from Mail_Sender_Props where rowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			MailSenderPropsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailSenderPropsVo>(MailSenderPropsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailSenderPropsVo> getAll() {
		
		String sql = "select * from Mail_Sender_Props ";
		List<MailSenderPropsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MailSenderPropsVo>(MailSenderPropsVo.class));
		return list;
	}
	
	public int update(MailSenderPropsVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailSenderVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Mail_Sender_Props", mailSenderVo);
		if (mailSenderVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from Mail_Sender_Props where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailSenderPropsVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailSenderVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Mail_Sender_Props", mailSenderVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailSenderVo.setRowId(retrieveRowId());
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsInserted;
	}
}
