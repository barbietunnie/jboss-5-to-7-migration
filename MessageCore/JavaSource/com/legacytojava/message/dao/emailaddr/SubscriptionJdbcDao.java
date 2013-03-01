package com.legacytojava.message.dao.emailaddr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;

@Component("subscriptionDao")
public class SubscriptionJdbcDao implements SubscriptionDao {
	static final Logger logger = Logger.getLogger(SubscriptionJdbcDao.class);
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

	protected static class SubscriptionMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			SubscriptionVo subscriptionVo = new SubscriptionVo();
			
			subscriptionVo.setEmailAddrId(rs.getLong("EmailAddrId"));
			subscriptionVo.setListId(rs.getString("ListId"));
			subscriptionVo.setSubscribed(rs.getString("Subscribed"));
			subscriptionVo.setCreateTime(rs.getTimestamp("CreateTime"));
			subscriptionVo.setEmailAddr(rs.getString("EmailAddr"));
			subscriptionVo.setAcceptHtml(rs.getString("AcceptHtml"));
			subscriptionVo.setSentCount(rs.getInt("SentCount"));
			subscriptionVo.setLastSentTime(rs.getTimestamp("LastSentTime"));
			subscriptionVo.setOpenCount(rs.getInt("OpenCount"));
			subscriptionVo.setLastOpenTime(rs.getTimestamp("LastOpenTime"));
			subscriptionVo.setClickCount(rs.getInt("ClickCount"));
			subscriptionVo.setLastClickTime(rs.getTimestamp("LastClickTime"));
			
