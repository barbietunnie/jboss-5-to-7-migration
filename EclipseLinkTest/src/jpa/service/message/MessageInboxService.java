package jpa.service.message;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageInbox;
import jpa.service.ClientDataService;
import jpa.service.CustomerDataService;
import jpa.service.EmailAddressService;
import jpa.service.rule.RuleLogicService;

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
	private ClientDataService clientService;
	@Autowired
	private CustomerDataService customerService;
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
		if (mi.getClientDataRowId()!=null) {
			try {
				mi.setClientData(clientService.getByRowId(mi.getClientDataRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mi.getCustomerDataRowId()!=null) {
			try {
				mi.setCustomerData(customerService.getByRowId(mi.getCustomerDataRowId()));
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
			if (msgInbox.getLeadMessageRowId()==null) {
				msgInbox.setLeadMessageRowId(msgInbox.getRowId());
			}
			em.flush();
		}
		finally {
		}
	}

}
