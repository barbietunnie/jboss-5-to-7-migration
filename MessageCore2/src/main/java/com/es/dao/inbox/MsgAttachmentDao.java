package com.es.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.inbox.MsgAttachmentVo;

@Component("msgAttachmentDao")
public class MsgAttachmentDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MsgAttachmentVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Attachment where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",attchmntDepth+"",attchmntSeq+""};
		try {
			MsgAttachmentVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgAttachmentVo>(MsgAttachmentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgAttachmentVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Attachment where msgId=? " +
			" order by attchmntDepth, attchmntSeq";
		Object[] parms = new Object[] {msgId+""};
		List<MsgAttachmentVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAttachmentVo>(MsgAttachmentVo.class));
		return list;
	}
	
	public int update(MsgAttachmentVo msgAttachmentVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgAttachmentVo.getAttchmntName());
		fields.add(msgAttachmentVo.getAttchmntType());
		fields.add(msgAttachmentVo.getAttchmntDisp());
		fields.add(msgAttachmentVo.getAttchmntValue());
		fields.add(msgAttachmentVo.getMsgId()+"");
		fields.add(msgAttachmentVo.getAttchmntDepth()+"");
		fields.add(msgAttachmentVo.getAttchmntSeq()+"");
		
		String sql =
			"update Msg_Attachment set " +
				"AttchmntName=?, " +
				"AttchmntType=?, " +
				"AttchmntDisp=?, " +
				"AttchmntValue=? " +
			" where " +
				" msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"delete from Msg_Attachment where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		fields.add(attchmntDepth+"");
		fields.add(attchmntSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Attachment where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgAttachmentVo msgAttachmentVo) {
		String sql = 
			"INSERT INTO Msg_Attachment (" +
			"MsgId, " +
			"AttchmntDepth, " +
			"AttchmntSeq, " +
			"AttchmntName, " +
			"AttchmntType, " +
			"AttchmntDisp, " +
			"AttchmntValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ? ,?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgAttachmentVo.getMsgId()+"");
		fields.add(msgAttachmentVo.getAttchmntDepth()+"");
		fields.add(msgAttachmentVo.getAttchmntSeq()+"");
		fields.add(msgAttachmentVo.getAttchmntName());
		fields.add(msgAttachmentVo.getAttchmntType());
		fields.add(msgAttachmentVo.getAttchmntDisp());
		fields.add(msgAttachmentVo.getAttchmntValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
