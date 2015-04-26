package jpa.service.maillist;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.model.BroadcastMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("broadcastMessageService")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastMessageService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = Logger.getLogger(BroadcastMessageService.class);
	
	@Autowired
	EntityManager em;
	
	public BroadcastMessage getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastMessage t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			BroadcastMessage broadcast = (BroadcastMessage) query.getSingleResult();
			//em.lock(broadcast, LockModeType.OPTIMISTIC);
			return broadcast;
		}
		finally {
		}
	}
	
	public List<BroadcastMessage> getByMailingListId(String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastMessage t, MailingList ml " +
					" where ml=t.mailingList and ml.listId = :listId");
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<BroadcastMessage> list = (List<BroadcastMessage>) query.getResultList();
			//em.lock(BroadcastMessage, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<BroadcastMessage> getByEmailTemplateId(String templateId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastMessage t, EmailTemplate et " +
					" where et=t.emailTemplate and et.templateId=:templateId ");
			query.setParameter("templateId", templateId);
			@SuppressWarnings("unchecked")
			List<BroadcastMessage> list = (List<BroadcastMessage>) query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<BroadcastMessage> getAll() {
		String sql = "select t from BroadcastMessage t order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			@SuppressWarnings("unchecked")
			List<BroadcastMessage> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(BroadcastMessage broadcast) {
		if (broadcast == null) return;
		try {
			em.remove(broadcast);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from BroadcastMessage t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(BroadcastMessage broadcast) {
		try {
			em.persist(broadcast);
			em.flush();
		}
		finally {
		}
	}

	public void update(BroadcastMessage broadcast) {
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
