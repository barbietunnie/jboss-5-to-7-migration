package com.es.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.core.util.StringUtil;
import com.es.data.constant.CodeType;
import com.es.data.constant.MsgDirectionCode;
import com.es.data.constant.MsgStatusCode;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.inbox.MsgInboxWebVo;
import com.es.vo.inbox.SearchFieldsVo;

@Component("msgInboxDao")
public class MsgInboxDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

//	private static class MsgInboxMapper implements RowMapper<MsgInboxVo> {
//		
//		public MsgInboxVo mapRow(ResultSet rs, int rowNum) throws SQLException {
//			MsgInboxVo msgInboxVo = setRowValues(rs);
//			
//			return msgInboxVo;
//		}
//		
//		protected static MsgInboxVo setRowValues(ResultSet rs) throws SQLException {
//			MsgInboxVo msgInboxVo = populateVo(rs);
//			
//			return msgInboxVo;
//		}
//		
//		protected static final MsgInboxVo populateVo(ResultSet rs) throws SQLException {
//			MsgInboxVo msgInboxVo = new MsgInboxVo();
//			
//			msgInboxVo.setMsgId(rs.getLong("MsgId"));
//			msgInboxVo.setMsgRefId((Long)rs.getObject("MsgRefId"));
//			msgInboxVo.setLeadMsgId(rs.getLong("LeadMsgId"));
//			msgInboxVo.setCarrierCode(rs.getString("CarrierCode"));
//			msgInboxVo.setMsgSubject(rs.getString("MsgSubject"));
//			msgInboxVo.setMsgPriority(rs.getString("MsgPriority"));
//			msgInboxVo.setReceivedTime(rs.getTimestamp("ReceivedTime"));
//			msgInboxVo.setFromAddrId((Long)rs.getObject("FromAddrId"));
//			msgInboxVo.setReplyToAddrId((Long)rs.getObject("ReplyToAddrId"));
//			msgInboxVo.setToAddrId((Long)rs.getObject("ToAddrId"));
//			msgInboxVo.setClientId(rs.getString("ClientId"));
//			msgInboxVo.setCustId(rs.getString("CustId"));
//			msgInboxVo.setPurgeDate((java.sql.Date)rs.getObject("PurgeDate"));
//			msgInboxVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
//			msgInboxVo.setUpdtUserId(rs.getString("UpdtUserId"));
//			msgInboxVo.setLockTime(rs.getTimestamp("LockTime"));
//			msgInboxVo.setLockId(rs.getString("LockId"));
//			msgInboxVo.setRuleName(rs.getString("RuleName"));
//			msgInboxVo.setReadCount(rs.getInt("ReadCount"));
//			msgInboxVo.setReplyCount(rs.getInt("ReplyCount"));
//			msgInboxVo.setForwardCount(rs.getInt("ForwardCount"));
//			msgInboxVo.setFlagged(rs.getString("Flagged"));
//			
//			msgInboxVo.setMsgDirection(rs.getString("MsgDirection"));
//			msgInboxVo.setDeliveryTime(rs.getTimestamp("DeliveryTime"));
//			msgInboxVo.setStatusId(rs.getString("StatusId"));
//			msgInboxVo.setSmtpMessageId(rs.getString("SmtpMessageId"));
//			msgInboxVo.setRenderId((Long)rs.getObject("RenderId"));
//			msgInboxVo.setOverrideTestAddr(rs.getString("OverrideTestAddr"));
//			
//			msgInboxVo.setAttachmentCount(rs.getInt("AttachmentCount"));
//			msgInboxVo.setAttachmentSize(rs.getInt("AttachmentSize"));
//			msgInboxVo.setMsgBodySize(rs.getInt("MsgBodySize"));
//			
//			msgInboxVo.setMsgContentType(rs.getString("MsgContentType"));
//			msgInboxVo.setBodyContentType(rs.getString("BodyContentType"));
//			msgInboxVo.setMsgBody(rs.getString("MsgBody"));
//			
//			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
//			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
//			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
//			return msgInboxVo;
//		}
//	}

