package com.es.dao.sender;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.core.util.BlobUtil;
import com.es.core.util.TimestampUtil;
import com.es.dao.abst.AbstractDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.data.constant.VariableType;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.comm.SenderDataVo;
import com.es.vo.template.SenderVariableVo;

@Component("senderDataDao")
public class SenderDataDao extends AbstractDao {
	
	private java.util.Date lastFetchTime = new java.util.Date();
	
	final static Map<String, SenderDataVo> senderCache = new HashMap<String, SenderDataVo>();

	public SenderDataVo getBySenderId(String senderId) {
		java.util.Date currTime = new java.util.Date();
		if (currTime.getTime() - lastFetchTime.getTime() > (15*60*1000)) {
			// reload every 15 minutes
			synchronized (senderCache) {
				senderCache.clear();
			}
			lastFetchTime = currTime;
		}
		if (!senderCache.containsKey(senderId)) {
			String sql = "select * from Sender_Data where senderid=?";
			Object[] parms = new Object[] { senderId };
			try {
				SenderDataVo senderVo = getJdbcTemplate().queryForObject(sql, parms,
						new BeanPropertyRowMapper<SenderDataVo>(SenderDataVo.class));
				synchronized (senderCache) {
					senderCache.put(senderId, senderVo);					
				}
			}
			catch (EmptyResultDataAccessException e) {
				return null;
			}
		}
		return (SenderDataVo) BlobUtil.deepCopy(senderCache.get(senderId));
	}

