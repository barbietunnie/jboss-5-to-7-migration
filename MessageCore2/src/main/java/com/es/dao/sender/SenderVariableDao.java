package com.es.dao.sender;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.StatusId;
import com.es.vo.template.SenderVariableVo;

@Component("senderVariableDao")
public class SenderVariableDao extends AbstractDao {
	
	private static final Map<String, List<SenderVariableVo>> currentVariablesCache = new HashMap<String, List<SenderVariableVo>>();
	
	public SenderVariableVo getByPrimaryKey(String senderId, String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"Sender_Variable where senderId=? and variableName=? ";
		
		Object[] parms;
		if (startTime!=null) {
			sql += " and startTime=? ";
			parms = new Object[] {senderId,variableName,startTime};
		}
		else {
			sql += " and startTime is null ";
			parms = new Object[] {senderId,variableName};
		}
		try {
			SenderVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SenderVariableVo>(SenderVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public SenderVariableVo getByBestMatch(String senderId, String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"Sender_Variable where senderId=? and variableName=? ";
		
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(senderId);
		keys.add(variableName);
		if (startTime!=null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		sql += " order by startTime desc ";
		
		Object[] parms = keys.toArray();
		List<SenderVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SenderVariableVo>(SenderVariableVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
	}
	
	public List<SenderVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" Sender_Variable where variableName=? " +
			" order by senderId, startTime asc ";
		Object[] parms = new Object[] {variableName};
		List<SenderVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SenderVariableVo>(SenderVariableVo.class));
		return list;
	}
	
	public List<SenderVariableVo> getCurrentBySenderId(String senderId) {
		if (!currentVariablesCache.containsKey(senderId)) {
			String sql = 
				"select * " +
					" from Sender_Variable a " +
					" inner join ( " +
					"  select b.senderid, b.variablename, max(b.starttime) as maxtime " +
					"   from Sender_Variable b " +
					"   where b.statusid=? and b.starttime<=? " +
					"    and b.senderid=? " +
					"   group by b.variablename " +
					" ) as c " +
					"  on a.variablename=c.variablename and a.starttime=c.maxtime " +
					"    and a.senderid=c.senderid " +
					" order by a.rowId asc ";
			Object[] parms = new Object[] {StatusId.ACTIVE.getValue(),
					new Timestamp(new java.util.Date().getTime()), senderId};
			List<SenderVariableVo> list = getJdbcTemplate().query(sql, parms,
					new BeanPropertyRowMapper<SenderVariableVo>(SenderVariableVo.class));
			currentVariablesCache.put(senderId, list);
		}
		
		List<SenderVariableVo> list = currentVariablesCache.get(senderId);
		return list;
	}
	
	public int update(SenderVariableVo senderVariableVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(senderVariableVo.getSenderId());
		fields.add(senderVariableVo.getVariableName());
		fields.add(senderVariableVo.getStartTime());
		fields.add(senderVariableVo.getVariableValue());
		fields.add(senderVariableVo.getVariableFormat());
		fields.add(senderVariableVo.getVariableType());
		fields.add(senderVariableVo.getStatusId());
		fields.add(senderVariableVo.getAllowOverride());
		fields.add(senderVariableVo.getRequired());
		fields.add(senderVariableVo.getRowId());
		
		String sql =
			"update Sender_Variable set " +
				"SenderId=?, " +
				"VariableName=?, " +
				"StartTime=?, " +
				"VariableValue=?, " +
				"VariableFormat=?, " +
				"VariableType=?, " +
				"StatusId=?, " +
				"AllowOverride=?, " +
				"Required=? " +
			"where " +
				" RowId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted>0)
			currentVariablesCache.remove(senderVariableVo.getSenderId());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String senderId, String variableName, Timestamp startTime) {
		String sql = 
			"delete from Sender_Variable where senderId=? and variableName=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(senderId);
		fields.add(variableName);
		if (startTime!=null) {
			sql += " and startTime=? ";
			fields.add(startTime);
		}
		else {
			sql += " and startTime is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.remove(senderId);
		return rowsDeleted;
	}
	
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from Sender_Variable where variableName=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.clear();
		return rowsDeleted;
	}
	
	public int deleteBySenderId(String senderId) {
		String sql = 
			"delete from Sender_Variable where senderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(senderId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0)
			currentVariablesCache.remove(senderId);
		return rowsDeleted;
	}
	
	public int insert(SenderVariableVo senderVariableVo) {
		String sql = 
			"INSERT INTO Sender_Variable (" +
			"SenderId, " +
			"VariableName, " +
			"StartTime, " +
			"VariableValue, " +
			"VariableFormat, " +
			"VariableType, " +
			"StatusId, " +
			"AllowOverride, " +
			"Required " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? ,?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(senderVariableVo.getSenderId());
		fields.add(senderVariableVo.getVariableName());
		fields.add(senderVariableVo.getStartTime());
		fields.add(senderVariableVo.getVariableValue());
		fields.add(senderVariableVo.getVariableFormat());
		fields.add(senderVariableVo.getVariableType());
		fields.add(senderVariableVo.getStatusId());
		fields.add(senderVariableVo.getAllowOverride());
		fields.add(senderVariableVo.getRequired());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		senderVariableVo.setRowId(retrieveRowId());
		if (rowsInserted>0)
			currentVariablesCache.remove(senderVariableVo.getSenderId());
		return rowsInserted;
	}

}
