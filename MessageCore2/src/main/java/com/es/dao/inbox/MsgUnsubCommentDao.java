package com.es.dao.inbox;

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
import com.es.vo.inbox.MsgUnsubCommentVo;

@Component("msgUnsubCommentDao")
public class MsgUnsubCommentDao extends AbstractDao {
	static final Logger logger = Logger.getLogger(MsgUnsubCommentDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	public MsgUnsubCommentVo getByPrimaryKey(int rowId){
		String sql = "select * from Msg_Unsub_Comment where RowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			MsgUnsubCommentVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgUnsubCommentVo> getAll() {
		String sql = "select * from Msg_Unsub_Comment " +
		" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public List<MsgUnsubCommentVo> getByMsgId(long msgId) {
		String sql = "select * from Msg_Unsub_Comment " +
			" where MsgId=" + msgId +
			" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public List<MsgUnsubCommentVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from Msg_Unsub_Comment " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public List<MsgUnsubCommentVo> getByListId(String listId) {
		String sql = "select * from Msg_Unsub_Comment " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<MsgUnsubCommentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgUnsubCommentVo>(MsgUnsubCommentVo.class));
		return list;
	}
	
	public int update(MsgUnsubCommentVo msgUnsubCommentsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgUnsubCommentsVo);
		if (msgUnsubCommentsVo.getAddTime() == null) {
			msgUnsubCommentsVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		String sql = MetaDataUtil.buildUpdateStatement("Msg_Unsub_Comment", msgUnsubCommentsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from Msg_Unsub_Comment where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = "delete from Msg_Unsub_Comment where MsgId=?";
		Object[] parms = new Object[] {msgId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from Msg_Unsub_Comment where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MsgUnsubCommentVo msgUnsubCommentsVo) {
		msgUnsubCommentsVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgUnsubCommentsVo);
		String sql = MetaDataUtil.buildInsertStatement("Msg_Unsub_Comment", msgUnsubCommentsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgUnsubCommentsVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
