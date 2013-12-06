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
import org.springframework.stereotype.Component;

import com.es.core.util.BlobUtil;
import com.es.core.util.TimestampUtil;
import com.es.dao.abst.AbstractDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.data.constant.VariableType;
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
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(key);
		String sql = "update Sender_Data set " +
			"SystemKey=? " +
			" where SenderId= '" + Constants.DEFAULT_SENDER_ID + "'";
		int rowsUpdated = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpdated;
	}

	public synchronized int update(SenderDataVo senderVo) {
		senderVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		validateSenderVo(senderVo);
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(senderVo.getSenderId());
		keys.add(senderVo.getSenderName());
		keys.add(senderVo.getSenderType());
		keys.add(senderVo.getDomainName());
		keys.add(senderVo.getStatusId());
		keys.add(senderVo.getIrsTaxId());
		keys.add(senderVo.getWebSiteUrl());
		keys.add(senderVo.getSaveRawMsg());
		keys.add(senderVo.getContactName());
		keys.add(senderVo.getContactPhone());
		keys.add(senderVo.getContactEmail());
		keys.add(senderVo.getSecurityEmail());
		keys.add(senderVo.getCustcareEmail());
		keys.add(senderVo.getRmaDeptEmail());
		keys.add(senderVo.getSpamCntrlEmail());
		keys.add(senderVo.getChaRspHndlrEmail());
		keys.add(senderVo.getEmbedEmailId());
		keys.add(senderVo.getReturnPathLeft());
		keys.add(senderVo.getUseTestAddr());
		keys.add(senderVo.getTestFromAddr());
		keys.add(senderVo.getTestToAddr());
		keys.add(senderVo.getTestReplytoAddr());
		keys.add(senderVo.getIsVerpEnabled());
		keys.add(senderVo.getVerpSubDomain());
		keys.add(senderVo.getVerpInboxName());
		keys.add(senderVo.getVerpRemoveInbox());
		keys.add(senderVo.getSystemKey());
		keys.add(senderVo.getUpdtTime());
		keys.add(senderVo.getUpdtUserId());
		keys.add(senderVo.getRowId());
		
		String sql = "update Sender_Data set " +
			"SenderId=?," +
			"SenderName=?," +
			"SenderType=?," +
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
			"SystemKey=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where RowId=?";
		
		if (senderVo.getOrigUpdtTime() != null) {
			// optimistic locking
			sql += " and UpdtTime=?";
			keys.add(senderVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
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
		senderVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		validateSenderVo(senderVo);
		String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.getCurrentDb2Tms());
		Object[] parms = {
				senderVo.getSenderId(),
				senderVo.getSenderName(),
				senderVo.getSenderType(),
				senderVo.getDomainName(),
				senderVo.getStatusId(),
				senderVo.getIrsTaxId(),
				senderVo.getWebSiteUrl(),
				senderVo.getSaveRawMsg(),
				senderVo.getContactName(),
				senderVo.getContactPhone(),
				senderVo.getContactEmail(),
				senderVo.getSecurityEmail(),
				senderVo.getCustcareEmail(),
				senderVo.getRmaDeptEmail(),
				senderVo.getSpamCntrlEmail(),
				senderVo.getChaRspHndlrEmail(),
				senderVo.getEmbedEmailId(),
				senderVo.getReturnPathLeft(),
				senderVo.getUseTestAddr(),
				senderVo.getTestFromAddr(),
				senderVo.getTestToAddr(),
				senderVo.getTestReplytoAddr(),
				senderVo.getIsVerpEnabled(),
				senderVo.getVerpSubDomain(),
				senderVo.getVerpInboxName(),
				senderVo.getVerpRemoveInbox(),
				systemId,
				senderVo.getSystemKey(),
				senderVo.getUpdtTime(),
				senderVo.getUpdtUserId()
			};
		String sql = 
			"INSERT INTO Sender_Data " +
			"(SenderId, " +
			"SenderName," +
			"SenderType," +
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
			"SystemKey," +
			"UpdtTime," +
			"UpdtUserId) " +
			"VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
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