			return subscriptionVo;
		}
	}
	
	private static final class SubscriberMapper2 extends SubscriptionMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			SubscriptionVo subscriptionVo = (SubscriptionVo) super.mapRow(rs, rowNum);
			subscriptionVo.setFirstName(rs.getString("FirstName"));
			subscriptionVo.setLastName(rs.getString("LastName"));
			subscriptionVo.setMiddleName(rs.getString("MiddleName"));
			
			return subscriptionVo;
		}
	}
	
	/**
	 * Set Subscribed field to "Yes". Called when an subscription is received
	 * from an email address.
	 */
	public int subscribe(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo == null) { // insert
			vo = new SubscriptionVo();
			vo.setEmailAddrId(addrId);
			vo.setListId(listId);
			vo.setSubscribed(Constants.YES_CODE);
			rowsAffected = insert(vo);
		}
		else { // update
			if (!(Constants.YES_CODE.equals(vo.getSubscribed()))) {
				vo.setSubscribed(Constants.YES_CODE);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	public int subscribe(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().findByAddress(addr);
		return subscribe(addrVo.getEmailAddrId(), listId);
	}
	
	/**
	 * Set Subscribed field to "No". Called when a removal request is received
	 * from either an email address or a public subscription request web page.
	 */
	public int unsubscribe(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo != null) { // update
			if (!Constants.NO_CODE.equals(vo.getSubscribed())) {
				vo.setSubscribed(Constants.NO_CODE);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}

	public int unsubscribe(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().getByAddress(addr);
		if (addrVo != null) {
			return unsubscribe(addrVo.getEmailAddrId(), listId);
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Set Subscribed field to "Pending". Called when a subscription is received
	 * from a public subscription request web page.
	 */
	public int optInRequest(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo == null) { // insert
			vo = new SubscriptionVo();
			vo.setEmailAddrId(addrId);
			vo.setListId(listId);
			vo.setSubscribed(MsgStatusCode.PENDING);
			rowsAffected = insert(vo);
		}
		else { // update
			if (!Constants.YES_CODE.equals(vo.getSubscribed())
					&& !MsgStatusCode.PENDING.equals(vo.getSubscribed())) {
				vo.setSubscribed(MsgStatusCode.PENDING);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	public int optInRequest(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().findByAddress(addr);
		return optInRequest(addrVo.getEmailAddrId(), listId);
	}
	
	/**
	 * Set Subscribed field to "Yes". Called when a subscription is received
	 * from public subscription confirmation web page.
	 */
	public int optInConfirm(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo != null) { // update
			if (MsgStatusCode.PENDING.equals(vo.getSubscribed())) {
				vo.setSubscribed(Constants.YES_CODE);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	public int optInConfirm(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().findByAddress(addr);
		return optInConfirm(addrVo.getEmailAddrId(), listId);
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };
	
	public int getSubscriberCount(String listId, PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(listId, vo, parms);
		String sql = 
			"select count(*) " +
			" from Subscription a " +
				" join EmailAddr b on a.EmailAddrId=b.EmailAddrId " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForInt(sql, parms.toArray());
		return rowCount;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionVo> getSubscribersWithPaging(String listId, PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(listId, vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "asc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId > ? ";
				parms.add(vo.getIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId < ? ";
				parms.add(vo.getIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<SubscriptionVo> lastList = new ArrayList<SubscriptionVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<SubscriptionVo> nextList = getSubscribersWithPaging(listId, vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setIdLast(nextList.get(nextList.size() - 1).getEmailAddrId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId >= ? ";
				parms.add(vo.getIdFirst());
			}
		}
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime, " +
				" c.FirstName, " +
				" c.LastName, " +
				" c.MiddleName " +
			" from Subscription a" +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" LEFT OUTER JOIN Customers c on a.EmailAddrId=c.EmailAddrId " +
			whereSql +
			" order by a.EmailAddrId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<SubscriptionVo> list = (List<SubscriptionVo>) getJdbcTemplate().query(sql, parms.toArray(),
				new SubscriberMapper2());
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}
	
	private String buildWhereClause(String listId, PagingVo vo, List<Object> parms) {
		String whereSql = CRIT[parms.size()] + " a.ListId = ? ";
		parms.add(listId);
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " b.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		//whereSql += CRIT[parms.size()] + " a.Subscribed = ? ";
		//parms.add(Constants.YES_CODE);
		// search by address
		if (vo.getSearchString() != null && vo.getSearchString().trim().length() > 0) {
			String addr = vo.getSearchString().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr LIKE '%" + addr + "%' ";
			}
			else {
				String regex = StringUtil.replaceAll(addr, " ", ".+");
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr REGEXP '" + regex + "' ";
			}
		}
		return whereSql;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionVo> getSubscribers(String listId) {
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime, " +
				" c.FirstName, " +
				" c.LastName, " +
				" c.MiddleName " +
			" from Subscription a" +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" LEFT OUTER JOIN Customers c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusIdCode.ACTIVE, Constants.YES_CODE};
		List<SubscriptionVo> list = (List<SubscriptionVo>) getJdbcTemplate().query(sql, parms,
				new SubscriberMapper2());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionVo> getSubscribersWithCustomerRecord(String listId) {
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime, " +
				" c.FirstName, " +
				" c.LastName, " +
				" c.MiddleName " +
			" from Subscription a" +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" JOIN Customers c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusIdCode.ACTIVE, Constants.YES_CODE};
		List<SubscriptionVo> list = (List<SubscriptionVo>) getJdbcTemplate().query(sql, parms,
				new SubscriberMapper2());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionVo> getSubscribersWithoutCustomerRecord(String listId) {
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime " +
			" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? " +
				" and not exists (select 1 from Customers where EmailAddrId=b.EmailAddrId) ";
		Object[] parms = new Object[] {listId, StatusIdCode.ACTIVE, Constants.YES_CODE};
		List<SubscriptionVo> list = (List<SubscriptionVo>) getJdbcTemplate().query(sql, parms,
				new SubscriptionMapper());
		return list;
	}
	
	public SubscriptionVo getByPrimaryKey(long addrId, String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" where a.EmailAddrId=? and a.ListId=?";
		Object[] parms = new Object[] {addrId, listId};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new SubscriptionMapper());
		if (list.size()>0) {
			return (SubscriptionVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	public SubscriptionVo getByAddrAndListId(String addr, String listId) {
		String sql = "SELECT a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=? and b.EmailAddr=?";
		Object[] parms = new Object[] {listId, addr};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new SubscriptionMapper());
		if (list.size()>0) {
			return (SubscriptionVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionVo> getByAddrId(long addrId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		List<SubscriptionVo> list = (List<SubscriptionVo>) getJdbcTemplate().query(sql, parms,
				new SubscriptionMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionVo> getByListId(String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=?";
		Object[] parms = new Object[] {listId};
		List<SubscriptionVo> list = (List<SubscriptionVo>) getJdbcTemplate().query(sql, parms,
				new SubscriptionMapper());
		return list;
	}

	public int update(SubscriptionVo subscriptionVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(subscriptionVo.getSubscribed());
		keys.add(subscriptionVo.getSentCount());
		keys.add(subscriptionVo.getLastSentTime());
		keys.add(subscriptionVo.getOpenCount());
		keys.add(subscriptionVo.getLastOpenTime());
		keys.add(subscriptionVo.getClickCount());
		keys.add(subscriptionVo.getLastClickTime());
		keys.add(subscriptionVo.getEmailAddrId());
		keys.add(subscriptionVo.getListId());

		String sql = "update Subscription set " +
			"Subscribed=?," +
			"SentCount=?," +
			"LastSentTime=?," +
			"OpenCount=?," +
			"LastOpenTime=?," +
			"ClickCount=?," +
			"LastClickTime=?" +
			" where EmailAddrId=? and ListId=?";
		
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int updateSentCount(long emailAddrId, String listId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update Subscription set " +
			"SentCount=SentCount+1, " +
			"LastSentTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	public int updateOpenCount(long emailAddrId, String listId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update Subscription set " +
			"OpenCount=OpenCount+1, " +
			"LastOpenTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	public int updateClickCount(long emailAddrId, String listId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update Subscription set " +
			"Clickcount=ClickCount+1, " +
			"LastClickTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long addrid, String listId) {
		String sql = "delete from Subscription where EmailAddrId=? and ListId=?";
		Object[] parms = new Object[] {addrid, listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByAddrId(long addrId) {
		String sql = "delete from Subscription where EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByListId(String listId) {
		String sql = "delete from Subscription where ListId=?";
		Object[] parms = new Object[] {listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(SubscriptionVo subscriptionVo) {
		if (subscriptionVo.getCreateTime()==null) {
			subscriptionVo.setCreateTime(new Timestamp(new java.util.Date().getTime()));
		}
		Object[] parms = {
				subscriptionVo.getEmailAddrId(),
				subscriptionVo.getListId(),
				subscriptionVo.getSubscribed(),
				subscriptionVo.getCreateTime(),
				subscriptionVo.getSentCount(),
				subscriptionVo.getLastSentTime(),
				subscriptionVo.getOpenCount(),
				subscriptionVo.getLastOpenTime(),
				subscriptionVo.getClickCount(),
				subscriptionVo.getLastClickTime()
			};
		
		String sql = "INSERT INTO Subscription (" +
			"EmailAddrId," +
			"ListId," +
			"Subscribed," +
			"CreateTime, " +
			"SentCount, " +
			"LastSentTime, " +
			"OpenCount, " +
			"LastOpenTime, " +
			"ClickCount, " +
			"LastClickTime " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		return rowsInserted;
	}
	
	private EmailAddrDao emailAddrDao = null;
	private EmailAddrDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getDaoAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
