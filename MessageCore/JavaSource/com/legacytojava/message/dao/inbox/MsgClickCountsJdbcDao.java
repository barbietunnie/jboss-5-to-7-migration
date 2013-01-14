package com.legacytojava.message.dao.inbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.inbox.MsgClickCountsVo;

@Component("msgClickCountsDao")
public class MsgClickCountsJdbcDao implements MsgClickCountsDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class MsgClickCountsMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgClickCountsVo msgClickCountsVo = new MsgClickCountsVo();
			
			msgClickCountsVo.setMsgId(rs.getLong("MsgId"));
			msgClickCountsVo.setListId(rs.getString("ListId"));
			msgClickCountsVo.setDeliveryOption(rs.getString("DeliveryOption"));
			msgClickCountsVo.setSentCount(rs.getInt("SentCount"));
			msgClickCountsVo.setOpenCount(rs.getInt("OpenCount"));
			msgClickCountsVo.setClickCount(rs.getInt("ClickCount"));
			msgClickCountsVo.setLastOpenTime(rs.getTimestamp("LastOpenTime"));
			msgClickCountsVo.setLastClickTime(rs.getTimestamp("LastClickTime"));
			msgClickCountsVo.setStartTime(rs.getTimestamp("StartTime"));
			msgClickCountsVo.setEndTime(rs.getTimestamp("EndTime"));
			msgClickCountsVo.setUnsubscribeCount(rs.getInt("UnsubscribeCount"));
			msgClickCountsVo.setComplaintCount(rs.getInt("ComplaintCount"));
			msgClickCountsVo.setReferralCount(rs.getInt("ReferralCount"));
			
			return msgClickCountsVo;
		}
	}

	@SuppressWarnings("unchecked")
	public List<MsgClickCountsVo> getAll() {
		String sql = 
			"select * " +
			"from " +
				"MsgClickCounts order by MsgId ";
		
		List<MsgClickCountsVo> list = (List<MsgClickCountsVo>) getJdbcTemplate().query(sql,
				new MsgClickCountsMapper());
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
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgClickCountsMapper());
		if (list.size()>0)
			return (MsgClickCountsVo)list.get(0);
		else
			return null;
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and " };
	
	@SuppressWarnings("unchecked")
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
		List<MsgClickCountsVo> list = (List<MsgClickCountsVo>) getJdbcTemplate().query(sql, parms
				.toArray(), new MsgClickCountsMapper());
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	public int update(MsgClickCountsVo msgClickCountsVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgClickCountsVo.getListId());
		fields.add(msgClickCountsVo.getDeliveryOption());
		fields.add(msgClickCountsVo.getSentCount());
		fields.add(msgClickCountsVo.getOpenCount());
		fields.add(msgClickCountsVo.getClickCount());
		fields.add(msgClickCountsVo.getLastOpenTime());
		fields.add(msgClickCountsVo.getLastClickTime());
		fields.add(msgClickCountsVo.getStartTime());
		fields.add(msgClickCountsVo.getEndTime());
		fields.add(msgClickCountsVo.getUnsubscribeCount());
		fields.add(msgClickCountsVo.getComplaintCount());
		fields.add(msgClickCountsVo.getReferralCount());
		fields.add(msgClickCountsVo.getMsgId());
		
		String sql =
			"update MsgClickCounts set " +
				"ListId=?, " +
				"DeliveryOption=?, " +
				"SentCount=?, " +
				"OpenCount=?, " +
				"ClickCount=?, " +
				"LastOpenTime=?, " +
				"LastClickTime=?, " +
				"StartTime=?, " +
				"EndTime=?, " +
				"UnsubscribeCount=?, " +
				"ComplaintCount=?, " +
				"ReferralCount=? " +
			" where " +
				" MsgId=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
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
		String sql = 
			"INSERT INTO MsgClickCounts (" +
			"MsgId, " +
			"ListId, " +
			"DeliveryOption, " +
			"SentCount, " +
			"OpenCount, " +
			"ClickCount, " +
			"LastOpenTime, " +
			"LastClickTime " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgClickCountsVo.getMsgId());
		fields.add(msgClickCountsVo.getListId());
		fields.add(msgClickCountsVo.getDeliveryOption());
		fields.add(msgClickCountsVo.getSentCount());
		fields.add(msgClickCountsVo.getOpenCount());
		fields.add(msgClickCountsVo.getClickCount());
		fields.add(msgClickCountsVo.getLastOpenTime());
		fields.add(msgClickCountsVo.getLastClickTime());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
