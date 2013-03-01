package com.legacytojava.message.dao.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.common.TimestampUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.constant.VariableType;
import com.legacytojava.message.dao.template.ClientVariableDao;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.message.vo.template.ClientVariableVo;

@Component("clientDao")
public class ClientJdbcDao implements ClientDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	private java.util.Date lastFetchTime = new java.util.Date();
	
	final static Map<String, ClientVo> clientCache = new HashMap<String, ClientVo>();

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class ClientMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClientVo clientVo = new ClientVo();
			
			clientVo.setPrimaryKey(rs.getString("ClientId"));
			
			clientVo.setRowId(rs.getInt("RowId"));
			clientVo.setClientId(rs.getString("ClientId"));
			clientVo.setClientName(rs.getString("ClientName"));
			clientVo.setClientType(rs.getString("ClientType"));
			clientVo.setDomainName(rs.getString("DomainName"));
			clientVo.setStatusId(rs.getString("StatusId"));
			clientVo.setIrsTaxId(rs.getString("IrsTaxId"));
			clientVo.setWebSiteUrl(rs.getString("WebSiteUrl"));
			clientVo.setSaveRawMsg(rs.getString("SaveRawMsg"));
			clientVo.setContactName(rs.getString("ContactName"));
			clientVo.setContactPhone(rs.getString("ContactPhone"));
			clientVo.setContactEmail(rs.getString("ContactEmail"));
			clientVo.setSecurityEmail(rs.getString("SecurityEmail"));
			clientVo.setCustcareEmail(rs.getString("CustcareEmail"));
			clientVo.setRmaDeptEmail(rs.getString("RmaDeptEmail"));
			clientVo.setSpamCntrlEmail(rs.getString("SpamCntrlEmail"));
			clientVo.setChaRspHndlrEmail(rs.getString("ChaRspHndlrEmail"));
			clientVo.setEmbedEmailId(rs.getString("EmbedEmailId"));
			clientVo.setReturnPathLeft(rs.getString("ReturnPathLeft"));
			clientVo.setUseTestAddr(rs.getString("UseTestAddr"));
			clientVo.setTestFromAddr(rs.getString("TestFromAddr"));
			clientVo.setTestToAddr(rs.getString("TestToAddr"));
			clientVo.setTestReplytoAddr(rs.getString("TestReplytoAddr"));
			clientVo.setIsVerpEnabled(rs.getString("IsVerpEnabled"));
			clientVo.setVerpSubDomain(rs.getString("VerpSubDomain"));
			clientVo.setVerpInboxName(rs.getString("VerpInboxName"));
			clientVo.setVerpRemoveInbox(rs.getString("VerpRemoveInbox"));
			clientVo.setSystemId(rs.getString("SystemId"));
			clientVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			clientVo.setUpdtUserId(rs.getString("UpdtUserId"));
			
			clientVo.setOrigUpdtTime(clientVo.getUpdtTime());
			clientVo.setOrigClientId(clientVo.getClientId());
			
			return clientVo;
		}
	} 
	
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
			String sql = "select * from Clients where clientid=?";
			Object[] parms = new Object[] { clientId };
			List<?> list = getJdbcTemplate().query(sql, parms, new ClientMapper());
			if (list.size() > 0) {
				ClientVo clientVo = (ClientVo) list.get(0);
				synchronized (clientCache) {
					clientCache.put(clientId, clientVo);					
				}
			}
		}
		return (ClientVo) BlobUtil.deepCopy(clientCache.get(clientId));
	}

	public ClientVo getByDomainName(String domainName) {
		String sql = "select * from Clients where DomainName=?";
		Object[] parms = new Object[] { domainName };
		List<?> list = getJdbcTemplate().query(sql, parms, new ClientMapper());
		if (list.size() > 0) {
			return (ClientVo) list.get(0);
		}
		else {
			return null;
		}
	}

	public List<ClientVo> getAll() {
		String sql = 
			"select * " +
				"from Clients order by clientId";
		
		@SuppressWarnings("unchecked")
		List<ClientVo> list = (List<ClientVo>)getJdbcTemplate().query(sql, new ClientMapper());
		return list;
	}
	
	public List<ClientVo> getAllForTrial() {
		String sql = 
			"select * " +
				"from Clients " +
			" order by RowId " +
			" limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		@SuppressWarnings("unchecked")
		List<ClientVo> list =  (List<ClientVo>)getJdbcTemplate().query(sql, new ClientMapper());
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
		clientVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		validateClientVo(clientVo);
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(clientVo.getClientId());
		keys.add(clientVo.getClientName());
		keys.add(clientVo.getClientType());
		keys.add(clientVo.getDomainName());
		keys.add(clientVo.getStatusId());
		keys.add(clientVo.getIrsTaxId());
		keys.add(clientVo.getWebSiteUrl());
		keys.add(clientVo.getSaveRawMsg());
		keys.add(clientVo.getContactName());
		keys.add(clientVo.getContactPhone());
		keys.add(clientVo.getContactEmail());
		keys.add(clientVo.getSecurityEmail());
		keys.add(clientVo.getCustcareEmail());
		keys.add(clientVo.getRmaDeptEmail());
		keys.add(clientVo.getSpamCntrlEmail());
		keys.add(clientVo.getChaRspHndlrEmail());
		keys.add(clientVo.getEmbedEmailId());
		keys.add(clientVo.getReturnPathLeft());
		keys.add(clientVo.getUseTestAddr());
		keys.add(clientVo.getTestFromAddr());
		keys.add(clientVo.getTestToAddr());
		keys.add(clientVo.getTestReplytoAddr());
		keys.add(clientVo.getIsVerpEnabled());
		keys.add(clientVo.getVerpSubDomain());
		keys.add(clientVo.getVerpInboxName());
		keys.add(clientVo.getVerpRemoveInbox());
		keys.add(clientVo.getUpdtTime());
		keys.add(clientVo.getUpdtUserId());
		keys.add(clientVo.getRowId());
		
		String sql = "update Clients set " +
			"ClientId=?," +
			"ClientName=?," +
			"ClientType=?," +
			"DomainName=?," +
			"StatusId=?," +
			"IrsTaxId=?," +
			"WebSiteUrl=?," +
			"SaveRawMsg=?," +
			"ContactName=?," +
			"ContactPhone=?," +
			"ContactEmail=?," +
			"SecurityEmail=?," +
			"CustcareEmail=?," +
			"RmaDeptEmail=?," +
			"SpamCntrlEmail=?," +
			"ChaRspHndlrEmail=?," +
			"EmbedEmailId=?," +
			"ReturnPathLeft=?," +
			"UseTestAddr=?," +
			"TestFromAddr=?," +
			"TestToAddr=?," +
			"TestReplytoAddr=?," +
			"IsVerpEnabled=?," +
			"VerpSubDomain=?," +
			"VerpInboxName=?," +
			"VerpRemoveInbox=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where RowId=?";
		
		if (clientVo.getOrigUpdtTime() != null) {
			// optimistic locking
			sql += " and UpdtTime=?";
			keys.add(clientVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
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
		clientVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		validateClientVo(clientVo);
		String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp());
		Object[] parms = {
				clientVo.getClientId(),
				clientVo.getClientName(),
				clientVo.getClientType(),
				clientVo.getDomainName(),
				clientVo.getStatusId(),
				clientVo.getIrsTaxId(),
				clientVo.getWebSiteUrl(),
				clientVo.getSaveRawMsg(),
				clientVo.getContactName(),
				clientVo.getContactPhone(),
				clientVo.getContactEmail(),
				clientVo.getSecurityEmail(),
				clientVo.getCustcareEmail(),
				clientVo.getRmaDeptEmail(),
				clientVo.getSpamCntrlEmail(),
				clientVo.getChaRspHndlrEmail(),
				clientVo.getEmbedEmailId(),
				clientVo.getReturnPathLeft(),
				clientVo.getUseTestAddr(),
				clientVo.getTestFromAddr(),
				clientVo.getTestToAddr(),
				clientVo.getTestReplytoAddr(),
				clientVo.getIsVerpEnabled(),
				clientVo.getVerpSubDomain(),
				clientVo.getVerpInboxName(),
				clientVo.getVerpRemoveInbox(),
				systemId,
				clientVo.getUpdtTime(),
				clientVo.getUpdtUserId()
			};
		String sql = 
			"INSERT INTO Clients " +
			"(ClientId, " +
			"ClientName," +
			"ClientType," +
			"DomainName," +
			"StatusId," +
			"IrsTaxId," +
			"WebSiteUrl," +
			"SaveRawMsg," +
			"ContactName," +
			"ContactPhone," +
			"ContactEmail," +
			"SecurityEmail," +
			"CustcareEmail," +
			"RmaDeptEmail," +
			"SpamCntrlEmail," +
			"ChaRspHndlrEmail," +
			"EmbedEmailId," +
			"ReturnPathLeft," +
			"UseTestAddr," +
			"TestFromAddr," +
			"TestToAddr," +
			"TestReplytoAddr," +
			"IsVerpEnabled," +
			"VerpSubDomain," +
			"VerpInboxName," +
			"VerpRemoveInbox," +
			"SystemId," +
			"UpdtTime," +
			"UpdtUserId) " +
			"VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ? )";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
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
	private ClientVariableDao clientVariableDao = null;
	private synchronized ClientVariableDao getClientVariableDao() {
		return clientVariableDao;
	}
	
	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
