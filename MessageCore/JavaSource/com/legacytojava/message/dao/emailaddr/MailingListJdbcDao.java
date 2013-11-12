package com.legacytojava.message.dao.emailaddr;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.emailaddr.MailingListVo;

@Component("mailingListDao")
public class MailingListJdbcDao implements MailingListDao {
	static final Logger logger = Logger.getLogger(MailingListJdbcDao.class);
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

	private static final class MailingListMapper implements RowMapper<MailingListVo> {
		
		public MailingListVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			MailingListVo mailingListVo = new MailingListVo();
			
			mailingListVo.setRowId(rs.getInt("RowId"));
			mailingListVo.setListId(rs.getString("ListId"));
			mailingListVo.setDisplayName(rs.getString("DisplayName"));
			mailingListVo.setAcctUserName(rs.getString("AcctUserName"));
			mailingListVo.setDomainName(rs.getString("DomainName"));
			mailingListVo.setDescription(rs.getString("Description"));
			mailingListVo.setClientId(rs.getString("ClientId"));
			mailingListVo.setStatusId(rs.getString("StatusId"));
			mailingListVo.setIsBuiltIn(rs.getString("IsBuiltIn"));
			mailingListVo.setIsSendText(rs.getString("IsSendText"));
			mailingListVo.setCreateTime(rs.getTimestamp("CreateTime"));
			mailingListVo.setListMasterEmailAddr(rs.getString("ListMasterEmailAddr"));
			
			mailingListVo.setSubscribed(rs.getString("Subscribed"));
			mailingListVo.setSentCount(toInteger(rs.getObject("SentCount")));
			mailingListVo.setOpenCount(toInteger(rs.getObject("OpenCount")));
			mailingListVo.setClickCount(toInteger(rs.getObject("ClickCount")));
			
			mailingListVo.setOrigListId(mailingListVo.getListId());
			return mailingListVo;
		}
	}
	
	private static Integer toInteger(Object count) {
		Integer intCount = null;
		if (count instanceof BigDecimal) {
			intCount = count == null ? null : ((BigDecimal) count).intValue();
		}
		else if (count instanceof Integer){
			intCount = (Integer)count;
		}
		return intCount;
	}
	
	public MailingListVo getByListId(String listId) {
		String sql = "select " +
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
				" sum(b.SentCount) as SentCount, sum(b.OpenCount) as OpenCount," +
				" sum(b.ClickCount) as ClickCount " +
				"from MailingList a " +
				" LEFT OUTER JOIN Subscription b on a.ListId = b.ListId " +
				" JOIN Clients c on a.ClientId = c.ClientId " +
				" where a.ListId = ? " +
				"group by " +
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
		Object[] parms = new Object[] {listId};
		List<?> list = (List<?>) getJdbcTemplate().query(sql, parms, new MailingListMapper());
		if (list.size()>0) {
			return (MailingListVo)list.get(0);
		}
		else {
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
		String sql = "select " +
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
			" a.CreateTime," +
			" a.ListMasterEmailAddr, " +
			" '' as Subscribed, " +
			" sum(b.SentCount) as SentCount, sum(b.OpenCount) as OpenCount," +
			" sum(b.ClickCount) as ClickCount " +
			"from MailingList a " +
			" LEFT OUTER JOIN Subscription b on a.ListId = b.ListId " +
			" JOIN Clients c on a.ClientId = c.ClientId " +
			" where a.AcctUserName = ? ";
		if (domainName != null && domainName.trim().length() > 0) {
			sql += " and c.DomainName = '" + domainName + "' ";
		}
		sql +=
			"group by " +
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
			" a.ListMasterEmailAddr " ;
		Object[] parms = new Object[] {acctUserName};
		List<MailingListVo> list = (List<MailingListVo>) getJdbcTemplate().query(sql, parms,
				new MailingListMapper());
		return list;
	}
	
	public List<MailingListVo> getAll(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = "select " +
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
			" a.CreateTime," +
			" a.ListMasterEmailAddr, " +
			" '' as Subscribed, " +
			" sum(b.SentCount) as SentCount, sum(b.OpenCount) as OpenCount," +
			" sum(b.ClickCount) as ClickCount " +
			"from MailingList a " +
			" LEFT OUTER JOIN Subscription b on a.ListId = b.ListId " +
			" JOIN Clients c on a.ClientId = c.ClientId ";
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusIdCode.ACTIVE);
		}
		sql += " group by " +
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
		" a.ListMasterEmailAddr " ;
		sql += " order by a.RowId ";
		List<MailingListVo> list = (List<MailingListVo>) getJdbcTemplate().query(sql, parms.toArray(),
				new MailingListMapper());
		return list;
	}

	public List<MailingListVo> getAllForTrial(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = "select " +
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
			" a.CreateTime," +
			" a.ListMasterEmailAddr, " +
			" '' as Subscribed, " +
			" sum(b.SentCount) as SentCount, sum(b.OpenCount) as OpenCount," +
			" sum(b.ClickCount) as ClickCount " +
			"from MailingList a " +
			" LEFT OUTER JOIN Subscription b on a.ListId = b.ListId " +
			" JOIN Clients c on a.ClientId = c.ClientId ";
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusIdCode.ACTIVE);
		}
		sql += " group by " +
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
		" a.ListMasterEmailAddr " ;
		sql += " order by a.RowId limit 5";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(5);
		getJdbcTemplate().setMaxRows(5);
		List<MailingListVo> list = (List<MailingListVo>) getJdbcTemplate().query(sql, parms.toArray(),
				new MailingListMapper());
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
		List<MailingListVo> list = (List<MailingListVo>) getJdbcTemplate().query(sql, parms,
				new MailingListMapper());
		return list;
	}

	public int update(MailingListVo mailingListVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(mailingListVo.getListId());
		keys.add(mailingListVo.getDisplayName());
		keys.add(mailingListVo.getAcctUserName());
		keys.add(mailingListVo.getDescription());
		keys.add(mailingListVo.getClientId());
		keys.add(mailingListVo.getStatusId());
		keys.add(mailingListVo.getIsBuiltIn());
		keys.add(mailingListVo.getIsSendText());
		keys.add(mailingListVo.getListMasterEmailAddr());
		keys.add(mailingListVo.getRowId());
		String sql = "update MailingList set " +
			"ListId=?," +
			"DisplayName=?," +
			"AcctUserName=?," +
			"Description=?," +
			"ClientId=?," +
			"StatusId=?," +
			"IsBuiltIn=?, " +
			"IsSendText=?, " +
			"ListMasterEmailAddr=?" +
			" where RowId=?";
		
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
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
		mailingListVo.setCreateTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				mailingListVo.getListId(),
				mailingListVo.getDisplayName(),
				mailingListVo.getAcctUserName(),
				mailingListVo.getDescription(),
				mailingListVo.getClientId(),
				mailingListVo.getStatusId(),
				mailingListVo.getIsBuiltIn(),
				mailingListVo.getIsSendText(),
				mailingListVo.getCreateTime(),
				mailingListVo.getListMasterEmailAddr()
			};
		
		String sql = "INSERT INTO MailingList (" +
			"ListId," +
			"DisplayName," +
			"AcctUserName," +
			"Description," +
			"ClientId," +
			"StatusId," +
			"IsBuiltIn," +
			"IsSendText," +
			"CreateTime, " +
			"ListMasterEmailAddr " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "+
				")";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		mailingListVo.setRowId(retrieveRowId());
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
