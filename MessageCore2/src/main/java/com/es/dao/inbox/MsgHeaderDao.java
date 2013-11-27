package com.es.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.inbox.MsgHeaderVo;

@Component("msgHeaderDao")
public class MsgHeaderDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MsgHeaderVo getByPrimaryKey(long msgId, int headerSeq) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Header where msgid=? and headerSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",headerSeq+""};
		try {
			MsgHeaderVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgHeaderVo>(MsgHeaderVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgHeaderVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Header where msgId=? " +
			" order by headerSeq";
		Object[] parms = new Object[] {msgId+""};
		List<MsgHeaderVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgHeaderVo>(MsgHeaderVo.class));
		return list;
	}
	
	public int update(MsgHeaderVo msgHeadersVo) {
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(StringUtils.left(msgHeadersVo.getHeaderName(), 100));
		fields.add(msgHeadersVo.getHeaderValue());
		fields.add(msgHeadersVo.getMsgId()+"");
		fields.add(msgHeadersVo.getHeaderSeq()+"");
		
		String sql =
			"update Msg_Header set " +
				"HeaderName=?, " +
				"HeaderValue=? " +
			" where " +
				" msgid=? and headerSeq=?  ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, int headerSeq) {
		String sql = 
			"delete from Msg_Header where msgid=? and headerSeq=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(headerSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Header where msgid=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgHeaderVo msgHeadersVo) {
		String sql = 
			"INSERT INTO Msg_Header (" +
			"MsgId, " +
			"HeaderSeq, " +
			"HeaderName, " +
			"HeaderValue " +
			") VALUES (" +
				" ?, ?, ?, ? " +
				")";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgHeadersVo.getMsgId()+"");
		fields.add(msgHeadersVo.getHeaderSeq()+"");
		fields.add(StringUtils.left(msgHeadersVo.getHeaderName(), 100));
		fields.add(msgHeadersVo.getHeaderValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}