package com.es.dao.smtp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.comm.MailSenderPropsVo;

@Component("mailSenderPropsDao")
public class MailSenderPropsDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

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
		mailSenderVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(mailSenderVo.getInternalLoopback());
		keys.add(mailSenderVo.getExternalLoopback());
		keys.add(mailSenderVo.getUseTestAddr());
		keys.add(mailSenderVo.getTestFromAddr());
		keys.add(mailSenderVo.getTestToAddr());
		keys.add(mailSenderVo.getTestReplytoAddr());
		keys.add(mailSenderVo.getIsVerpEnabled());
		keys.add(mailSenderVo.getUpdtTime());
		keys.add(mailSenderVo.getUpdtUserId());
		keys.add(mailSenderVo.getRowId());
		
		String sql = "update Mail_Sender_Props set " +
			"InternalLoopback=?," +
			"ExternalLoopback=?," +
			"UseTestAddr=?," +
			"TestFromAddr=?," +
			"TestToAddr=?," +
			"TestReplytoAddr=?," +
			"IsVerpEnabled=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where RowId=?";
		
		if (mailSenderVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(mailSenderVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
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
		mailSenderVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				mailSenderVo.getInternalLoopback(),
				mailSenderVo.getExternalLoopback(),
				mailSenderVo.getUseTestAddr(),
				mailSenderVo.getTestFromAddr(),
				mailSenderVo.getTestToAddr(),
				mailSenderVo.getTestReplytoAddr(),
				mailSenderVo.getIsVerpEnabled(),
				mailSenderVo.getUpdtTime(),
				mailSenderVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO Mail_Sender_Props (" +
			"InternalLoopback," +
			"ExternalLoopback," +
			"UseTestAddr," +
			"TestFromAddr," +
			"TestToAddr," +
			"TestReplytoAddr," +
			"IsVerpEnabled," +
			"UpdtTime," +
			"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		mailSenderVo.setRowId(retrieveRowId());
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
