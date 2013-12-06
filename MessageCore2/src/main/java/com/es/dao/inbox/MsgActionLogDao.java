package com.es.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.inbox.MsgActionLogVo;

@Component("msgActionLogDao")
public class MsgActionLogDao extends AbstractDao {

	public MsgActionLogVo getByPrimaryKey(long msgId, int actionSeq) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Action_Log where msgId=? and actionSeq=? ";
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		fields.add(actionSeq);
		
		try {
			MsgActionLogVo vo = getJdbcTemplate().queryForObject(sql, fields.toArray(),
					new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgActionLogVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Action_Log where msgId=? " +
			" order by actionSeq ";
		Object[] parms = new Object[] {msgId};
		List<MsgActionLogVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
		return list;
	}
	
	public int update(MsgActionLogVo msgActionLogsVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionLogsVo.getActionBo());
		fields.add(msgActionLogsVo.getParameters());
		fields.add(msgActionLogsVo.getMsgId());
		fields.add(msgActionLogsVo.getActionSeq());
		
		String sql =
			"update Msg_Action_Log set " +
				"ActionBo=?, " +
				"Parameters=? " +
			" where " +
				" MsgId=? and ActionSeq=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, int actionSeq) {
		String sql = 
			"delete from Msg_Action_Log where msgId=? and actionSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		fields.add(actionSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Action_Log where msgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgActionLogVo msgActionLogsVo) {
		Timestamp addTime = new Timestamp(System.currentTimeMillis());
		msgActionLogsVo.setAddTime(addTime);
		msgActionLogsVo.setActionSeq(getNextActionSeq(msgActionLogsVo.getMsgId()));
		
		String sql = 
			"INSERT INTO Msg_Action_Log (" +
			"MsgId, " +
			"ActionSeq, " +
			"AddTime, " +
			"ActionBo, " +
			"Parameters " +
			") VALUES (" +
				" ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgActionLogsVo.getMsgId());
		fields.add(msgActionLogsVo.getActionSeq());
		fields.add(msgActionLogsVo.getAddTime());
		fields.add(msgActionLogsVo.getActionBo());
		fields.add(msgActionLogsVo.getParameters());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	private int getNextActionSeq(long msgId) {
		String sql = 
			"select max(actionSeq) " +
			" from " +
				" Msg_Action_Log where msgId=? ";
		Object[] parms = new Object[] {msgId};
		Integer seq = getJdbcTemplate().queryForObject(sql, parms, Integer.class);
		if (seq == null) {
			return 1;
		}
		return (seq+1);
	}

}
