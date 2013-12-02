package com.es.dao.outbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.outbox.MsgRenderedVo;

@Component("msgRenderedDao")
public class MsgRenderedDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MsgRenderedVo getByPrimaryKey(long renderId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rendered where renderId=? ";
		
		Object[] parms = new Object[] {renderId};
		try {
			MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
			return  vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgRenderedVo getFirstRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rendered where renderId=(select min(RenderId) from Msg_Rendered) ";
		
		MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return vo;
	}
	
	public MsgRenderedVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Rendered where renderId=(select max(RenderId) from Msg_Rendered) ";
		
		MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return vo;
	}
	
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Rendered where msgSourceId=? " +
			" order by renderId";
		Object[] parms = new Object[] {msgSourceId};
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return list;
	}
	
	
	public int update(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgRenderedVo.getMsgSourceId());
		fields.add(msgRenderedVo.getTemplateId());
		fields.add(msgRenderedVo.getStartTime());
		fields.add(msgRenderedVo.getSenderId());
		fields.add(msgRenderedVo.getSubrId());
		fields.add(msgRenderedVo.getPurgeAfter());
		fields.add(msgRenderedVo.getUpdtTime());
		fields.add(msgRenderedVo.getUpdtUserId());
		fields.add(msgRenderedVo.getRenderId());
		
		String sql =
			"update Msg_Rendered set " +
				"MsgSourceId=?, " +
				"TemplateId=?, " +
				"StartTime=?, " +
				"SenderId=?, " +
				"SubrId=?, " +
				"PurgeAfter=?, " +
				"UpdtTime=?, " +
				"UpdtUserId=? " +
			" where " +
				" renderId=? ";
		
		if (msgRenderedVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgRenderedVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long renderId) {
		String sql = 
			"delete from Msg_Rendered where renderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgRenderedVo msgRenderedVo) {
		String sql = 
			"INSERT INTO Msg_Rendered (" +
				"MsgSourceId, " +
				"TemplateId, " +
				"StartTime, " +
				"SenderId, " +
				"SubrId, " +
				"PurgeAfter, " +
				"UpdtTime, " +
				"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		msgRenderedVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgRenderedVo.getMsgSourceId());
		fields.add(msgRenderedVo.getTemplateId());
		fields.add(msgRenderedVo.getStartTime());
		fields.add(msgRenderedVo.getSenderId());
		fields.add(msgRenderedVo.getSubrId());
		fields.add(msgRenderedVo.getPurgeAfter());
		fields.add(msgRenderedVo.getUpdtTime());
		fields.add(msgRenderedVo.getUpdtUserId());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		msgRenderedVo.setRenderId(getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class));
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
