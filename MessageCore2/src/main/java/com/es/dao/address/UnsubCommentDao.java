package com.es.dao.address;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.address.UnsubCommentVo;

@Component("unsubCommentDao")
public class UnsubCommentDao extends AbstractDao {
	static final Logger logger = Logger.getLogger(UnsubCommentDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(unsubCommentsVo);

		String sql = MetaDataUtil.buildUpdateStatement("Unsub_Comment", unsubCommentsVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		unsubCommentsVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(unsubCommentsVo);

		String sql = MetaDataUtil.buildInsertStatement("Unsub_Comment", unsubCommentsVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		unsubCommentsVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
