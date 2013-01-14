package com.legacytojava.message.dao.emailaddr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.emailaddr.UnsubCommentsVo;

@Component("unsubCommentsDao")
public class UnsubCommentsJdbcDao implements UnsubCommentsDao {
	static final Logger logger = Logger.getLogger(UnsubCommentsJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class UnsubCommentsMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			UnsubCommentsVo unsubCommentsVo = new UnsubCommentsVo();
			
			unsubCommentsVo.setRowId(rs.getInt("RowId"));
			unsubCommentsVo.setEmailAddrId(rs.getLong("EmailAddrId"));
			unsubCommentsVo.setListId(rs.getString("ListId"));
			unsubCommentsVo.setComments(rs.getString("Comments"));
			unsubCommentsVo.setAddTime(rs.getTimestamp("AddTime"));
			
			return unsubCommentsVo;
		}
	}
	
	public UnsubCommentsVo getByPrimaryKey(int rowId){
		String sql = "select * from UnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new UnsubCommentsMapper());
		if (list.size()>0) {
			return (UnsubCommentsVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<UnsubCommentsVo> getAll() {
		String sql = "select * from UnsubComments " +
		" order by RowId";
		List<UnsubCommentsVo> list = (List<UnsubCommentsVo>)getJdbcTemplate().query(sql, new UnsubCommentsMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<UnsubCommentsVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from UnsubComments " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<UnsubCommentsVo> list = (List<UnsubCommentsVo>)getJdbcTemplate().query(sql, new UnsubCommentsMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<UnsubCommentsVo> getByListId(String listId) {
		String sql = "select * from UnsubComments " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<UnsubCommentsVo> list = (List<UnsubCommentsVo>)getJdbcTemplate().query(sql, new UnsubCommentsMapper());
		return list;
	}
	
	public int update(UnsubCommentsVo unsubCommentsVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(unsubCommentsVo.getEmailAddrId());
		keys.add(unsubCommentsVo.getListId());
		keys.add(unsubCommentsVo.getComments());
		keys.add(unsubCommentsVo.getRowId());

		String sql = "update UnsubComments set " +
			"EmailAddrId=?," +
			"ListId=?," +
			"Comments=?" +
			" where RowId=?";
		
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from UnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from UnsubComments where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(UnsubCommentsVo unsubCommentsVo) {
		unsubCommentsVo.setAddTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				unsubCommentsVo.getEmailAddrId(),
				unsubCommentsVo.getListId(),
				unsubCommentsVo.getComments(),
				unsubCommentsVo.getAddTime()
			};
		
		String sql = "INSERT INTO UnsubComments (" +
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
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
