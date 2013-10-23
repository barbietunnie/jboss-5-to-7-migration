package jpa.service.message;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.model.message.MessageIdDuplicate;
import jpa.model.message.MessageInbox;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.service.EmailAddressService;
import jpa.service.SenderDataService;
import jpa.service.SubscriberDataService;
import jpa.service.rule.RuleLogicService;
import jpa.util.JpaUtil;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageInboxService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageInboxService {
	static Logger logger = Logger.getLogger(MessageInboxService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	private EmailAddressService emailService;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private SubscriberDataService subscriberService;
	@Autowired
	private RuleLogicService logicService;
	@Autowired
	private MessageRenderedService renderedService;

	public MessageInbox getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageInbox record = (MessageInbox) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageInbox getByPrimaryKey(int rowId) throws NoResultException {
		return getByRowId(rowId);
	}

	public MessageInbox getAllDataByPrimaryKey(int rowId) throws NoResultException {
		MessageInbox mi = getByRowId(rowId);
		if (mi.getReferringMessageRowId()!=null) {
			try {
				mi.setReferringMessage(getByRowId(mi.getReferringMessageRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getLeadMessageRowId()!=null) {
			try {
				mi.setLeadMessage(getByRowId(mi.getLeadMessageRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getFromAddrRowId()!=null) {
			try {
				mi.setFromAddress(emailService.getByRowId(mi.getFromAddrRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getReplytoAddrRowId()!=null) {
			try {
				mi.setReplytoAddress(emailService.getByRowId(mi.getReplytoAddrRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getToAddrRowId()!=null) {
			try {
				mi.setToAddress(emailService.getByRowId(mi.getToAddrRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getSenderDataRowId()!=null) {
			try {
				mi.setSenderData(senderService.getByRowId(mi.getSenderDataRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getSubscriberDataRowId()!=null) {
			try {
				mi.setSubscriberData(subscriberService.getByRowId(mi.getSubscriberDataRowId()));
			}
			catch (NoResultException e) {}
		}
		try {
			mi.setRuleLogic(logicService.getByRowId(mi.getRuleLogicRowId()));
		}
		catch (NoResultException e) {}
		if (mi.getMessageRenderedRowId()!=null) {
			try {
				mi.setMessageRendered(renderedService.getByRowId(mi.getMessageRenderedRowId()));
			}
			catch (NoResultException e) {}
		}
		//
		return mi;
	}
	

	public MessageInbox getLastRecord() throws NoResultException {
		String sql = 
				"select t.* " +
				"from " +
					"Message_Inbox t " +
				" where t.Row_Id = (select max(t2.Row_Id) from Message_Inbox t2) ";
		try {
			Query query = em.createNativeQuery(sql, MessageInbox.MAPPING_MESSAGE_INBOX);
			MessageInbox record = (MessageInbox) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageInbox getPrevoiusRecord(MessageInbox inbox) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t where t.rowId<:rowId order by t.rowId desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", inbox.getRowId());
			MessageInbox record = (MessageInbox) query.setMaxResults(1).getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageInbox getNextRecord(MessageInbox inbox) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t where t.rowId>:rowId order by t.rowId asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", inbox.getRowId());
			MessageInbox record = (MessageInbox) query.setMaxResults(1).getSingleResult();
			return record;
		}
		finally {
		}
	}

	/*
	 * define methods primarily used by UI components. 
	 */

	public List<MessageInbox> getByLeadMsgId(int leadMsgId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t, MessageInbox t2 " +
				"where t2.rowId=t.leadMessageRowId and t2.rowId=:rowId order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", leadMsgId);
			@SuppressWarnings("unchecked")
			List<MessageInbox> list = query.getResultList();
			return getAllForList(list);
		}
		finally {
		}
	}

	private List<MessageInbox> getAllForList(List<MessageInbox> messages) {
		// XXX revisit
//		List<MessageInbox> msgs = new ArrayList<MessageInbox>();
//		for (MessageInbox message : messages) {
//			MessageInbox msg = getAllDataByPrimaryKey(message.getRowId());
//			msgs.add(msg);
//		}
//		return msgs;
		return messages;
	}

	public List<MessageInbox> getByReferringMsgId(int referredMsgId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t, MessageInbox t2 " +
				"where t2.rowId=t.referredMessageRowId and t2.rowId=:rowId order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", referredMsgId);
			@SuppressWarnings("unchecked")
			List<MessageInbox> list = query.getResultList();
			return getAllForList(list);
		}
		finally {
		}
	}

	public List<MessageInbox> getByFromAddress(String address) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t, EmailAddress ea " +
				"where ea.rowId=t.fromAddrRowId and ea.address=:address order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<MessageInbox> list = query.getResultList();
			return getAllForList(list);
		}
		finally {
		}
	}

	public List<MessageInbox> getByToAddress(String address) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageInbox t, EmailAddress ea " +
				"where ea.rowId=t.toAddrRowId and ea.address=:address order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<MessageInbox> list = query.getResultList();
			return getAllForList(list);
		}
		finally {
		}
	}

	public List<MessageInbox> getRecent(int days) {
		if (days < 0) days = 100; // default to last 100 days
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR,  - days);
		return getRecent(new java.sql.Date(cal.getTime().getTime()));
	}
	
	public List<MessageInbox> getRecent(java.sql.Date date) throws NoResultException {
		String sql = 
				"select t " +
				" from " +
					" MessageInbox t " +
				" where t.receivedTime>=:date " +
				" order by t.receivedTime desc " ;
		try {
			Query query = em.createQuery(sql);
			query.setParameter("date", date);
			@SuppressWarnings("unchecked")
			List<MessageInbox> list = query.setMaxResults(100).getResultList();
			return getAllForList(list);
		}
		finally {
		}
	}

	/**
	 * check if the message received is a duplicate.
	 * @param smtpMessageId to check.
	 * @return true if the smtpMessageId exists.
	 */
	public synchronized boolean isMessageIdDuplicate(String smtpMessageId) {
		if (Constants.DB_PRODNAME_PSQL.equalsIgnoreCase(JpaUtil.getDBProductName())) {
			return isMessageIdDuplicateV2(smtpMessageId);
		}
		else {
			return isMessageIdDuplicateV1(smtpMessageId);
		}
	}
	
	private boolean isMessageIdDuplicateV1(String smtpMessageId) {
		MessageIdDuplicate mdup = new MessageIdDuplicate();
		mdup.setMessageId(smtpMessageId);
		mdup.setAddTime(new java.sql.Timestamp(System.currentTimeMillis()));
		try {
			em.persist(mdup);
			em.flush();
			return false;
		}
		catch (EntityExistsException e) { // thrown from Hibernate
			return true;
		}
		catch (PersistenceException e) { // thrown from EclipseLink
			return true;
		}
		finally {
			em.detach(mdup);
		}
	}

	private boolean isMessageIdDuplicateV2(String smtpMessageId) {
		// PostgreSQL will abort the transaction when a duplicate key error is occurred.
		// Work around for PostgreSQL:
		// 1) query the record first
		// 2) insert a record if not found
		if (isMessageIdExist(smtpMessageId)) {
			return true;
		}
		return isMessageIdDuplicateV1(smtpMessageId);
	}

	public boolean isMessageIdExist(String smtpMessageId) {
		String sql = "select t from MessageIdDuplicate t where t.messageId=:messageId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("messageId", smtpMessageId);
			query.getSingleResult();
			return true;
		}
		catch (NoResultException e) {
			return false;
		}
		finally {
		}
	}

	public synchronized int purgeMessageIdDuplicate(int hours) {
		logger.info("purge records older than " + hours + " hours...");
		// prepare for delete of aged records
		String sql =
			"delete from  MessageIdDuplicate t "
			+ " where t.addTime < :addTime ";
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -hours);
		Timestamp go_back=new Timestamp(calendar.getTimeInMillis());

		try {
			if (Constants.DB_PRODNAME_MYSQL.equalsIgnoreCase(JpaUtil.getDBProductName())
					|| Constants.DB_PRODNAME_DERBY.equalsIgnoreCase(JpaUtil.getDBProductName())) {
				em.clear(); // to work around a weird problem (MySQL and Derby) where
					// the query.executeUpdate() triggers an INSERT from em cache.
			}
			Query query = em.createQuery(sql);
			query.setParameter("addTime", go_back);
			int rows = query.executeUpdate();
			logger.info("number of records purged: "+rows);
			return rows;
		}
		finally {
		}
	}

	public void delete(MessageInbox msgInbox) {
		if (msgInbox == null) return;
		try {
			em.remove(msgInbox);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageInbox t " +
				" where t.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageInbox msgInbox) {
		try {
			if (em.contains(msgInbox)) {
				em.persist(msgInbox);
			}
			else {
				em.merge(msgInbox);
			}
		}
		finally {
			em.flush();
			if (Constants.DB_PRODNAME_DERBY.equalsIgnoreCase(JpaUtil.getDBProductName())) {
				em.clear();
			}
		}
	}

	public int updateCounts(MessageInbox msgInbox) {
		String sql = "update Message_Inbox set ForwardCount=?, ReadCount=?, ReplyCount=? " +
				"where Row_Id = ? ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, msgInbox.getForwardCount());
			query.setParameter(2, msgInbox.getReadCount());
			query.setParameter(3, msgInbox.getReplyCount());
			query.setParameter(4, msgInbox.getRowId());
			int rowsUpdated = query.executeUpdate();
			return rowsUpdated;
		}
		finally {
		}
	}

	public void insert(MessageInbox msgInbox) {
		try {
			em.persist(msgInbox);
			em.flush(); // to populate the @Id field
			if (msgInbox.getLeadMessageRowId()==null) {
				msgInbox.setLeadMessageRowId(msgInbox.getRowId());
			}
			em.flush();
		}
		finally {
		}
	}

	public int getReceivedUnreadCount() {
		return getUnreadCount(MsgDirectionCode.RECEIVED);
	}

	public int getSentUnreadCount() {
		return getUnreadCount(MsgDirectionCode.SENT);
	}

	public int getAllUnreadCount() {
		return getUnreadCount(null);
	}

	private int getUnreadCount(MsgDirectionCode msgDirection) {
		String sql = 
			"select count(*) from Message_Inbox where ReadCount=?1 ";
		if (msgDirection!=null) {
			sql += " and MsgDirection=?2 "; 
		}
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, 0);
			if (msgDirection!=null) {
				query.setParameter(2, msgDirection.getValue());
			}
			Number count = (Number) query.getSingleResult();
			return count.intValue();
		}
		finally {
		}
	}

	public int updateStatusIdByLeadMsgId(MessageInbox msgInbox) {
		msgInbox.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		String sql =
			"update Message_Inbox a set " +
				"a.updtTime=?1, " +
				"a.updtUserId=?2, " +
				"a.statusId=?3 " +
			" where " +
				" a.LeadMsgRowId=?4 ";

		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, msgInbox.getUpdtTime());
			query.setParameter(2, msgInbox.getUpdtUserId());
			query.setParameter(3, msgInbox.getStatusId());
			query.setParameter(4, msgInbox.getLeadMessageRowId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };

	public int getRowCountForWeb(SearchFieldsVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		String sql = 
			"SELECT count(*) " +
			" FROM Message_Inbox a " + 
			" JOIN Email_Address b ON a.FromAddressRowId=b.Row_Id " +
			" LEFT JOIN Rule_Logic c ON a.RuleLogicRowId=c.Row_Id " +
			whereSql;
		Query query = em.createNativeQuery(sql);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		Number count = (Number) query.getSingleResult();
		return count.intValue();
	}

	public List<MessageInbox> getListForWeb(SearchFieldsVo vo) {
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
				whereSql += CRIT[parms.size()] + " a.Row_Id < ? ";
				parms.add(vo.getMsgIdLast());
			}
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.PREVIOUS)) {
			if (vo.getMsgIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id > ? ";
				parms.add(vo.getMsgIdFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.LAST)) {
			List<MessageInbox> lastList = new ArrayList<MessageInbox>();
			vo.setPageAction(SearchFieldsVo.PageAction.NEXT);
			while (true) {
				List<MessageInbox> nextList = getListForWeb(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setMsgIdLast(nextList.get(nextList.size() - 1).getRowId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.CURRENT)) {
			if (vo.getMsgIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id <= ? ";
				parms.add(vo.getMsgIdFirst());
			}
		}
		// build SQL
		String sql = 
			"SELECT a.* " +
			" FROM " +
				"Message_Inbox a " +
				" JOIN Email_Address b ON a.FromAddressRowId=b.Row_Id " +
				" LEFT JOIN Rule_Logic c ON a.RuleLogicRowId=c.Row_Id " +
				whereSql +
			" order by a.Row_Id " + fetchOrder;
		//if (Constants.DB_PRODNAME_MYSQL.equals(JpaUtil.getDBProductName())) {
		//	sql += " limit " + vo.getPageSize();
		//}
		// set result set size
		Query query = em.createNativeQuery(sql, MessageInbox.MAPPING_MESSAGE_INBOX);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		@SuppressWarnings("unchecked")
		List<MessageInbox> list = query.setMaxResults(vo.getPageSize()).getResultList();
		if (vo.getPageAction().equals(SearchFieldsVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}
	
	private String getWhereSqlForWeb(SearchFieldsVo vo, List<Object> parms) {
		String whereSql = "";
		// Closed?
		String closed = null;
		if (vo.getMsgType() != null) {
			if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Closed)) {
				closed = MsgStatusCode.CLOSED.getValue();
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
			if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Received)) {
				direction = MsgDirectionCode.RECEIVED.getValue();
			}
			else if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Sent)) {
				direction = MsgDirectionCode.SENT.getValue();
			}
		}
		if (direction != null && closed == null) { // and not closed
			whereSql += CRIT[parms.size()] + " a.MsgDirection = ? ";
			parms.add(direction);
		}
		// ruleName
		if (StringUtils.isNotBlank(vo.getRuleName())) {
			if (!SearchFieldsVo.RuleName.All.name().equals(vo.getRuleName())) {
				whereSql += CRIT[parms.size()] + " c.RuleName = ? ";
				parms.add(vo.getRuleName());
			}
		}
		// toAddress RowId
		if (vo.getToAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.ToAddressRowId = ? ";
			parms.add(vo.getToAddrId());
		}
		// fromAddress RowId
		if (vo.getFromAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.FromAddressRowId = ? ";
			parms.add(vo.getFromAddrId());
		}
		// readCount
		if (vo.getIsRead() != null) {
			if (vo.getIsRead()==true)
				whereSql += CRIT[parms.size()] + " a.ReadCount > ? ";
			else
				whereSql += CRIT[parms.size()] + " a.ReadCount <= ? ";
			parms.add(0);
		}
		// msgFlag
		if (vo.getIsFlagged() != null) {
			whereSql += CRIT[parms.size()] + " a.IsFlagged = ? ";
			parms.add(vo.getIsFlagged());
		}
		// subject
		if (StringUtils.isNotBlank(vo.getSubject())) {
			String subj = vo.getSubject().trim();
			if (subj.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.MsgSubject LIKE ? ";
				parms.add("%" + subj + "%");
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
				whereSql += CRIT[parms.size()] + " a.MsgBody LIKE ? ";
				parms.add("%" + vo.getBody().trim() + "%");
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
					whereSql += CRIT[parms.size()] + " b.OrigAddress LIKE ? ";
					parms.add("%" + from + "%");
				}
				else {
					String regex = StringUtil.replaceAll(from, " ", ".+");
					whereSql += CRIT[parms.size()] + " b.OrigAddress REGEXP '" + regex + "' ";
				}
			}
		}
		return whereSql;
	}
}
