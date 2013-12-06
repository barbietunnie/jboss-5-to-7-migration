package com.es.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.inbox.MsgRfcFieldVo;

@Component("msgRfcFieldDao")
public class MsgRfcFieldDao extends AbstractDao {
	
	public MsgRfcFieldVo getByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rfc_Field where msgid=? and rfcType=? ";
		
		Object[] parms = new Object[] {msgId+"",rfcType};
		try {
			MsgRfcFieldVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgRfcFieldVo>(MsgRfcFieldVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgRfcFieldVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Rfc_Field where msgId=? " +
			" order by rfcType";
		Object[] parms = new Object[] {msgId+""};
		List<MsgRfcFieldVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgRfcFieldVo>(MsgRfcFieldVo.class));
		return list;
	}
	
	public int update(MsgRfcFieldVo rfcFieldsVo) {

		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(rfcFieldsVo.getRfcStatus());
		fields.add(rfcFieldsVo.getRfcAction());
		fields.add(rfcFieldsVo.getFinalRcpt());
		fields.add(rfcFieldsVo.getFinalRcptId());
		fields.add(rfcFieldsVo.getOrigRcpt());
		fields.add(rfcFieldsVo.getOrigMsgSubject());
		fields.add(rfcFieldsVo.getMessageId());
		fields.add(rfcFieldsVo.getDsnText());
		fields.add(rfcFieldsVo.getDsnRfc822());
		fields.add(rfcFieldsVo.getDlvrStatus());
		fields.add(rfcFieldsVo.getMsgId()+"");
		fields.add(rfcFieldsVo.getRfcType());
		
		String sql =
			"update Msg_Rfc_Field set " +
				"RfcStatus=?, " +
				"RfcAction=?, " +
				"FinalRcpt=?, " +
				"FinalRcptId=?, " +
				"OrigRcpt=?, " +
				"OrigMsgSubject=?, " +
				"MessageId=?, " +
				"DsnText=?, " +
				"DsnRfc822=?, " +
				"DlvrStatus=? " +
			" where " +
				" msgid=? and rfcType=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"delete from Msg_Rfc_Field where msgid=? and rfcType=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		fields.add(rfcType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Rfc_Field where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgRfcFieldVo rfcFieldsVo) {
		String sql = 
			"INSERT INTO Msg_Rfc_Field (" +
				"MsgId, " +
				"RfcType, " +
				"RfcStatus, " +
				"RfcAction, " +
				"FinalRcpt, " +
				"FinalRcptId, " +
				"OrigRcpt, " +
				"OrigMsgSubject, " +
				"MessageId, " +
				"DsnText, " +
				"DsnRfc822, " +
				"DlvrStatus " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?)";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(rfcFieldsVo.getMsgId()+"");
		fields.add(rfcFieldsVo.getRfcType());
		fields.add(rfcFieldsVo.getRfcStatus());
		fields.add(rfcFieldsVo.getRfcAction());
		fields.add(rfcFieldsVo.getFinalRcpt());
		fields.add(rfcFieldsVo.getFinalRcptId());
		fields.add(rfcFieldsVo.getOrigRcpt());
		fields.add(rfcFieldsVo.getOrigMsgSubject());
		fields.add(rfcFieldsVo.getMessageId());
		fields.add(rfcFieldsVo.getDsnText());
		fields.add(rfcFieldsVo.getDsnRfc822());
		fields.add(rfcFieldsVo.getDlvrStatus());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
}
