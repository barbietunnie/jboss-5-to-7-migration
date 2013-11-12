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

import com.legacytojava.message.vo.inbox.AttachmentsVo;

@Component("attachmentsDao")
public class AttachmentsJdbcDao implements AttachmentsDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class AttachmentsMapper implements RowMapper<AttachmentsVo> {
		
		public AttachmentsVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			AttachmentsVo attachmentsVo = new AttachmentsVo();
			
			attachmentsVo.setMsgId(rs.getLong("MsgId"));
			attachmentsVo.setAttchmntDepth(rs.getInt("AttchmntDepth"));
			attachmentsVo.setAttchmntSeq(rs.getInt("AttchmntSeq"));
			attachmentsVo.setAttchmntName(rs.getString("AttchmntName"));
			attachmentsVo.setAttchmntType(rs.getString("AttchmntType"));
			attachmentsVo.setAttchmntDisp(rs.getString("AttchmntDisp"));
			attachmentsVo.setAttchmntValue(rs.getBytes("AttchmntValue"));
			
			return attachmentsVo;
		}
	}

	public AttachmentsVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"Attachments where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",attchmntDepth+"",attchmntSeq+""};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new AttachmentsMapper());
		if (list.size()>0)
			return (AttachmentsVo)list.get(0);
		else
			return null;
	}
	
	public List<AttachmentsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" Attachments where msgId=? " +
			" order by attchmntDepth, attchmntSeq";
		Object[] parms = new Object[] {msgId+""};
		List<AttachmentsVo> list = (List<AttachmentsVo>) getJdbcTemplate().query(sql, parms,
				new AttachmentsMapper());
		return list;
	}
	
	public int update(AttachmentsVo attachmentsVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(attachmentsVo.getAttchmntName());
		fields.add(attachmentsVo.getAttchmntType());
		fields.add(attachmentsVo.getAttchmntDisp());
		fields.add(attachmentsVo.getAttchmntValue());
		fields.add(attachmentsVo.getMsgId()+"");
		fields.add(attachmentsVo.getAttchmntDepth()+"");
		fields.add(attachmentsVo.getAttchmntSeq()+"");
		
		String sql =
			"update Attachments set " +
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
		String sql = 
			"INSERT INTO Attachments (" +
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
		fields.add(attachmentsVo.getMsgId()+"");
		fields.add(attachmentsVo.getAttchmntDepth()+"");
		fields.add(attachmentsVo.getAttchmntSeq()+"");
		fields.add(attachmentsVo.getAttchmntName());
		fields.add(attachmentsVo.getAttchmntType());
		fields.add(attachmentsVo.getAttchmntDisp());
		fields.add(attachmentsVo.getAttchmntValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
