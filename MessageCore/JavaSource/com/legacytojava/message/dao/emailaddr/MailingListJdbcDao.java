package com.legacytojava.message.dao.emailaddr;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.vo.emailaddr.MailingListVo;

@Component("mailingListDao")
public class MailingListJdbcDao extends AbstractDao implements MailingListDao {
	static final Logger logger = Logger.getLogger(MailingListJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String getSelectClause() {
		String select = "select " +
				" a.RowId, " +
				" a.ListId, " +
				" a.DisplayName, " +
				" a.AcctUserName, " +
				" c.DomainName, " + 
				" a.Description, " +
				" a.ClientId, " +
				" a.StatusId, " +
				" a.IsBuiltIn, " +
				" a.IsSendText, " +
				" a.CreateTime, " +
				" a.ListMasterEmailAddr, " +
				" '' as Subscribed, " +
				" a.ListId as OrigListId, " +
				" sum(b.SentCount) as SentCount, sum(b.OpenCount) as OpenCount," +
				" sum(b.ClickCount) as ClickCount " +
				"from MailingList a " +
				" LEFT OUTER JOIN Subscription b on a.ListId = b.ListId " +
				" JOIN Clients c on a.ClientId = c.ClientId ";
		return select;
	}

	private String getGroupByClause() {
		String groupBy = "group by " +
				" a.RowId, " +
				" a.ListId, " +
				" a.DisplayName, " +
				" a.AcctUserName, " +
				" c.DomainName, " + 
				" a.Description, " +
				" a.ClientId, " +
				" a.StatusId, " +
				" a.IsBuiltIn, " +
				" a.IsSendText, " +
				" a.CreateTime, " +
				" a.ListMasterEmailAddr ";
		return groupBy;
	}

	public MailingListVo getByListId(String listId) {
		String sql = getSelectClause() +
				" where a.ListId = ? " +
				getGroupByClause();
		Object[] parms = new Object[] {listId};
		try {
			MailingListVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailingListVo> getByAddress(String emailAddr) {
		emailAddr = emailAddr == null ? "" : emailAddr; // just for safety
		String acctUserName = emailAddr;
		String domainName = null;
		int atSignPos = emailAddr.indexOf("@");
		if (atSignPos >= 0) {
			acctUserName = emailAddr.substring(0, atSignPos);
			domainName = emailAddr.substring(atSignPos + 1);
		}
		String sql = getSelectClause() +
			" where a.AcctUserName = ? ";
		if (domainName != null && domainName.trim().length() > 0) {
			sql += " and c.DomainName = '" + domainName + "' ";
		}
		sql += getGroupByClause();
		Object[] parms = new Object[] {acctUserName};
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}
	
	public List<MailingListVo> getAll(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = getSelectClause();
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusIdCode.ACTIVE);
		}
		sql += getGroupByClause();
		sql += " order by a.RowId ";
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}

	public List<MailingListVo> getAllForTrial(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = getSelectClause();
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusIdCode.ACTIVE);
		}
		sql += getGroupByClause();
		sql += " order by a.RowId limit 5";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(5);
		getJdbcTemplate().setMaxRows(5);
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}

	public List<MailingListVo> getSubscribedLists(long emailAddrId) {
		String sql = "SELECT " +
			" m.*, " +
			" c.DomainName, " +
			" s.Subscribed, " +
			" s.SentCount, " +
			" s.OpenCount, " +
			" s.ClickCount " +
			" FROM MailingList m, Subscription s, Clients c " +
			" where m.ListId=s.ListId " +
			" and m.ClientId=c.ClientId " +
			" and s.EmailAddrId=? ";
		Object[] parms = new Object[] {emailAddrId};
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}

	public int update(MailingListVo mailingListVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailingListVo);
		String sql = MetaDataUtil.buildUpdateStatement("MailingList", mailingListVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsUpadted;
	}
	
	public int deleteByListId(String listId) {
		String sql = "delete from MailingList where ListId=?";
		Object[] parms = new Object[] {listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByAddress(String emailAddr) {
		String sql = "delete from MailingList where EmailAddr=?";
		Object[] parms = new Object[] {emailAddr};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailingListVo mailingListVo) {
		mailingListVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailingListVo);
		String sql = MetaDataUtil.buildInsertStatement("MailingList", mailingListVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailingListVo.setRowId(retrieveRowId());
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsInserted;
	}
}
