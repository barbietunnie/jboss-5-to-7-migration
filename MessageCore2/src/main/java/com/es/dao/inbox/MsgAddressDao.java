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
import com.es.vo.inbox.MsgAddressVo;

@Component("msgAddressDao")
public class MsgAddressDao extends AbstractDao {
	
	public MsgAddressVo getByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Address where msgid=? and addrType=? and addrSeq=? ";
		
		Object[] parms = new Object[] {msgId,addrType,addrSeq};
		try {
			MsgAddressVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgAddressVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Address where msgId=? " +
			" order by addrType, addrSeq";
		Object[] parms = new Object[] {msgId};
		List<MsgAddressVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
		return list;
	}
	
	public List<MsgAddressVo> getByMsgIdAndType(long msgId, String addrType) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Address where msgId=? and addrType=? " +
			" order by addrSeq";
		Object[] parms = new Object[] {msgId,addrType};
		List<MsgAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
		return list;
	}
	
	public int update(MsgAddressVo msgAddrVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAddrVo);

		String sql = MetaDataUtil.buildUpdateStatement("Msg_Address", msgAddrVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"delete from Msg_Address where msgid=? and addrType=? and addrSeq=? ";
		
		List<String> fields = new ArrayList<String>();
		fields.add(String.valueOf(msgId));
		fields.add(addrType);
		fields.add(String.valueOf(addrSeq));
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Address where msgid=? ";
		
		List<String> fields = new ArrayList<String>();
		fields.add(String.valueOf(msgId));
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgAddressVo msgAddrVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAddrVo);

		String sql = MetaDataUtil.buildInsertStatement("Msg_Address", msgAddrVo);

		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
