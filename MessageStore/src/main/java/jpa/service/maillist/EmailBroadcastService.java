package jpa.service.maillist;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.model.EmailBroadcast;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailBroadcastService")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailBroadcastService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = Logger.getLogger(EmailBroadcastService.class);
	
	@Autowired
	EntityManager em;
	
	public EmailBroadcast getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailBroadcast t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			EmailBroadcast broadcast = (EmailBroadcast) query.getSingleResult();
			//em.lock(broadcast, LockModeType.OPTIMISTIC);
			return broadcast;
		}
		finally {
		}
	}
	
	public List<EmailBroadcast> getByEmailAddress(String address) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailBroadcast t, EmailAddress ea " +
					" where ea=t.emailAddress and ea.address = :address");
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<EmailBroadcast> list = (List<EmailBroadcast>) query.getResultList();
			//em.lock(EmailBroadcast, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<EmailBroadcast> getByEmailAddrRowId(int emailAddrRowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailBroadcast t, EmailAddress ea " +
					" where ea=t.emailAddress and ea.rowId = :rowId");
			query.setParameter("rowId", emailAddrRowId);
			@SuppressWarnings("unchecked")
			List<EmailBroadcast> list = (List<EmailBroadcast>) query.getResultList();
			//em.lock(EmailBroadcast, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<EmailBroadcast> getByBroadcastDataRowId(int broadcastDataRowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailBroadcast t, BroadcastData bd " +
					" where bd=t.broadcastData and bd.rowId=:rowId ");
			query.setParameter("rowId", broadcastDataRowId);
			@SuppressWarnings("unchecked")
			List<EmailBroadcast> list = (List<EmailBroadcast>) query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(EmailBroadcast broadcast) {
		if (broadcast == null) return;
		try {
			em.remove(broadcast);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from EmailBroadcast t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(EmailBroadcast broadcast) {
		try {
			em.persist(broadcast);
			em.flush();
		}
		finally {
		}
	}

	public void update(EmailBroadcast broadcast) {
		try {
			if (em.contains(broadcast)) {
				em.persist(broadcast);
			}
			else {
				em.merge(broadcast);
			}
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}
}
