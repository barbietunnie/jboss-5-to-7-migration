package com.es.dao.idtoken;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.IdTokensVo;

@Component("idTokensDao")
public class IdTokensDao extends AbstractDao {
	
	private static final Hashtable<String, Object> cache = new Hashtable<String, Object>();
	
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
		idTokensVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(idTokensVo);

		String sql = MetaDataUtil.buildUpdateStatement("Id_Tokens", idTokensVo);
		synchronized (cache) {
			int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		idTokensVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		idTokensVo.setOrigUpdtTime(idTokensVo.getUpdtTime());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(idTokensVo);

		String sql = MetaDataUtil.buildInsertStatement("Id_Tokens", idTokensVo);
		synchronized (cache) {
			int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
}
