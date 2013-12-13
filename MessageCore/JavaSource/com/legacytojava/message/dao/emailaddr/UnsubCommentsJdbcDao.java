package com.legacytojava.message.dao.emailaddr;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.emailaddr.UnsubCommentsVo;

@Component("unsubCommentsDao")
public class UnsubCommentsJdbcDao extends AbstractDao implements UnsubCommentsDao {
	static final Logger logger = Logger.getLogger(UnsubCommentsJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public UnsubCommentsVo getByPrimaryKey(int rowId){
		String sql = "select * from UnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			UnsubCommentsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<UnsubCommentsVo>(UnsubCommentsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<UnsubCommentsVo> getAll() {
		String sql = "select * from UnsubComments " +
		" order by RowId";
		List<UnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<UnsubCommentsVo>(UnsubCommentsVo.class));
		return list;
	}
	
	public List<UnsubCommentsVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from UnsubComments " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<UnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<UnsubCommentsVo>(UnsubCommentsVo.class));
		return list;
	}
	
	public List<UnsubCommentsVo> getByListId(String listId) {
		String sql = "select * from UnsubComments " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<UnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<UnsubCommentsVo>(UnsubCommentsVo.class));
		return list;
	}
	
	public int update(UnsubCommentsVo unsubCommentsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(unsubCommentsVo);
		String sql = MetaDataUtil.buildUpdateStatement("UnsubComments", unsubCommentsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		unsubCommentsVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(unsubCommentsVo);
		String sql = MetaDataUtil.buildInsertStatement("UnsubComments", unsubCommentsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		unsubCommentsVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
