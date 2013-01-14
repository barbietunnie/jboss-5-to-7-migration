package com.legacytojava.message.dao.idtokens;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.IdTokensVo;

@Component("idTokensDao")
public class IdTokensJdbcDao implements IdTokensDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate = null;
	
	private static final Hashtable<String, Object> cache = new Hashtable<String, Object>();
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class IdTokensMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			IdTokensVo idTokensVo = new IdTokensVo();
			
			idTokensVo.setRowId(rs.getInt("RowId"));
			idTokensVo.setClientId(rs.getString("ClientId"));
			idTokensVo.setDescription(rs.getString("Description"));
			idTokensVo.setBodyBeginToken(rs.getString("BodyBeginToken"));
			idTokensVo.setBodyEndToken(rs.getString("BodyEndToken"));
			idTokensVo.setXHeaderName(rs.getString("XHeaderName"));
			idTokensVo.setXhdrBeginToken(rs.getString("XhdrBeginToken"));
			idTokensVo.setXhdrEndToken(rs.getString("XhdrEndToken"));
			idTokensVo.setMaxLength(rs.getInt("MaxLength"));
			idTokensVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			idTokensVo.setUpdtUserId(rs.getString("UpdtUserId"));
			idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
			return idTokensVo;
		}
	}
	public IdTokensVo getByClientId(String clientId) {
		/*
		 * This method is not thread safe as the "cache" is not locked.
		 * Since this method is heavily used it is reasonable to keep the 
		 * performance impact at the minimal by sacrificing thread safety.
		 */
		if (!cache.containsKey(clientId)) {
			String sql = "select * from IdTokens where clientId=?";
			Object[] parms = new Object[] {clientId};
			List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new IdTokensMapper());
			if (list.size()>0) {
				cache.put(clientId, list.get(0));
			}
			else {
				cache.put(clientId, null);
			}
		}
		return (IdTokensVo)cache.get(clientId);
	}
	
	@SuppressWarnings("unchecked")
	public List<IdTokensVo> getAll() {
		String sql = "select * from IdTokens order by clientId";
		List<IdTokensVo> list = (List<IdTokensVo>)getJdbcTemplate().query(sql, new IdTokensMapper());
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
				idTokensVo.getClientId()
				};
		
		String sql = "update IdTokens set " +
			"Description=?," +
			"BodyBeginToken=?," +
			"BodyEndToken=?," +
			"XHeaderName=?," +
			"XhdrBeginToken=?," +
			"XhdrEndToken=?," +
			"MaxLength=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where clientId=?";
		
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		synchronized (cache) {
			int rowsUpadted = getJdbcTemplate().update(sql, parms);
			removeFromCache(idTokensVo.getClientId());
			return rowsUpadted;
		}
	}
	
	public int delete(String clientId) {
		String sql = "delete from IdTokens where clientId=?";
		Object[] parms = new Object[] {clientId};
		synchronized (cache) {
			int rowsDeleted = getJdbcTemplate().update(sql, parms);
			removeFromCache(clientId);
			return rowsDeleted;
		}
	}
	
	public int insert(IdTokensVo idTokensVo) {
		idTokensVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
			idTokensVo.getClientId(),
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
			"INSERT INTO IdTokens " +
				"(ClientId," +
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
			removeFromCache(idTokensVo.getClientId());
			return rowsInserted;
		}
	}
	
	private void removeFromCache(String clientId) {
		if (cache.containsKey(clientId)) {
			cache.remove(clientId);
		}
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
