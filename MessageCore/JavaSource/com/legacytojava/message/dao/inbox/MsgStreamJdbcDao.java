package com.legacytojava.message.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.vo.outbox.MsgStreamVo;

@Component("msgStreamDao")
public class MsgStreamJdbcDao extends AbstractDao implements MsgStreamDao {
	
	public MsgStreamVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"MsgStream where msgid=? ";
		
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
				"MsgStream where fromAddrId=? ";
		
		Object[] parms = new Object[] {fromAddrId+""};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	public MsgStreamVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"MsgStream where msgid = (select max(MsgId) from MsgStream) ";
		
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public int update(MsgStreamVo msgStreamVo) {
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		fields.add(msgStreamVo.getMsgId()+"");
		
		String sql =
			"update MsgStream set " +
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
			"delete from MsgStream where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgStreamVo msgStreamVo) {
		String sql = 
			"INSERT INTO MsgStream (" +
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
			msgStreamVo.setAddTime(new Timestamp(System.currentTimeMillis()));
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
}
