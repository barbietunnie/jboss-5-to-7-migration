package com.legacytojava.message.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.inbox.MsgClickCountsVo;

@Component("msgClickCountsDao")
public class MsgClickCountsJdbcDao extends AbstractDao implements MsgClickCountsDao {
	
	public List<MsgClickCountsVo> getAll() {
		String sql = 
			"select * " +
			"from " +
				"MsgClickCounts order by MsgId ";
		
		List<MsgClickCountsVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgClickCountsVo>(MsgClickCountsVo.class));
		return list;
	}
	
	public int getMsgCountForWeb() {
		String sql = 
			"select count(*) " +
			"from " +
				"MsgClickCounts where SentCount > 0 and StartTime is not null ";
		int count = getJdbcTemplate().queryForInt(sql);
		return count;
	}
	
	public MsgClickCountsVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"MsgClickCounts where msgid=? ";
		
		Object[] parms = new Object[] {msgId};
		try {
			MsgClickCountsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgClickCountsVo>(MsgClickCountsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and " };
	
	public List<MsgClickCountsVo> getBroadcastsWithPaging(PagingVo vo) {
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
			List<MsgClickCountsVo> lastList = new ArrayList<MsgClickCountsVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<MsgClickCountsVo> nextList = getBroadcastsWithPaging(vo);
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
			" from MsgClickCounts a " +
			whereSql +
			" and a.StartTime is not null " +
			" order by a.MsgId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgClickCountsVo> list = getJdbcTemplate().query(sql, parms.toArray(), 
				new BeanPropertyRowMapper<MsgClickCountsVo>(MsgClickCountsVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	public int update(MsgClickCountsVo msgClickCountsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountsVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgClickCounts", msgClickCountsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int updateSentCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"SentCount=SentCount+" + count +
				", EndTime=now() " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int updateOpenCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
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
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
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
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
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
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"StartTime=? " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int updateUnsubscribeCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"UnsubscribeCount=UnsubscribeCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int updateComplaintCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"ComplaintCount=ComplaintCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from MsgClickCounts where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgClickCountsVo msgClickCountsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountsVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgClickCounts", msgClickCountsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}
