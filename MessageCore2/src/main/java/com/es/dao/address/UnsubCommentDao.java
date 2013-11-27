package com.es.dao.address;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.address.UnsubCommentVo;

@Component("unsubCommentDao")
public class UnsubCommentDao {
	static final Logger logger = Logger.getLogger(UnsubCommentDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public UnsubCommentVo getByPrimaryKey(int rowId){
		String sql = "select * from Unsub_Comment where RowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			UnsubCommentVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<UnsubCommentVo>(UnsubCommentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<UnsubCommentVo> getAll() {
		String sql = "select * from Unsub_Comment " +
		" order by RowId";
		List<UnsubCommentVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<UnsubCommentVo>(UnsubCommentVo.class));
		return list;
	}
	
	public List<UnsubCommentVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from Unsub_Comment " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<UnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<UnsubCommentVo>(UnsubCommentVo.class));
		return list;
	}
	
	public List<UnsubCommentVo> getByListId(String listId) {
		String sql = "select * from Unsub_Comment " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<UnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<UnsubCommentVo>(UnsubCommentVo.class));
		return list;
	}
	
	public int update(UnsubCommentVo unsubCommentsVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(unsubCommentsVo.getEmailAddrId());
		keys.add(unsubCommentsVo.getListId());
		keys.add(unsubCommentsVo.getComments());
		keys.add(unsubCommentsVo.getRowId());

		String sql = "update Unsub_Comment set " +
			"EmailAddrId=?," +
			"ListId=?," +
			"Comments=?" +
			" where RowId=?";
		
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from Unsub_Comment where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from Unsub_Comment where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(UnsubCommentVo unsubCommentsVo) {
		unsubCommentsVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				unsubCommentsVo.getEmailAddrId(),
				unsubCommentsVo.getListId(),
				unsubCommentsVo.getComments(),
				unsubCommentsVo.getAddTime()
			};
		
		String sql = "INSERT INTO Unsub_Comment (" +
			"EmailAddrId," +
			"ListId," +
			"Comments," +
			"AddTime " +
			") VALUES (" +
				" ?, ?, ?, ? " +
				")";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		unsubCommentsVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