	public List<SenderDataVo> getAll() {
		String sql = 
			"select * " +
				"from Sender_Data order by senderId";
		
		List<SenderDataVo> list = (List<SenderDataVo>)getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<SenderDataVo>(SenderDataVo.class));
		return list;
	}
	
	public List<SenderDataVo> getAllForTrial() {
		String sql = 
			"select * " +
				"from Sender_Data " +
			" order by RowId " +
			" limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<SenderDataVo> list =  (List<SenderDataVo>)getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<SenderDataVo>(SenderDataVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	public String getSystemId() {
		String sql = 
			"select SystemId " +
				"from Sender_Data where SenderId='" + Constants.DEFAULT_SENDER_ID + "'";
		return (String) getJdbcTemplate().queryForObject(sql, String.class);
	}

	public String getSystemKey() {
		String sql = 
			"select SystemKey " +
				"from Sender_Data where SenderId='" + Constants.DEFAULT_SENDER_ID + "'";
		return (String) getJdbcTemplate().queryForObject(sql, String.class);
	}

	public synchronized int updateSystemKey(String key) {
		List<Object> keys = new ArrayList<Object>();
		keys.add(key);
		String sql = "update Sender_Data set " +
			"SystemKey=? " +
			" where SenderId= '" + Constants.DEFAULT_SENDER_ID + "'";
		int rowsUpdated = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpdated;
	}

	public synchronized int update(SenderDataVo senderVo) {
		senderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		validateSenderVo(senderVo);
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(senderVo);
				
		String sql = MetaDataUtil.buildUpdateStatement("Sender_Data", senderVo);
		if (senderVo.getOrigUpdtTime() != null) {
			// optimistic locking
			sql += " and UpdtTime=:origUpdtTime";
		}

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		
		synchronized (senderCache) {
			senderCache.remove(senderVo.getSenderId()); // remove from cache
		}
		updateSenderVariables(senderVo);
		senderVo.setOrigUpdtTime(senderVo.getUpdtTime());
		senderVo.setOrigSenderId(senderVo.getSenderId());
		updateReloadFlags();
		return rowsUpadted;
	}

	public synchronized int delete(String senderId) {
		if (Constants.DEFAULT_SENDER_ID.equals(senderId)) {
			throw new IllegalArgumentException("Can't delete System Default Sender_Data.");
		}
		String sql = "delete from Sender_Data where senderid=?";
		Object[] parms = new Object[] {senderId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		synchronized (senderCache) {
			senderCache.remove(senderId); // remove from cache			
		}
		deleteSenderVariables(senderId);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(SenderDataVo senderVo) {
		senderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		validateSenderVo(senderVo);
		String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.getCurrentDb2Tms());
		senderVo.setSystemId(systemId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(senderVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Sender_Data", senderVo);

		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		
		senderVo.setRowId(retrieveRowId());
		senderVo.setOrigUpdtTime(senderVo.getUpdtTime());
		senderVo.setOrigSenderId(senderVo.getSenderId());
		updateSenderVariables(senderVo);
		updateReloadFlags();
		return rowsInserted;
	}
	
	public static void validateSenderVo(SenderDataVo senderVo) {
		if (senderVo.getUseTestAddress()) {
			if (StringUtils.isEmpty(senderVo.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (senderVo.getIsVerpAddressEnabled()) {
			if (StringUtils.isEmpty(senderVo.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtils.isEmpty(senderVo.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}
	
	/**
	 * A synchronization method that updates sender variables based on the new
	 * Sender record.
	 * 
	 * @return number of rows inserted
	 */
	private int updateSenderVariables(SenderDataVo senderVo) {
		getSenderVariableDao().deleteBySenderId(senderVo.getSenderId());
		if (StringUtils.isEmpty(senderVo.getOrigSenderId())) {
			getSenderVariableDao().deleteBySenderId(senderVo.getOrigSenderId());
		}
		int rowsInserted = 0;
		SenderVariableVo vo = new SenderVariableVo();
		vo.setSenderId(senderVo.getSenderId());
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
		
		vo.setVariableName("DomainName");
		vo.setVariableValue(senderVo.getDomainName());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("SiteName");
		vo.setVariableValue(senderVo.getSenderName());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		if (!StringUtils.isEmpty(senderVo.getWebSiteUrl())) {
			vo.setVariableName("WebSiteUrl");
			vo.setVariableValue(senderVo.getWebSiteUrl());
			vo.setVariableFormat(null);
			vo.setVariableType(VariableType.TEXT.getValue());
			vo.setAllowOverride(CodeType.YES_CODE.getValue());
			vo.setRequired(CodeType.NO_CODE.getValue());
			getSenderVariableDao().insert(vo);
			rowsInserted++;
		}
		
		vo.setVariableName("ContactEmailAddress");
		vo.setVariableValue(senderVo.getContactEmail());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		if (!StringUtils.isEmpty(senderVo.getContactPhone())) {
			vo.setVariableName("ContactPhoneNumber");
			vo.setVariableValue(senderVo.getContactPhone());
			vo.setVariableFormat(null);
			vo.setVariableType(VariableType.TEXT.getValue());
			vo.setAllowOverride(CodeType.YES_CODE.getValue());
			vo.setRequired(CodeType.NO_CODE.getValue());
			getSenderVariableDao().insert(vo);
			rowsInserted++;
		}
		
		vo.setVariableName("SenderId");
		vo.setVariableValue(senderVo.getSenderId());
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.TEXT.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentDateTime");
		vo.setVariableValue(null);
		vo.setVariableFormat(null);
		vo.setVariableType(VariableType.DATETIME.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentDate");
		vo.setVariableValue(null);
		vo.setVariableFormat("yyyy-MM-dd");
		vo.setVariableType(VariableType.DATETIME.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		vo.setVariableName("CurrentTime");
		vo.setVariableValue(null);
		vo.setVariableFormat("hh:mm:ss a");
		vo.setVariableType(VariableType.DATETIME.getValue());
		vo.setAllowOverride(CodeType.YES_CODE.getValue());
		vo.setRequired(CodeType.NO_CODE.getValue());
		getSenderVariableDao().insert(vo);
		rowsInserted++;
		
		return rowsInserted;
	}

	private int deleteSenderVariables(String senderId) {
		int rowsDeleted = getSenderVariableDao().deleteBySenderId(senderId);
		return rowsDeleted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateSenderReloadFlag();
	}
	
	@Autowired
	private SenderVariableDao senderVariableDao = null;
	private synchronized SenderVariableDao getSenderVariableDao() {
		return senderVariableDao;
	}
	
	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
	
}