//	private static class MsgInboxMapperWeb implements RowMapper<MsgInboxWebVo> {
//		
//		public MsgInboxWebVo mapRow(ResultSet rs, int rowNum) throws SQLException {
//			MsgInboxWebVo msgInboxVo = new MsgInboxWebVo();
//			
//			msgInboxVo.setMsgId(rs.getLong("MsgId"));
//			msgInboxVo.setMsgRefId((Long)rs.getObject("MsgRefId"));
//			msgInboxVo.setLeadMsgId(rs.getLong("LeadMsgId"));
//			msgInboxVo.setMsgSubject(rs.getString("MsgSubject"));
//			msgInboxVo.setReceivedTime(rs.getTimestamp("ReceivedTime"));
//			msgInboxVo.setFromAddrId((Long)rs.getObject("FromAddrId"));
//			msgInboxVo.setToAddrId((Long)rs.getObject("ToAddrId"));
//			msgInboxVo.setRuleName(rs.getString("RuleName"));
//			msgInboxVo.setReadCount(rs.getInt("ReadCount"));
//			msgInboxVo.setReplyCount(rs.getInt("ReplyCount"));
//			msgInboxVo.setForwardCount(rs.getInt("ForwardCount"));
//			msgInboxVo.setFlagged(rs.getString("Flagged"));
//			msgInboxVo.setMsgDirection(rs.getString("MsgDirection"));
//			msgInboxVo.setStatusId(rs.getString("StatusId"));
//			
//			msgInboxVo.setAttachmentCount(rs.getInt("AttachmentCount"));
//			msgInboxVo.setAttachmentSize(rs.getInt("AttachmentSize"));
//			msgInboxVo.setMsgBodySize(rs.getInt("MsgBodySize"));
//			
//			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
//			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
//			
//			return msgInboxVo;
//		}
//	}
	
	public MsgInboxVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"Msg_Inbox " +
			" where msgId=? ";
		Object[] parms = new Object[] {msgId};
		try {
			MsgInboxVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MsgInboxVo getFirstRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Inbox " +
			" where msgId = (select min(MsgId) from Msg_Inbox) ";
		MsgInboxVo vo = getJdbcTemplate().queryForObject(sql,
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return vo;
	}
	
	public MsgInboxVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"Msg_Inbox " +
			" where msgId = (select max(MsgId) from Msg_Inbox) ";

		MsgInboxVo vo = getJdbcTemplate().queryForObject(sql, 
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return vo;
	}
	
	public List<MsgInboxWebVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Inbox " +
			" where leadMsgId=? " +
			" order by msgId";
		Object[] parms = new Object[] {leadMsgId};
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		return list;
	}
	
	public List<MsgInboxWebVo> getByMsgRefId(long msgRefId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Inbox " +
			" where MsgRefId=? " +
			" order by msgId";
		Object[] parms = new Object[] {msgRefId};
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		return list;
	}
	
	public List<MsgInboxVo> getByFromAddrId(long addrId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Inbox " +
			" where fromAddrId=? " +
			" order by msgId";
		Object[] parms = new Object[] {addrId};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	public List<MsgInboxVo> getByToAddrId(long addrId) {
		String sql = 
			"select * " +
			" from " +
				" Msg_Inbox " +
			" where toAddrId=? " +
			" order by msgId";
		Object[] parms = new Object[] {addrId};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	public List<MsgInboxVo> getRecent(int days) {
		if (days < 0) days = 365 * 20; // retrieve all mails
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR,  - days);
		return getRecent(cal.getTime());
	}
	
	/**
	 * retrieve up to 100 rows
	 */
	public List<MsgInboxVo> getRecent(Date date) {
		if (date == null) {
			date = new java.util.Date();
		}
		String sql = 
			"select * " +
			" from " +
				" Msg_Inbox " +
			" where receivedTime>=? " +
			" order by receivedTime desc limit 100";
		Object[] parms = new Object[] {new Timestamp(date.getTime())};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	public int getInboxUnreadCount() {
		return getMsgUnreadCountDao().selectInboxUnreadCount();
	}
	
	public int getSentUnreadCount() {
		return getMsgUnreadCountDao().selectSentUnreadCount();
	}
	
	public int getAllUnreadCount() {
		return getInboxUnreadCount() + getSentUnreadCount();
	}
	
	public boolean isMessageIdExist(String smtpMessageId) {
		String sql = "select count(*) from MSGID_DUPCHK where message_id=? ";
		int rows = getJdbcTemplate().queryForObject(sql, new Object[] {smtpMessageId}, Integer.class);
		return (rows>0);
	}

	public int resetInboxUnreadCount() {
		String sql = 
			"select count(*) " +
			" from " +
				" Msg_Inbox " +
			" where ReadCount=0 " +
				" and MsgDirection=? " +
				" and (StatusId is null OR StatusId!=?) ";
		List<Object> parms = new ArrayList<Object>();
		parms.add(MsgDirectionCode.RECEIVED.getValue());
		parms.add(MsgStatusCode.CLOSED.getValue());
		int inboxUnreadCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		getMsgUnreadCountDao().resetInboxUnreadCount(inboxUnreadCount);
		return inboxUnreadCount;
	}
	
	public int resetSentUnreadCount() {
		String sql = 
			"select count(*) " +
			" from " +
				" Msg_Inbox " +
			" where ReadCount=0 " +
				" and MsgDirection=? " +
				" and (StatusId is null OR StatusId!=?) ";
		List<Object> parms = new ArrayList<Object>();
		parms.add(MsgDirectionCode.SENT.getValue());
		parms.add(MsgStatusCode.CLOSED.getValue());
		int sentUnreadCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		getMsgUnreadCountDao().resetSentUnreadCount(sentUnreadCount);
		return sentUnreadCount;
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };

	public int getRowCountForWeb(SearchFieldsVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		String sql = 
			"SELECT count(*) " +
			" FROM Msg_Inbox a " + 
			" JOIN Email_Address b ON a.FromAddrId=b.EmailAddrId " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	public List<MsgInboxWebVo> getListForWeb(SearchFieldsVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "desc";
		if (vo.getPageAction().equals(SearchFieldsVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.NEXT)) {
			if (vo.getMsgIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId < ? ";
				parms.add(vo.getMsgIdLast());
			}
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.PREVIOUS)) {
			if (vo.getMsgIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId > ? ";
				parms.add(vo.getMsgIdFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.LAST)) {
			List<MsgInboxWebVo> lastList = new ArrayList<MsgInboxWebVo>();
			vo.setPageAction(SearchFieldsVo.PageAction.NEXT);
			while (true) {
				List<MsgInboxWebVo> nextList = getListForWeb(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setMsgIdLast(nextList.get(nextList.size() - 1).getMsgId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.CURRENT)) {
			if (vo.getMsgIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId <= ? ";
				parms.add(vo.getMsgIdFirst());
			}
		}
		// build SQL
		String sql = 
			"SELECT " +
				"MsgId, " +
				"MsgRefId, " +
				"LeadMsgId, " +
				"MsgSubject, " +
				"ReceivedTime, " +
				"FromAddrId, " +
				"ToAddrId, " +
				"RuleName, " +
				"ReadCount, " +
				"ReplyCount, " +
				"ForwardCount, " +
				"Flagged, " +
				"MsgDirection, " +
				"a.StatusId, " +
				"AttachmentCount, " +
				"AttachmentSize, " +
				"MsgBodySize " +
			" FROM " +
				"Msg_Inbox a " +
				" JOIN Email_Address b ON a.FromAddrId=b.EmailAddrId " +
				whereSql +
			" order by MsgId " + fetchOrder +
			" limit " + vo.getPageSize();
		// set result set size
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms.toArray(), 
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(SearchFieldsVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}
	
	private String getWhereSqlForWeb(SearchFieldsVo vo, List<Object> parms) {
		String whereSql = "";
		// Closed?
		MsgStatusCode closed = null;
		if (vo.getMsgType() != null) {
			if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Closed)) {
				closed = MsgStatusCode.CLOSED;
			}
		}
		if (closed != null) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(MsgStatusCode.CLOSED.getValue());
		}
		else {
			whereSql += CRIT[parms.size()] + " a.StatusId != ? ";
			parms.add(MsgStatusCode.CLOSED.getValue());
		}
		// msgDirection
		String direction = null;
		if (vo.getMsgType() != null) {
			if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Received))
				direction = MsgDirectionCode.RECEIVED.getValue();
			else if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Sent))
				direction = MsgDirectionCode.SENT.getValue();
		}
		if (direction != null && closed == null) { // and not closed
			whereSql += CRIT[parms.size()] + " a.MsgDirection = ? ";
			parms.add(direction);
		}
		// ruleName
		if (StringUtils.isNotBlank(vo.getRuleName())) {
			if (!SearchFieldsVo.RuleName.All.toString().equals(vo.getRuleName())) {
				whereSql += CRIT[parms.size()] + " a.RuleName = ? ";
				parms.add(vo.getRuleName());
			}
		}
		// toAddress
		if (vo.getToAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.ToAddrId = ? ";
			parms.add(vo.getToAddrId());
		}
		// fromAddress
		if (vo.getFromAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.FromAddrId = ? ";
			parms.add(vo.getFromAddrId());
		}
		// readCount
		if (vo.getRead() != null) {
			if (vo.getRead().booleanValue())
				whereSql += CRIT[parms.size()] + " a.ReadCount > ? ";
			else
				whereSql += CRIT[parms.size()] + " a.ReadCount <= ? ";
			parms.add(0);
		}
		// msgFlag
		if (vo.getFlagged() != null) {
			whereSql += CRIT[parms.size()] + " a.Flagged = ? ";
			parms.add(CodeType.YES_CODE.getValue());
		}
		// subject
		if (StringUtils.isNotBlank(vo.getSubject())) {
			String subj = vo.getSubject().trim();
			if (subj.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.MsgSubject LIKE '%" + subj + "%' ";
			}
			else {
				String regex = StringUtil.replaceAll(subj, " ", ".+");
				whereSql += CRIT[parms.size()] + " a.MsgSubject REGEXP '" + regex + "' ";
			}
		}
		// body
		if (StringUtils.isNotBlank(vo.getBody())) {
			String body = vo.getBody().trim();
			if (body.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.MsgBody LIKE '%" + vo.getBody().trim() + "%' ";
			}
			else {
				// ".+" or "[[:space:]].*" or "([[:space:]]+|[[:space:]].+[[:space:]])"
				String regex = StringUtil.replaceAll(body, " ", "[[:space:]].*");
				whereSql += CRIT[parms.size()] + " a.MsgBody REGEXP '" + regex + "' ";
			}
		}
		// from address
		if (StringUtils.isNotBlank(vo.getFromAddr())) {
			if (vo.getFromAddrId() == null) {
				String from = vo.getFromAddr().trim();
				if (from.indexOf(" ") < 0) {
					whereSql += CRIT[parms.size()] + " b.OrigEmailAddr LIKE '%" + from + "%' ";
				}
				else {
					String regex = StringUtil.replaceAll(from, " ", ".+");
					whereSql += CRIT[parms.size()] + " b.OrigEmailAddr REGEXP '" + regex + "' ";
				}
			}
		}
		
		return whereSql;
	}

	public int update(MsgInboxWebVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getMsgRefId());
		fields.add(msgInboxVo.getLeadMsgId());
		fields.add(msgInboxVo.getMsgSubject());
		fields.add(msgInboxVo.getReceivedTime());
		fields.add(msgInboxVo.getFromAddrId());
		fields.add(msgInboxVo.getToAddrId());
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getRuleName());
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		fields.add(msgInboxVo.getMsgDirection());
		fields.add(msgInboxVo.getStatusId());
		fields.add(msgInboxVo.getAttachmentCount());
		fields.add(msgInboxVo.getAttachmentSize());
		fields.add(msgInboxVo.getMsgBodySize());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update Msg_Inbox set " +
				"MsgRefId=?, " +
				"LeadMsgId=?, " +
				"MsgSubject=?, " +
				"ReceivedTime=?, " +
				"FromAddrId=?, " +
				"ToAddrId=?, " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"RuleName=?, " +
				"ReadCount=?, " +
				"ReplyCount=?, " +
				"ForwardCount=?, " +
				"Flagged=?, " +
				"MsgDirection=?, " +
				"StatusId=?, " + 
				"AttachmentCount=?, " +
				"AttachmentSize=?, " +
				"MsgBodySize=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}

	public int updateCounts(MsgInboxWebVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update Msg_Inbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"ReadCount=?, " +
				"ReplyCount=?, " +
				"ForwardCount=?, " +
				"Flagged=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}

	private void adjustUnreadCounts(MsgInboxWebVo msgInboxVo) {
		if (!MsgStatusCode.CLOSED.equals(msgInboxVo.getOrigStatusId())) { // Was Open
			if (msgInboxVo.getOrigReadCount() == 0 && msgInboxVo.getReadCount() > 0)
				updateCounts(msgInboxVo, -1);
			else if (msgInboxVo.getOrigReadCount() > 0 && msgInboxVo.getReadCount() == 0)
				updateCounts(msgInboxVo, 1);
			else if (MsgStatusCode.CLOSED.equals(msgInboxVo.getStatusId())
					&& msgInboxVo.getReadCount() == 0)
				updateCounts(msgInboxVo, -1);
		}
		else { // Was Closed
			if (!MsgStatusCode.CLOSED.equals(msgInboxVo.getStatusId())
					&& msgInboxVo.getReadCount() == 0)
				updateCounts(msgInboxVo, 1);
		}
	}
	
	private void adjustUnreadCounts(MsgInboxVo msgInboxVo) {
		if (!MsgStatusCode.CLOSED.getValue().equals(msgInboxVo.getOrigStatusId())) { // Was Open
			if (msgInboxVo.getOrigReadCount() == 0 && msgInboxVo.getReadCount() > 0) {
				updateCounts(msgInboxVo, -1);
			}
			else if (msgInboxVo.getOrigReadCount() > 0 && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, 1);
			}
			else if (MsgStatusCode.CLOSED.getValue().equals(msgInboxVo.getStatusId())
					&& msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, -1);
			}
		}
		else { // Was Closed
			if (!MsgStatusCode.CLOSED.getValue().equals(msgInboxVo.getStatusId())
					&& msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, 1);
			}
		}
	}
	
	private void updateCounts(MsgInboxVo msgInboxVo, int count) {
		if (MsgDirectionCode.RECEIVED.getValue().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateInboxUnreadCount(count);
		}
		else if (MsgDirectionCode.SENT.getValue().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateSentUnreadCount(count);
		}
	}
	
	private void updateCounts(MsgInboxWebVo msgInboxVo, int count) {
		if (MsgDirectionCode.RECEIVED.getValue().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateInboxUnreadCount(count);
		}
		else if (MsgDirectionCode.SENT.getValue().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateSentUnreadCount(count);
		}
	}
	
	public int updateCounts(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update Msg_Inbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"ReadCount=?, " +
				"ReplyCount=?, " +
				"ForwardCount=?, " +
				"Flagged=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}

	public int updateStatusId(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getStatusId());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update Msg_Inbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"StatusId=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
		}
		return rowsUpadted;
	}
	
	public int updateStatusIdByLeadMsgId(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getStatusId());
		
		fields.add(msgInboxVo.getLeadMsgId());
		
		String sql =
			"update Msg_Inbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"StatusId=? " +
			" where " +
				" LeadMsgId=? ";

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}
	
	public int update(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getMsgRefId());
		fields.add(msgInboxVo.getLeadMsgId());
		fields.add(msgInboxVo.getCarrierCode());
		fields.add(msgInboxVo.getMsgSubject());
		fields.add(msgInboxVo.getMsgPriority());
		fields.add(msgInboxVo.getReceivedTime());
		fields.add(msgInboxVo.getFromAddrId());
		fields.add(msgInboxVo.getReplyToAddrId());
		fields.add(msgInboxVo.getToAddrId());
		fields.add(msgInboxVo.getSenderId());
		fields.add(msgInboxVo.getSubrId());
		fields.add(msgInboxVo.getPurgeDate());
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getLockTime());
		fields.add(msgInboxVo.getLockId());
		fields.add(msgInboxVo.getRuleName());
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		
		fields.add(msgInboxVo.getMsgDirection());
		fields.add(msgInboxVo.getDeliveryTime());
		fields.add(msgInboxVo.getStatusId());
		fields.add(msgInboxVo.getSmtpMessageId());
		fields.add(msgInboxVo.getRenderId());
		fields.add(msgInboxVo.getOverrideTestAddr());
		
		fields.add(msgInboxVo.getAttachmentCount());
		fields.add(msgInboxVo.getAttachmentSize());
		fields.add(msgInboxVo.getMsgBodySize());
		
		fields.add(msgInboxVo.getMsgContentType());
		fields.add(msgInboxVo.getBodyContentType());
		fields.add(msgInboxVo.getMsgBody());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update Msg_Inbox set " +
				"MsgRefId=?, " +
				"LeadMsgId=?, " +
				"CarrierCode=?, " +
				"MsgSubject=?, " +
				"MsgPriority=?, " +
				"ReceivedTime=?, " +
				"FromAddrId=?, " +
				"ReplyToAddrId=?, " +
				"ToAddrId=?, " +
				"SenderId=?, " +
				"SubrId=?, " +
				"PurgeDate=?, " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"LockTime=?, " +
				"LockId=?, " +
				"RuleName=?, " +
				"ReadCount=?, " +
				"ReplyCount=?, " +
				"ForwardCount=?, " +
				"Flagged=?, " +
				"MsgDirection=?, " +
				"DeliveryTime=?, " +
				"StatusId=?, " +
				"SmtpMessageId=?, " +
				"RenderId=?, " +
				"OverrideTestAddr=?, " +
				"AttachmentCount=?, " +
				"AttachmentSize=?, " +
				"MsgBodySize=?, " +
				"MsgContentType=?, " +
				"BodyContentType=?, " +
				"MsgBody=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId) {
		MsgInboxVo msgInboxVo = getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			return 0;
		}
		
		String sql = 
			"delete from Msg_Inbox where msgId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0 && msgInboxVo.getOrigReadCount() <= 0 // origReadCount was -1
				&& !MsgStatusCode.CLOSED.getValue().equals(msgInboxVo.getStatusId())) {
			if (MsgDirectionCode.RECEIVED.getValue().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateInboxUnreadCount(-1);
			}
			else if (MsgDirectionCode.SENT.getValue().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateSentUnreadCount(-1);
			}
		}
		return rowsDeleted;
	}
	
	public int insert(MsgInboxVo msgInboxVo) {
		String sql = 
			"INSERT INTO Msg_Inbox (" +
			"MsgId, " +
			"MsgRefId, " +
			"LeadMsgId, " +
			"CarrierCode, " +
			"MsgSubject, " +
			"MsgPriority, " +
			"ReceivedTime, " +
			"FromAddrId, " +
			"ReplyToAddrId, " +
			"ToAddrId, " +
			"SenderId, " +
			"SubrId, " +
			"PurgeDate, " +
			"UpdtTime, " +
			"UpdtUserId, " +
			"LockTime, " +
			"LockId, " +
			"RuleName, " +
			"ReadCount, " +
			"ReplyCount, " +
			"ForwardCount, " +
			"Flagged, " +
			"MsgDirection, " +
			"DeliveryTime, " +
			"StatusId, " +
			"SmtpMessageId, " +
			"RenderId, " +
			"OverrideTestAddr, " +
			"AttachmentCount, " +
			"AttachmentSize, " +
			"MsgBodySize, " +
			"MsgContentType, " +
			"BodyContentType, " +
			"MsgBody " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				" ?, ?, ?, ?)";
		
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getMsgId());
		fields.add(msgInboxVo.getMsgRefId());
		fields.add(msgInboxVo.getLeadMsgId());
		fields.add(msgInboxVo.getCarrierCode());
		fields.add(msgInboxVo.getMsgSubject());
		fields.add(msgInboxVo.getMsgPriority());
		fields.add(msgInboxVo.getReceivedTime());
		fields.add(msgInboxVo.getFromAddrId());
		fields.add(msgInboxVo.getReplyToAddrId());
		fields.add(msgInboxVo.getToAddrId());
		fields.add(msgInboxVo.getSenderId());
		fields.add(msgInboxVo.getSubrId());
		fields.add(msgInboxVo.getPurgeDate());
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getLockTime());
		fields.add(msgInboxVo.getLockId());
		fields.add(msgInboxVo.getRuleName());
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		fields.add(msgInboxVo.getMsgDirection());
		fields.add(msgInboxVo.getDeliveryTime());
		fields.add(msgInboxVo.getStatusId());
		fields.add(msgInboxVo.getSmtpMessageId());
		fields.add(msgInboxVo.getRenderId());
		fields.add(msgInboxVo.getOverrideTestAddr());
		fields.add(msgInboxVo.getAttachmentCount());
		fields.add(msgInboxVo.getAttachmentSize());
		fields.add(msgInboxVo.getMsgBodySize());
		fields.add(msgInboxVo.getMsgContentType());
		fields.add(msgInboxVo.getBodyContentType());
		fields.add(msgInboxVo.getMsgBody());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		//msgInboxVo.setMsgId(getJdbcTemplate().queryForInt(getRowIdSql()));
		if (rowsInserted > 0 && msgInboxVo.getReadCount() == 0
				&& !MsgStatusCode.CLOSED.getValue().equals(msgInboxVo.getStatusId())) {
			if (MsgDirectionCode.RECEIVED.getValue().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateInboxUnreadCount(1);
			}
			else if (MsgDirectionCode.SENT.getValue().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateSentUnreadCount(1);
			}
		}
		return rowsInserted;
	}

	@Autowired
	private MsgUnreadCountDao msgUnreadCountDao = null;
	MsgUnreadCountDao getMsgUnreadCountDao() {
		return msgUnreadCountDao;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
