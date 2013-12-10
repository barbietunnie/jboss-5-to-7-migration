package com.es.dao.outbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.outbox.DeliveryStatusVo;

@Component("deliveryStatusDao")
public class DeliveryStatusDao extends AbstractDao {
	
	public DeliveryStatusVo getByPrimaryKey(long msgId, long finalRecipientId) {
		String sql = 
			"select * " +
			"from " +
				"Delivery_Status where msgid=? and finalRecipientId=? ";
		
		Object[] parms = new Object[] {msgId, finalRecipientId};
		try {
			DeliveryStatusVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<DeliveryStatusVo>(DeliveryStatusVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<DeliveryStatusVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Delivery_Status where msgId=? " +
			" order by finalRecipient";
		Object[] parms = new Object[] {msgId};
		List<DeliveryStatusVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<DeliveryStatusVo>(DeliveryStatusVo.class));
		return list;
	}
	
	public int update(DeliveryStatusVo deliveryStatusVo) {
		if (deliveryStatusVo.getAddTime()==null) {
			deliveryStatusVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deliveryStatusVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Delivery_Status", deliveryStatusVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, long finalRecipientId) {
		String sql = 
			"delete from Delivery_Status where msgid=? and finalRecipientId=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		fields.add(finalRecipientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Delivery_Status where msgid=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	/**
	 * First delete the record to be inserted, then insert the record. This is a
	 * workaround to avoid DataIntegrityViolationException if the record to be
	 * inserted already exists in the database.
	 * <p>
	 * 
	 * Once the DataIntegrityViolationException is generated, spring will also
	 * set the global rollback-only flag to true, causing the entire transaction
	 * to fail.
	 */
	public synchronized int insertWithDelete(DeliveryStatusVo deliveryStatusVo) {
		deleteByPrimaryKey(deliveryStatusVo.getMsgId(), deliveryStatusVo.getFinalRecipientId());
		return insert(deliveryStatusVo);
	}
	
	public int insert(DeliveryStatusVo deliveryStatusVo) {
		if (deliveryStatusVo.getAddTime()==null) {
			deliveryStatusVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deliveryStatusVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Delivery_Status", deliveryStatusVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
	public synchronized int upsert(DeliveryStatusVo deliveryStatusVo) {
		try {
			return insert(deliveryStatusVo);
		}
		catch (DataIntegrityViolationException e) {
			return update(deliveryStatusVo);
		}
	}
	
}
