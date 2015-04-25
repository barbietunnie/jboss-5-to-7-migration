package jpa.service.maillist;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.model.BroadcastData;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("broadcastDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastDataService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = Logger.getLogger(BroadcastDataService.class);
	
	@Autowired
	EntityManager em;
	
	public BroadcastData getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastData t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			BroadcastData broadcast = (BroadcastData) query.getSingleResult();
			//em.lock(broadcast, LockModeType.OPTIMISTIC);
			return broadcast;
		}
		finally {
		}
	}
	
	public List<BroadcastData> getByMailingListId(String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastData t, MailingList ml " +
					" where ml=t.mailingList and ml.listId = :listId");
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<BroadcastData> list = (List<BroadcastData>) query.getResultList();
			//em.lock(BroadcastData, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<BroadcastData> getByEmailTemplateId(String templateId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastData t, EmailTemplate et " +
					" where et=t.emailTemplate and et.templateId=:templateId ");
			query.setParameter("templateId", templateId);
			@SuppressWarnings("unchecked")
			List<BroadcastData> list = (List<BroadcastData>) query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<BroadcastData> getAll() {
		String sql = "select t from BroadcastData t order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			@SuppressWarnings("unchecked")
			List<BroadcastData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(BroadcastData broadcast) {
		if (broadcast == null) return;
		try {
			em.remove(broadcast);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from BroadcastData t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(BroadcastData broadcast) {
		try {
			em.persist(broadcast);
			em.flush();
		}
		finally {
		}
	}

	public void update(BroadcastData broadcast) {
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
