package com.es.dao.mailbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.address.EmailAddressDao;
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.StatusId;
import com.es.vo.comm.MailBoxVo;
import com.es.vo.comm.SenderDataVo;

@Component("mailBoxDao")
public class MailBoxDao {
	
	@Autowired
	private DataSource msgDataSource;
	@Autowired
	private SenderDataDao senderDao;

	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	// TODO
	private static final class MailBoxMapper implements RowMapper<MailBoxVo> {
		private final String senderDomains;
		MailBoxMapper(String senderDomains) {
			this.senderDomains = senderDomains;
		}
		
		public MailBoxVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			MailBoxVo mailBoxVo = new MailBoxVo();
			
			mailBoxVo.setRowId(rs.getInt("RowId"));
			mailBoxVo.setUserId(rs.getString("UserId"));
			mailBoxVo.setUserPswd(rs.getString("UserPswd"));
			mailBoxVo.setHostName(rs.getString("HostName"));
			mailBoxVo.setPortNumber(rs.getInt("PortNumber"));
			mailBoxVo.setProtocol(rs.getString("Protocol"));
			mailBoxVo.setServerType(rs.getString("ServerType"));
			mailBoxVo.setFolderName(rs.getString("FolderName"));
			mailBoxVo.setMailBoxDesc(rs.getString("MailBoxDesc"));
			mailBoxVo.setStatusId(rs.getString("StatusId"));
			mailBoxVo.setCarrierCode(rs.getString("CarrierCode"));
			mailBoxVo.setInternalOnly(rs.getString("InternalOnly"));
			mailBoxVo.setReadPerPass(rs.getInt("ReadPerPass"));
			mailBoxVo.setUseSsl(rs.getString("UseSsl"));
			mailBoxVo.setThreads(rs.getInt("Threads"));
			mailBoxVo.setRetryMax((Integer)rs.getObject("RetryMax"));
			mailBoxVo.setMinimumWait((Integer)rs.getObject("MinimumWait"));
			mailBoxVo.setMessageCount((Integer)rs.getObject("MessageCount"));
			mailBoxVo.setToPlainText(rs.getString("ToPlainText"));
			//mailBoxVo.setToAddrDomain(rs.getString("ToAddrDomain"));
			mailBoxVo.setToAddrDomain(senderDomains);
			mailBoxVo.setCheckDuplicate(rs.getString("CheckDuplicate"));
			mailBoxVo.setAlertDuplicate(rs.getString("AlertDuplicate"));
			mailBoxVo.setLogDuplicate(rs.getString("LogDuplicate"));
			mailBoxVo.setPurgeDupsAfter((Integer)rs.getObject("PurgeDupsAfter"));
			mailBoxVo.setProcessorName(rs.getString("ProcessorName"));
			mailBoxVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			mailBoxVo.setUpdtUserId(rs.getString("UpdtUserId"));
			
