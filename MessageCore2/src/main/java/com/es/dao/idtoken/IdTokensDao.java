package com.es.dao.idtoken;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.comm.IdTokensVo;

@Component("idTokensDao")
public class IdTokensDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate = null;
	
	private static final Hashtable<String, Object> cache = new Hashtable<String, Object>();
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public IdTokensVo getBySenderId(String senderId) {
		/*
		 * This method is not thread safe as the "cache" is not locked.
		 * Since this method is heavily used it is reasonable to keep the 
		 * performance impact at the minimal by sacrificing thread safety.
		 */
		if (!cache.containsKey(senderId)) {
			String sql = "select * from Id_Tokens where senderId=?";
			Object[] parms = new Object[] {senderId};
			List<IdTokensVo> list = getJdbcTemplate().query(sql, parms,
					new BeanPropertyRowMapper<IdTokensVo>(IdTokensVo.class));
			if (list.size()>0) {
				cache.put(senderId, list.get(0));
			}
			else {
				cache.put(senderId, null);
			}
		}
		return (IdTokensVo)cache.get(senderId);
	}
	
	public List<IdTokensVo> getAll() {
		String sql = "select * from Id_Tokens order by senderId";
		List<IdTokensVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<IdTokensVo>(IdTokensVo.class));
		return list;
	}
	
	public int update(IdTokensVo idTokensVo) {
		idTokensVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				idTokensVo.getDescription(),
				idTokensVo.getBodyBeginToken(),
				idTokensVo.getBodyEndToken(),
				idTokensVo.getXHeaderName(),
				idTokensVo.getXhdrBeginToken(),
				idTokensVo.getXhdrEndToken(),
				""+idTokensVo.getMaxLength(),
				idTokensVo.getUpdtTime(),
				idTokensVo.getUpdtUserId(),
				idTokensVo.getSenderId()
				};
		
		String sql = "update Id_Tokens set " +
			"Description=?," +
			"BodyBeginToken=?," +
			"BodyEndToken=?," +
			"XHeaderName=?," +
			"XhdrBeginToken=?," +
			"XhdrEndToken=?," +
			"MaxLength=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where senderId=?";
		
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		synchronized (cache) {
			int rowsUpadted = getJdbcTemplate().update(sql, parms);
			removeFromCache(idTokensVo.getSenderId());
			return rowsUpadted;
		}
	}
	
	public int delete(String senderId) {
		String sql = "delete from Id_Tokens where senderId=?";
		Object[] parms = new Object[] {senderId};
		synchronized (cache) {
			int rowsDeleted = getJdbcTemplate().update(sql, parms);
			removeFromCache(senderId);
			return rowsDeleted;
		}
	}
	
	public int insert(IdTokensVo idTokensVo) {
		idTokensVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
			idTokensVo.getSenderId(),
			idTokensVo.getDescription(),
			idTokensVo.getBodyBeginToken(),
			idTokensVo.getBodyEndToken(),
			idTokensVo.getXHeaderName(),
			idTokensVo.getXhdrBeginToken(),
			idTokensVo.getXhdrEndToken(),
			""+idTokensVo.getMaxLength(),
			idTokensVo.getUpdtTime(),
			idTokensVo.getUpdtUserId()
			};
		
		String sql = 
			"INSERT INTO Id_Tokens " +
				"(SenderId," +
				"Description," +
				"BodyBeginToken," +
				"BodyEndToken," +
				"XHeaderName," +
				"XhdrBeginToken," +
				"XhdrEndToken," +
				"MaxLength," +
				"UpdtTime," +
				"UpdtUserId " +
			") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		synchronized (cache) {
			int rowsInserted = getJdbcTemplate().update(sql, parms);
			idTokensVo.setRowId(retrieveRowId());
			removeFromCache(idTokensVo.getSenderId());
			return rowsInserted;
		}
	}
	
	private void removeFromCache(String senderId) {
		if (cache.containsKey(senderId)) {
			cache.remove(senderId);
		}
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
