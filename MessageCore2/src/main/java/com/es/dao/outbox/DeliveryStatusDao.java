package com.es.dao.outbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.outbox.DeliveryStatusVo;

@Component("deliveryStatusDao")
public class DeliveryStatusDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

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
			deliveryStatusVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(deliveryStatusVo.getFinalRecipient());
		fields.add(deliveryStatusVo.getOriginalRecipientId());
		fields.add(deliveryStatusVo.getMessageId());
		fields.add(deliveryStatusVo.getDsnStatus());
		fields.add(deliveryStatusVo.getDsnReason());
		fields.add(deliveryStatusVo.getDsnText());
		fields.add(deliveryStatusVo.getDsnRfc822());
		fields.add(deliveryStatusVo.getDeliveryStatus());
		fields.add(deliveryStatusVo.getAddTime());
		fields.add(deliveryStatusVo.getMsgId());
		fields.add(deliveryStatusVo.getFinalRecipientId());
		
		String sql =
			"update Delivery_Status set " +
				"FinalRecipient=?, " +
				"OriginalRecipientId=?, " +
				"MessageId=?, " +
				"DsnStatus=?, " +
				"DsnReason=?, " +
				"DsnText=?, " +
				"DsnRfc822=?, " +
				"DeliveryStatus=?, " +
				"AddTime=? " +
			" where " +
				" msgid=? and finalRecipientId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, long finalRecipientId) {
		String sql = 
			"delete from Delivery_Status where msgid=? and finalRecipientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		fields.add(finalRecipientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from Delivery_Status where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
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
		String sql = 
			"INSERT INTO Delivery_Status (" +
				"MsgId, " +
				"FinalRecipientId, " +
				"FinalRecipient, " +
				"OriginalRecipientId, " +
				"MessageId, " +
				"DsnStatus, " +
				"DsnReason, " +
				"DsnText, " +
				"DsnRfc822, " +
				"DeliveryStatus, " +
				"AddTime " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				",? " +
				")";
		
		if (deliveryStatusVo.getAddTime()==null) {
			deliveryStatusVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		}
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(deliveryStatusVo.getMsgId());
		fields.add(deliveryStatusVo.getFinalRecipientId());
		fields.add(deliveryStatusVo.getFinalRecipient());
		fields.add(deliveryStatusVo.getOriginalRecipientId());
		fields.add(deliveryStatusVo.getMessageId());
		fields.add(deliveryStatusVo.getDsnStatus());
		fields.add(deliveryStatusVo.getDsnReason());
		fields.add(deliveryStatusVo.getDsnText());
		fields.add(deliveryStatusVo.getDsnRfc822());
		fields.add(deliveryStatusVo.getDeliveryStatus());
		fields.add(deliveryStatusVo.getAddTime());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
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
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
