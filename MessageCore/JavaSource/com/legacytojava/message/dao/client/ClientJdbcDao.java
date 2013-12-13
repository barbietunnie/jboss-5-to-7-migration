package com.legacytojava.message.dao.client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.common.TimestampUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.constant.VariableType;
import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.dao.template.ClientVariableDao;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.message.vo.template.ClientVariableVo;

@Component("clientDao")
public class ClientJdbcDao extends AbstractDao implements ClientDao {
	
	final static Map<String, ClientVo> clientCache = new HashMap<String, ClientVo>();
	private java.util.Date lastFetchTime = new java.util.Date();

	public ClientVo getByClientId(String clientId) {
		java.util.Date currTime = new java.util.Date();
		if (currTime.getTime() - lastFetchTime.getTime() > (15*60*1000)) {
			// reload every 15 minutes
			synchronized (clientCache) {
				clientCache.clear();
			}
			lastFetchTime = currTime;
		}
		if (!clientCache.containsKey(clientId)) {
			String sql = "select *, ClientId as OrigClientId, UpdtTime as OrigUpdtTime " +
					"from Clients where clientid=?";
			Object[] parms = new Object[] { clientId };
			try {
				ClientVo vo = getJdbcTemplate().queryForObject(sql, parms, 
						new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
				synchronized (clientCache) {
					clientCache.put(clientId, vo);
				}
			}
			catch (EmptyResultDataAccessException e) {
			}
		}
		return (ClientVo) BlobUtil.deepCopy(clientCache.get(clientId));
	}

	public ClientVo getByDomainName(String domainName) {
		String sql = "select *, ClientId as OrigClientId, UpdtTime as OrigUpdtTime " +
				"from Clients where DomainName=?";
		Object[] parms = new Object[] { domainName };
		List<ClientVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
		if (list.size() > 0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}

	public List<ClientVo> getAll() {
		String sql = 
			"select *, ClientId as OrigClientId, UpdtTime as OrigUpdtTime " +
				"from Clients order by clientId";
		
		List<ClientVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
		return list;
	}
	
	public List<ClientVo> getAllForTrial() {
		String sql = 
			"select *, ClientId as OrigClientId, UpdtTime as OrigUpdtTime " +
				"from Clients " +
			" order by RowId " +
			" limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<ClientVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<ClientVo>(ClientVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public String getSystemId() {
		String sql = 
			"select SystemId " +
				"from Clients where ClientId='" + Constants.DEFAULT_CLIENTID + "'";
		return (String) getJdbcTemplate().queryForObject(sql, String.class);
	}

	public String getSystemKey() {
		String sql = 
			"select SystemKey " +
				"from Clients where ClientId='" + Constants.DEFAULT_CLIENTID + "'";
		return (String) getJdbcTemplate().queryForObject(sql, String.class);
	}

	public synchronized int updateSystemKey(String key) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(key);
		String sql = "update Clients set " +
			"SystemKey=? " +
			" where ClientId= '" + Constants.DEFAULT_CLIENTID + "'";
		int rowsUpdated = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpdated;
	}

	public synchronized int update(ClientVo clientVo) {
		clientVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		validateClientVo(clientVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVo);
		String sql = MetaDataUtil.buildUpdateStatement("Clients", clientVo);
		if (clientVo.getOrigUpdtTime() != null) {
			// optimistic locking
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		synchronized (clientCache) {
			clientCache.remove(clientVo.getClientId()); // remove from cache
		}
		updateClientVariables(clientVo);
		clientVo.setOrigUpdtTime(clientVo.getUpdtTime());
		clientVo.setOrigClientId(clientVo.getClientId());
		updateReloadFlags();
		return rowsUpadted;
	}

	public synchronized int delete(String clientId) {
		if (Constants.DEFAULT_CLIENTID.equals(clientId)) {
			throw new IllegalArgumentException("Can't delete System Default Client.");
		}
		String sql = "delete from Clients where clientid=?";
		Object[] parms = new Object[] {clientId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		synchronized (clientCache) {
			clientCache.remove(clientId); // remove from cache			
		}
		deleteClientVariables(clientId);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(ClientVo clientVo) {
		clientVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		validateClientVo(clientVo);
		String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp());
		clientVo.setSystemId(systemId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVo);
		String sql = MetaDataUtil.buildInsertStatement("Clients", clientVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		clientVo.setRowId(retrieveRowId());
		clientVo.setOrigUpdtTime(clientVo.getUpdtTime());
		clientVo.setOrigClientId(clientVo.getClientId());
		updateClientVariables(clientVo);
		updateReloadFlags();
		return rowsInserted;
	}
	
	public static void validateClientVo(ClientVo clientVo) {
		if (clientVo.getUseTestAddress()) {
			if (StringUtil.isEmpty(clientVo.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (clientVo.getIsVerpAddressEnabled()) {
			if (StringUtil.isEmpty(clientVo.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtil.isEmpty(clientVo.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}
	
	/**
	 * A synchronization method that updates client variables based on the new
	 * Client record.
	 * 
	 * @return number of rows inserted
	 */
	private int updateClientVariables(ClientVo clientVo) {
		getClientVariableDao().deleteByClientId(clientVo.getClientId());
		if (StringUtil.isEmpty(clientVo.getOrigClientId())) {
			getClientVariableDao().deleteByClientId(clientVo.getOrigClientId());
		}
		int rowsInserted = 0;
		ClientVariableVo vo = new ClientVariableVo();
		vo.setClientId(clientVo.getClientId());
		vo.setStatusId(StatusIdCode.ACTIVE);
		vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
		
		vo.setVariableName("DomainName");
		vo.setVariableValue(clientVo.getDomainName());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("SiteName");
		vo.setVariableValue(clientVo.getClientName());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		if (!StringUtil.isEmpty(clientVo.getWebSiteUrl())) {
			vo.setVariableName("WebSiteUrl");
			vo.setVariableValue(clientVo.getWebSiteUrl());
			vo.setVariableFormat(null);
			vo.setVariableType(VariableType.TEXT);
			vo.setAllowOverride(Constants.YES_CODE);
			vo.setRequired(Constants.NO_CODE);
			getClientVariableDao().insert(vo);
			rowsInserted++;
		}
		
		vo.setVariableName("ContactEmailAddress");
		vo.setVariableValue(clientVo.getContactEmail());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		if (!StringUtil.isEmpty(clientVo.getContactPhone())) {
			vo.setVariableName("ContactPhoneNumber");
			vo.setVariableValue(clientVo.getContactPhone());
			vo.setVariableFormat(null);
			vo.setVariableType(VariableType.TEXT);
			vo.setAllowOverride(Constants.YES_CODE);
			vo.setRequired(Constants.NO_CODE);
			getClientVariableDao().insert(vo);
			rowsInserted++;
		}
		
		vo.setVariableName("ClientId");
		vo.setVariableValue(clientVo.getClientId());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentDateTime");
		vo.setVariableValue(null);
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.DATETIME);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentDate");
		vo.setVariableValue(null);
		vo.setVariableFormat("yyyy-MM-dd");
		vo.setVariableType(VariableType.DATETIME);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentTime");
		vo.setVariableValue(null);
		vo.setVariableFormat("hh:mm:ss a");
		vo.setVariableType(VariableType.DATETIME);
		vo.setAllowOverride(Constants.YES_CODE);
		vo.setRequired(Constants.NO_CODE);
		getClientVariableDao().insert(vo);
		rowsInserted++;
		
		return rowsInserted;
	}

	private int deleteClientVariables(String clientId) {
		int rowsDeleted = getClientVariableDao().deleteByClientId(clientId);
		return rowsDeleted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateClientReloadFlag();
	}
	
	@Autowired
	private ClientVariableDao clientVariableDao;
	private synchronized ClientVariableDao getClientVariableDao() {
		return clientVariableDao;
	}
	
	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
}
