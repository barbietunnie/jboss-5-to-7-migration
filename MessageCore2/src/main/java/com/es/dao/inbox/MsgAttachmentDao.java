package com.es.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.inbox.MsgAttachmentVo;

@Component("msgAttachmentDao")
public class MsgAttachmentDao extends AbstractDao {

	public MsgAttachmentVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Attachment where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {msgId,attchmntDepth,attchmntSeq};
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
		Object[] parms = new Object[] {msgId};
		List<MsgAttachmentVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAttachmentVo>(MsgAttachmentVo.class));
		return list;
	}
	
	public int update(MsgAttachmentVo msgAttachmentVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAttachmentVo);

		String sql = MetaDataUtil.buildUpdateStatement("Msg_Attachment", msgAttachmentVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"delete from Msg_Attachment where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		fields.add(attchmntDepth);
		fields.add(attchmntSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Attachment where msgid=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgAttachmentVo msgAttachmentVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAttachmentVo);

		String sql = MetaDataUtil.buildInsertStatement("Msg_Attachment", msgAttachmentVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
