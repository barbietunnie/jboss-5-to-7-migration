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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(rfcFieldsVo);
		String sql = MetaDataUtil.buildUpdateStatement("Msg_Rfc_Field", rfcFieldsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"delete from Msg_Rfc_Field where msgid=? and rfcType=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		fields.add(rfcType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Rfc_Field where msgid=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgRfcFieldVo rfcFieldsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(rfcFieldsVo);
		String sql = MetaDataUtil.buildInsertStatement("Msg_Rfc_Field", rfcFieldsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
