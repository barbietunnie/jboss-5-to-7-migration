package jpa.service;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.MessageInbox;

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
				"where t2=t.leadMessage and t2.rowId=:rowId order by t.rowId ";
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
				"where t2=t.referredMessage and t2.rowId=:rowId order by t.rowId ";
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
				"MessageInbox t, EmailAddr ea " +
				"where ea=t.fromAddress and ea.address=:address order by t.rowId ";
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
				"MessageInbox t, EmailAddr ea " +
				"where ea=t.toAddress and ea.address=:address order by t.rowId ";
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
			Query query = em.createNativeQuery(sql);
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
		}
	}

	public void insert(MessageInbox msgInbox) {
		try {
			em.persist(msgInbox);
			em.flush(); // to populate the @Id field
			if (msgInbox.getLeadMessage()==null) {
				msgInbox.setLeadMessage(msgInbox);
			}
			em.flush();
		}
		finally {
		}
	}

}
