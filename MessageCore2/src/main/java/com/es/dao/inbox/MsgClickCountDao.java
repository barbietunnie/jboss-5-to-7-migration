package com.es.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.PagingVo;
import com.es.vo.inbox.MsgClickCountVo;

@Component("msgClickCountDao")
public class MsgClickCountDao extends AbstractDao {
	
	public MsgClickCountVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Click_Count where msgid=? ";
		
		Object[] parms = new Object[] {msgId};
		try {
			MsgClickCountVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MsgClickCountVo> getAll() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Click_Count order by MsgId ";
		
		List<MsgClickCountVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
		return list;
	}
	
	public int getMsgCountForWeb() {
		String sql = 
			"select count(*) " +
			"from " +
				"Msg_Click_Count where SentCount > 0 and StartTime is not null ";
		int count = getJdbcTemplate().queryForObject(sql, Integer.class);
		return count;
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and " };
	
	public List<MsgClickCountVo> getBroadcastsWithPaging(PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = "";
		/*
		 * paging logic
		 */
		String fetchOrder = "desc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId < ? ";
				parms.add(vo.getIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId > ? ";
				parms.add(vo.getIdFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<MsgClickCountVo> lastList = new ArrayList<MsgClickCountVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<MsgClickCountVo> nextList = getBroadcastsWithPaging(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setIdLast(nextList.get(nextList.size() - 1).getMsgId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId <= ? ";
				parms.add(vo.getIdFirst());
			}
		}
		whereSql += CRIT[parms.size()] + " a.SentCount > ? ";
		parms.add(0);
		
		String sql = 
			"select a.* " +
			" from Msg_Click_Count a " +
			whereSql +
			" and a.StartTime is not null " +
			" order by a.MsgId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgClickCountVo> list = getJdbcTemplate().query(sql, parms
				.toArray(), new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	public int update(MsgClickCountVo msgClickCountVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountVo);

		String sql = MetaDataUtil.buildUpdateStatement("Msg_Click_Count", msgClickCountVo);

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int updateSentCount(long msgId, int count) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"SentCount=SentCount+" + count +
				", EndTime=now() " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int updateOpenCount(long msgId, int count) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"OpenCount=OpenCount+" + count +
				", LastOpenTime=now() " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int updateOpenCount(long msgId) {
		return updateOpenCount(msgId, 1);
	}

	public int updateClickCount(long msgId, int count) {
		Timestamp currTime = new Timestamp(new java.util.Date().getTime());
		List<Object> fields = new ArrayList<Object>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"ClickCount=ClickCount+" + count +
				" ,LastClickTime=? " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int updateClickCount(long msgId) {
		return updateClickCount(msgId, 1);
	}

	public int updateReferalCount(long msgId, int count) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"ReferralCount=ReferralCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}

	public int updateReferalCount(long msgId) {
		return updateReferalCount(msgId, 1);
	}

	public int updateStartTime(long msgId) {
		Timestamp currTime = new Timestamp(new java.util.Date().getTime());
		List<Object> fields = new ArrayList<Object>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"StartTime=? " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int updateUnsubscribeCount(long msgId, int count) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"UnsubscribeCount=UnsubscribeCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int updateComplaintCount(long msgId, int count) {
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update Msg_Click_Count set " +
				"ComplaintCount=ComplaintCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from Msg_Click_Count where msgid=? ";
		
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgClickCountVo msgClickCountVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountVo);

		String sql = MetaDataUtil.buildInsertStatement("Msg_Click_Count", msgClickCountVo);

		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
