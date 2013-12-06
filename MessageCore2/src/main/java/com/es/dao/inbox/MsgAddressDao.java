package com.es.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.inbox.MsgAddressVo;

@Component("msgAddressDao")
public class MsgAddressDao extends AbstractDao {
	
	public MsgAddressVo getByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Address where msgid=? and addrType=? and addrSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",addrType,addrSeq+""};
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
		Object[] parms = new Object[] {msgId+""};
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
		Object[] parms = new Object[] {msgId+"",addrType};
		List<MsgAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
		return list;
	}
	
	public int update(MsgAddressVo msgAddrVo) {
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgAddrVo.getAddrValue());
		fields.add(msgAddrVo.getMsgId()+"");
		fields.add(msgAddrVo.getAddrType());
		fields.add(msgAddrVo.getAddrSeq()+"");
		
		String sql =
			"update Msg_Address set " +
				"AddrValue=? " +
			" where " +
				" msgid=? and addrType=? and addrSeq=?  ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"delete from Msg_Address where msgid=? and addrType=? and addrSeq=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(addrType);
		fields.add(addrSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Msg_Address where msgid=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgAddressVo msgAddrVo) {
		String sql = 
			"INSERT INTO Msg_Address (" +
			"MsgId, " +
			"AddrType, " +
			"AddrSeq, " +
			"Addrvalue " +
			") VALUES (" +
				" ?, ?, ?, ? " +
				")";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgAddrVo.getMsgId()+"");
		fields.add(msgAddrVo.getAddrType());
		fields.add(msgAddrVo.getAddrSeq()+"");
		fields.add(msgAddrVo.getAddrValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
}
