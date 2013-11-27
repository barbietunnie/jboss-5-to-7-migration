package com.es.dao.address;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.core.util.SpringUtil;
import com.es.core.util.StringUtil;
import com.es.data.constant.CodeType;
import com.es.data.constant.MsgStatusCode;
import com.es.data.constant.StatusId;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.address.SubscriptionVo;
import com.es.vo.comm.PagingVo;

@Component("subscriptionDao")
public class SubscriptionDao {
	static final Logger logger = Logger.getLogger(SubscriptionDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
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
			vo.setSubscribed(CodeType.YES_CODE.getValue());
			rowsAffected = insert(vo);
		}
		else { // update
			if (!(CodeType.YES_CODE.getValue().equals(vo.getSubscribed()))) {
				vo.setSubscribed(CodeType.YES_CODE.getValue());
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	public int subscribe(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddrDao().findSertAddress(addr);
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
			if (!CodeType.NO_CODE.getValue().equals(vo.getSubscribed())) {
				vo.setSubscribed(CodeType.NO_CODE.getValue());
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}

	public int unsubscribe(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddrDao().getByAddress(addr);
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
			vo.setSubscribed(MsgStatusCode.PENDING.getValue());
			rowsAffected = insert(vo);
		}
		else { // update
			if (!CodeType.YES_CODE.getValue().equals(vo.getSubscribed())
					&& !MsgStatusCode.PENDING.getValue().equals(vo.getSubscribed())) {
				vo.setSubscribed(MsgStatusCode.PENDING.getValue());
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	public int optInRequest(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddrDao().findSertAddress(addr);
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
			if (MsgStatusCode.PENDING.getValue().equals(vo.getSubscribed())) {
				vo.setSubscribed(CodeType.YES_CODE.getValue());
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	public int optInConfirm(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddrDao().findSertAddress(addr);
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
				" join Email_Address b on a.EmailAddrId=b.EmailAddrId " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	private String selectClause = "select a.EmailAddrId, " +
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
			" JOIN Email_Address b ON a.EmailAddrId=b.EmailAddrId ";
	
	
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
			selectClause +
				" LEFT OUTER JOIN Subscriber c on a.EmailAddrId=c.EmailAddrId " +
			whereSql +
			" order by a.EmailAddrId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
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
		if (!StringUtils.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " b.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		//whereSql += CRIT[parms.size()] + " a.Subscribed = ? ";
		//parms.add(CodeType.YES_CODE.getValue());
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
	
	public List<SubscriptionVo> getSubscribers(String listId) {
		String sql = 
			selectClause +
				" LEFT OUTER JOIN Subscriber c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusId.ACTIVE.getValue(), CodeType.YES_CODE.getValue()};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	public List<SubscriptionVo> getSubscribersWithCustomerRecord(String listId) {
		String sql = 
			selectClause +
				" JOIN Subscriber c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusId.ACTIVE.getValue(), CodeType.YES_CODE.getValue()};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
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
				" JOIN Email_Address b ON a.EmailAddrId=b.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? " +
				" and not exists (select 1 from Subscriber where EmailAddrId=b.EmailAddrId) ";
		Object[] parms = new Object[] {listId, StatusId.ACTIVE.getValue(), CodeType.YES_CODE.getValue()};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	public SubscriptionVo getByPrimaryKey(long addrId, String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN Email_Address b ON a.EmailAddrId=b.EmailAddrId " +
				" where a.EmailAddrId=? and a.ListId=?";
		Object[] parms = new Object[] {addrId, listId};
		try {
			SubscriptionVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public SubscriptionVo getByAddrAndListId(String addr, String listId) {
		String sql = "SELECT a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN Email_Address b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=? and b.EmailAddr=?";
		Object[] parms = new Object[] {listId, addr};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		if (list.size()>0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	public List<SubscriptionVo> getByAddrId(long addrId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN Email_Address b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	public List<SubscriptionVo> getByListId(String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN Email_Address b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=?";
		Object[] parms = new Object[] {listId};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
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
	
	private EmailAddressDao emailAddrDao = null;
	private EmailAddressDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
