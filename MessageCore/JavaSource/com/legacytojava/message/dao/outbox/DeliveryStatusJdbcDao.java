package com.legacytojava.message.dao.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.outbox.DeliveryStatusVo;

@Component("deliveryStatusDao")
public class DeliveryStatusJdbcDao implements DeliveryStatusDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class DeliveryStatusMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			DeliveryStatusVo deliveryStatusVo = new DeliveryStatusVo();

			deliveryStatusVo.setMsgId(rs.getLong("MsgId"));
			deliveryStatusVo.setFinalRecipientId(rs.getLong("FinalRecipientId"));
			deliveryStatusVo.setFinalRecipient(rs.getString("FinalRecipient"));
			deliveryStatusVo.setOriginalRecipientId((Long)rs.getObject("OriginalRecipientId"));
			deliveryStatusVo.setMessageId(rs.getString("MessageId"));
			deliveryStatusVo.setDsnStatus(rs.getString("DsnStatus"));
			deliveryStatusVo.setDsnReason(rs.getString("DsnReason"));
			deliveryStatusVo.setDsnText(rs.getString("DsnText"));
			deliveryStatusVo.setDsnRfc822(rs.getString("DsnRfc822"));
			deliveryStatusVo.setDeliveryStatus(rs.getString("DeliveryStatus"));
			deliveryStatusVo.setAddTime(rs.getTimestamp("AddTime"));
			
			return deliveryStatusVo;
		}
	}

	public DeliveryStatusVo getByPrimaryKey(long msgId, long finalRecipientId) {
		String sql = 
			"select * " +
			"from " +
				"DeliveryStatus where msgid=? and finalRecipientId=? ";
		
		Object[] parms = new Object[] {msgId, finalRecipientId};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new DeliveryStatusMapper());
		if (list.size()>0)
			return (DeliveryStatusVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<DeliveryStatusVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" DeliveryStatus where msgId=? " +
			" order by finalRecipient";
		Object[] parms = new Object[] {msgId};
		List<DeliveryStatusVo> list = (List<DeliveryStatusVo>)getJdbcTemplate().query(sql, parms, new DeliveryStatusMapper());
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
			"update DeliveryStatus set " +
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
			"delete from DeliveryStatus where msgid=? and finalRecipientId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		fields.add(finalRecipientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from DeliveryStatus where msgid=? ";
		
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
			"INSERT INTO DeliveryStatus (" +
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
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
