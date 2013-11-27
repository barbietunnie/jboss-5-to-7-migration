package com.es.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.outbox.MsgStreamVo;

@Component("msgStreamDao")
public class MsgStreamDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MsgStreamVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Stream where msgid=? ";
		
		Object[] parms = new Object[] {msgId+""};
		try {
			MsgStreamVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgStreamVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Stream where fromAddrId=? ";
		
		Object[] parms = new Object[] {fromAddrId+""};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	public MsgStreamVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Stream where msgid = (select max(MsgId) from Msg_Stream) ";
		
		MsgStreamVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return vo;
	}
	
	public int update(MsgStreamVo msgStreamVo) {
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		fields.add(msgStreamVo.getMsgId()+"");
		
		String sql =
			"update Msg_Stream set " +
				"FromAddrId=?, " +
				"ToAddrId=?, " +
				"MsgSubject=?, " +
				"AddTime=?, " +
				"MsgStream=? " +
			" where " +
				" msgid=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from Msg_Stream where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgStreamVo msgStreamVo) {
		String sql = 
			"INSERT INTO Msg_Stream (" +
				"MsgId, " +
				"FromAddrId, " +
				"ToAddrId, " +
				"MsgSubject, " +
				"AddTime, " +
				"MsgStream " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgStreamVo.getMsgId()+"");
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
