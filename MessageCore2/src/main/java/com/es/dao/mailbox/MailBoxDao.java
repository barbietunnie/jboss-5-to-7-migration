package com.es.dao.mailbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.MailBoxVo;
import com.es.vo.comm.SenderDataVo;

@Component("mailBoxDao")
public class MailBoxDao extends AbstractDao {
	
	@Autowired
	private SenderDataDao senderDao;

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
		String sql = "select *, '" + getSenderDomains() + "' as ToAddrDomain, " +
				"CONCAT(HostName, '.', UserId) as ServerName, UpdtTime as OrigUpdtTime " +
				"from Mail_Box where UserId=? and HostName=?";
		Object[] parms = new Object[] {userId, hostName};
		try {
			MailBoxVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailBoxVo>(MailBoxVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailBoxVo> getAll(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select *, '" + getSenderDomains() + "' as ToAddrDomain, " +
				"CONCAT(HostName, '.', UserId) as ServerName, UpdtTime as OrigUpdtTime " +
				"from Mail_Box ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by HostName, UserId ";
		List<MailBoxVo> list = (List<MailBoxVo>) getJdbcTemplate().query(sql, keys.toArray(),
				new BeanPropertyRowMapper<MailBoxVo>(MailBoxVo.class));
		return list;
	}
	
	public List<MailBoxVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select *, '" + getSenderDomains() + "' as ToAddrDomain, " +
				"CONCAT(HostName, '.', UserId) as ServerName, UpdtTime as OrigUpdtTime " +
				"from Mail_Box ";
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
				new BeanPropertyRowMapper<MailBoxVo>(MailBoxVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public int update(MailBoxVo mailBoxVo) {
		mailBoxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailBoxVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Mail_Box", mailBoxVo);
		
		if (mailBoxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		mailBoxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailBoxVo);
		String sql = MetaDataUtil.buildInsertStatement("Mail_Box", mailBoxVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);

		mailBoxVo.setRowId(retrieveRowId());
		mailBoxVo.setOrigUpdtTime(mailBoxVo.getUpdtTime());
		// insert mailbox address to EmailAddr table
		getEmailAddrDao().findSertAddress(mailBoxVo.getUserId() + "@" + mailBoxVo.getHostName());
		return rowsInserted;
	}
	
	@Autowired
	private EmailAddressDao emailAddrDao;
	private EmailAddressDao getEmailAddrDao() {
		return emailAddrDao;
	}
	
}
