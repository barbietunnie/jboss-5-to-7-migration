package jpa.service.message;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.model.message.MessageIdDuplicate;
import jpa.model.message.MessageInbox;
import jpa.service.EmailAddressService;
import jpa.service.SenderDataService;
import jpa.service.SubscriberDataService;
import jpa.service.rule.RuleLogicService;
import jpa.util.JpaUtil;

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
			return list;
		}
		finally {
		}
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
			return list;
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
			return list;
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
			return list;
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
				" order by t.receivedTime desc " ; //limit 100";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("date", date);
			@SuppressWarnings("unchecked")
			List<MessageInbox> list = query.setMaxResults(100).getResultList();
			return list;
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
			if ("Apache Derby".equalsIgnoreCase(JpaUtil.getDBProductName())) {
				em.clear();
			}
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

}
