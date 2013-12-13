package com.legacytojava.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.inbox.AttachmentsVo;

@Component("attachmentsDao")
public class AttachmentsJdbcDao extends AbstractDao implements AttachmentsDao {

	public AttachmentsVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"Attachments where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",attchmntDepth+"",attchmntSeq+""};
		try {
			AttachmentsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<AttachmentsVo>(AttachmentsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<AttachmentsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Attachments where msgId=? " +
			" order by attchmntDepth, attchmntSeq";
		Object[] parms = new Object[] {msgId+""};
		List<AttachmentsVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<AttachmentsVo>(AttachmentsVo.class));
		return list;
	}
	
	public int update(AttachmentsVo attachmentsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(attachmentsVo);
		String sql = MetaDataUtil.buildUpdateStatement("Attachments", attachmentsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"delete from Attachments where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		fields.add(attchmntDepth+"");
		fields.add(attchmntSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Attachments where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(AttachmentsVo attachmentsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(attachmentsVo);
		String sql = MetaDataUtil.buildInsertStatement("Attachments", attachmentsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
