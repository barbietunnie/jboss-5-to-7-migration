package com.legacytojava.message.dao.inbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.inbox.RfcFieldsVo;

@Component("rfcFieldsDao")
public class RfcFieldsJdbcDao implements RfcFieldsDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class RfcFieldsMapper implements RowMapper<RfcFieldsVo> {
		
		public RfcFieldsVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RfcFieldsVo rfcFieldsVo = new RfcFieldsVo();
			
			rfcFieldsVo.setMsgId(rs.getLong("MsgId"));
			rfcFieldsVo.setRfcType(rs.getString("RfcType"));
			rfcFieldsVo.setRfcStatus(rs.getString("RfcStatus"));
			rfcFieldsVo.setRfcAction(rs.getString("RfcAction"));
			rfcFieldsVo.setFinalRcpt(rs.getString("FinalRcpt"));
			rfcFieldsVo.setFinalRcptId((Long)rs.getObject("FinalRcptId"));
			rfcFieldsVo.setOrigRcpt(rs.getString("OrigRcpt"));
			rfcFieldsVo.setOrigMsgSubject(rs.getString("OrigMsgSubject"));
			rfcFieldsVo.setMessageId(rs.getString("MessageId"));
			rfcFieldsVo.setDsnText(rs.getString("DsnText"));
			rfcFieldsVo.setDsnRfc822(rs.getString("DsnRfc822"));
			rfcFieldsVo.setDlvrStatus(rs.getString("DlvrStatus"));
			
			return rfcFieldsVo;
		}
	}

	public RfcFieldsVo getByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"select * " +
			"from " +
				"RfcFields where msgid=? and rfcType=? ";
		
		Object[] parms = new Object[] {msgId+"",rfcType};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new RfcFieldsMapper());
		if (list.size()>0)
			return (RfcFieldsVo)list.get(0);
		else
			return null;
	}
	
	public List<RfcFieldsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" RfcFields where msgId=? " +
			" order by rfcType";
		Object[] parms = new Object[] {msgId+""};
		List<RfcFieldsVo> list = (List<RfcFieldsVo>)getJdbcTemplate().query(sql, parms, new RfcFieldsMapper());
		return list;
	}
	
	public int update(RfcFieldsVo rfcFieldsVo) {

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
			"update RfcFields set " +
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
			"delete from RfcFields where msgid=? and rfcType=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		fields.add(rfcType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from RfcFields where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RfcFieldsVo rfcFieldsVo) {
		String sql = 
			"INSERT INTO RfcFields (" +
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
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
