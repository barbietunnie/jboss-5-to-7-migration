package com.es.dao.address;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.address.MailingListVo;

@Component("mailingListDao")
public class MailingListDao extends AbstractDao {
	static final Logger logger = Logger.getLogger(MailingListDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String selectCluse = "select " +
			" a.RowId, " +
			" a.ListId, " +
			" a.DisplayName, " +
			" a.AcctUserName, " +
			" c.DomainName, " + 
			" a.Description, " +
			" a.SenderId, " +
			" a.StatusId, " +
			" a.IsBuiltIn, " +
			" a.IsSendText, " +
			" a.CreateTime, " +
			" a.ListMasterEmailAddr, " +
			" '' as Subscribed, " +
			" sum(b.SentCount) as SentCount, sum(b.OpenCount) as OpenCount," +
			" sum(b.ClickCount) as ClickCount " +
			"from Mailing_List a " +
			" LEFT OUTER JOIN Subscription b on a.ListId = b.ListId " +
			" JOIN Sender_Data c on a.SenderId = c.SenderId ";
	
	private String groupByCluse = " group by " +
			" a.RowId, " +
			" a.ListId, " +
			" a.DisplayName, " +
			" a.AcctUserName, " +
			" c.DomainName, " + 
			" a.Description, " +
			" a.SenderId, " +
			" a.StatusId, " +
			" a.IsBuiltIn, " +
			" a.IsSendText, " +
			" a.CreateTime, " +
			" a.ListMasterEmailAddr ";
	
	public MailingListVo getByListId(String listId) {
		String sql = selectCluse +
				" where a.ListId = ? " +
				groupByCluse;
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
	
	public MailingListVo getByListAddress(String emailAddr) {
		emailAddr = emailAddr == null ? "" : emailAddr; // just for safety
		String acctUserName = emailAddr;
		String domainName = null;
		int atSignPos = emailAddr.indexOf("@");
		if (atSignPos >= 0) {
			acctUserName = emailAddr.substring(0, atSignPos);
			domainName = emailAddr.substring(atSignPos + 1);
		}
		String sql = selectCluse +
			" where a.AcctUserName = ? ";
		if (StringUtils.isNotBlank(domainName)) {
			sql += " and c.DomainName = '" + domainName + "' ";
		}
		sql += groupByCluse;
		Object[] parms = new Object[] {acctUserName};
		try {
			MailingListVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailingListVo> getAll(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = selectCluse;
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusId.ACTIVE.getValue());
		}
		sql += groupByCluse;
		sql += " order by a.RowId ";
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}

	public List<MailingListVo> getAllForTrial(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = selectCluse;
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusId.ACTIVE.getValue());
		}
		sql += groupByCluse;
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
			" FROM Mailing_List m, Subscription s, Sender_Data c " +
			" where m.ListId=s.ListId " +
			" and m.SenderId=c.SenderId " +
			" and s.EmailAddrId=? ";
		Object[] parms = new Object[] {emailAddrId};
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}

	public int update(MailingListVo mailingListVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailingListVo);

		String sql = MetaDataUtil.buildUpdateStatement("Mailing_List", mailingListVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsUpadted;
	}
	
	public int deleteByListId(String listId) {
		String sql = "delete from Mailing_List where ListId=?";
		Object[] parms = new Object[] {listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByAddress(String emailAddr) {
		String sql = "delete from Mailing_List where EmailAddr=?";
		Object[] parms = new Object[] {emailAddr};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailingListVo mailingListVo) {
		mailingListVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailingListVo);
				
		String sql = MetaDataUtil.buildInsertStatement("Mailing_List", mailingListVo);
		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailingListVo.setRowId(retrieveRowId());
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsInserted;
	}
}