			mailBoxVo.setServerName(mailBoxVo.getHostName()+"."+mailBoxVo.getUserId());
			mailBoxVo.setOrigUpdtTime(mailBoxVo.getUpdtTime());
			return mailBoxVo;
		}
	}
	
	private String getSenderDomains() {
		// retrieve matching domains from SenderData table
		List<SenderDataVo> vos = senderDao.getAll();
		String toDomains = "";
		for (int i = 0; i < vos.size(); i++) {
			if (i > 0) {
				toDomains += ",";
			}
			toDomains += vos.get(i).getDomainName();
		}
		return toDomains;
	}
	
	public MailBoxVo getByPrimaryKey(String userId, String hostName) {
		String sql = "select * from Mail_Box where UserId=? and HostName=?";
		Object[] parms = new Object[] {userId, hostName};
		try {
			MailBoxVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new MailBoxMapper(getSenderDomains()));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailBoxVo> getAll(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from Mail_Box ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by HostName, UserId ";
		List<MailBoxVo> list = (List<MailBoxVo>) getJdbcTemplate().query(sql, keys.toArray(),
				new MailBoxMapper(getSenderDomains()));
		return list;
	}
	
	public List<MailBoxVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from Mail_Box ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by RowId limit 1";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<MailBoxVo> list = (List<MailBoxVo>) getJdbcTemplate().query(sql, keys.toArray(),
				new MailBoxMapper(getSenderDomains()));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public int update(MailBoxVo mailBoxVo) {
		mailBoxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(mailBoxVo.getUserId());
		keys.add(mailBoxVo.getHostName());
		keys.add(mailBoxVo.getUserPswd());
		keys.add(mailBoxVo.getPortNumber());
		keys.add(mailBoxVo.getProtocol());
		keys.add(mailBoxVo.getServerType());
		keys.add(mailBoxVo.getFolderName());
		keys.add(mailBoxVo.getMailBoxDesc());
		keys.add(mailBoxVo.getStatusId());
		keys.add(mailBoxVo.getCarrierCode());
		keys.add(mailBoxVo.getInternalOnly());
		keys.add(mailBoxVo.getReadPerPass());
		keys.add(mailBoxVo.getUseSsl());
		keys.add(mailBoxVo.getThreads());
		keys.add(mailBoxVo.getRetryMax());
		keys.add(mailBoxVo.getMinimumWait());
		keys.add(mailBoxVo.getMessageCount());
		keys.add(mailBoxVo.getToPlainText());
		keys.add(mailBoxVo.getToAddrDomain());
		keys.add(mailBoxVo.getCheckDuplicate());
		keys.add(mailBoxVo.getAlertDuplicate());
		keys.add(mailBoxVo.getLogDuplicate());
		keys.add(mailBoxVo.getPurgeDupsAfter());
		keys.add(mailBoxVo.getProcessorName());
		keys.add(mailBoxVo.getUpdtTime());
		keys.add(mailBoxVo.getUpdtUserId());
		keys.add(mailBoxVo.getRowId());
		
		String sql = "update Mail_Box set " +
			"UserId=?," +
			"HostName=?," +
			"UserPswd=?," +
			"PortNumber=?," +
			"Protocol=?," +
			"ServerType=?," +
			"FolderName=?," +
			"MailBoxDesc=?," +
			"StatusId=?," +
			"CarrierCode=?," +
			"InternalOnly=?," +
			"ReadPerPass=?," +
			"UseSsl=?," +
			"Threads=?," +
			"RetryMax=?," +
			"MinimumWait=?," +
			"MessageCount=?," +
			"ToPlainText=?," +
			"ToAddrDomain=?," +
			"CheckDuplicate=?," +
			"AlertDuplicate=?," +
			"LogDuplicate=?," +
			"PurgeDupsAfter=?," +
			"ProcessorName=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where Rowid=?";
		
		if (mailBoxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(mailBoxVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		mailBoxVo.setOrigUpdtTime(mailBoxVo.getUpdtTime());
		// insert/update EmailAddr record
		getEmailAddrDao().findSertAddress(mailBoxVo.getUserId() + "@" + mailBoxVo.getHostName());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String userId, String hostName) {
		String sql = "delete from Mail_Box where UserId=? and HostName=?";
		Object[] parms = new Object[] {userId, hostName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailBoxVo mailBoxVo) {
		mailBoxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				mailBoxVo.getUserId(),
				mailBoxVo.getUserPswd(),
				mailBoxVo.getHostName(),
				mailBoxVo.getPortNumber(),
				mailBoxVo.getProtocol(),
				mailBoxVo.getServerType(),
				mailBoxVo.getFolderName(),
				mailBoxVo.getMailBoxDesc(),
				mailBoxVo.getStatusId(),
				mailBoxVo.getCarrierCode(),
				mailBoxVo.getInternalOnly(),
				mailBoxVo.getReadPerPass(),
				mailBoxVo.getUseSsl(),
				mailBoxVo.getThreads(),
				mailBoxVo.getRetryMax(),
				mailBoxVo.getMinimumWait(),
				mailBoxVo.getMessageCount(),
				mailBoxVo.getToPlainText(),
				mailBoxVo.getToAddrDomain(),
				mailBoxVo.getCheckDuplicate(),
				mailBoxVo.getAlertDuplicate(),
				mailBoxVo.getLogDuplicate(),
				mailBoxVo.getPurgeDupsAfter(),
				mailBoxVo.getProcessorName(),
				mailBoxVo.getUpdtTime(),
				mailBoxVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO Mail_Box (" +
			"UserId," +
			"UserPswd," +
			"HostName," +
			"PortNumber," +
			"Protocol," +
			"ServerType," +
			"FolderName," +
			"MailBoxDesc," +
			"StatusId," +
			"CarrierCode," +
			"InternalOnly," +
 			"ReadPerPass," +
			"UseSsl," +
			"Threads," +
			"RetryMax," +
			"MinimumWait," +
			"MessageCount," +
			"ToPlainText," +
			"ToAddrDomain," +
			"CheckDuplicate," +
			"AlertDuplicate," +
			"LogDuplicate," +
			"PurgeDupsAfter," +
			"ProcessorName," +
			"UpdtTime," +
			"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		mailBoxVo.setRowId(retrieveRowId());
		mailBoxVo.setOrigUpdtTime(mailBoxVo.getUpdtTime());
		// insert mailbox address to EmailAddr table
		getEmailAddrDao().findSertAddress(mailBoxVo.getUserId() + "@" + mailBoxVo.getHostName());
		return rowsInserted;
	}
	
	@Autowired
	private EmailAddressDao emailAddrDao = null;
	private EmailAddressDao getEmailAddrDao() {
		return emailAddrDao;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
