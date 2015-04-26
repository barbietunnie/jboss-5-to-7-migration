package jpa.service.maillist;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.model.BroadcastTracking;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("broadcastTrackingService")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastTrackingService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = Logger.getLogger(BroadcastTrackingService.class);
	
	@Autowired
	EntityManager em;
	
	public BroadcastTracking getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastTracking t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			BroadcastTracking broadcast = (BroadcastTracking) query.getSingleResult();
			//em.lock(broadcast, LockModeType.OPTIMISTIC);
			return broadcast;
		}
		finally {
		}
	}
	
	public List<BroadcastTracking> getByEmailAddress(String address) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastTracking t, EmailAddress ea " +
					" where ea=t.emailAddress and ea.address = :address");
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<BroadcastTracking> list = (List<BroadcastTracking>) query.getResultList();
			//em.lock(BroadcastTracking, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<BroadcastTracking> getByEmailAddrRowId(int emailAddrRowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastTracking t, EmailAddress ea " +
					" where ea=t.emailAddress and ea.rowId = :rowId");
			query.setParameter("rowId", emailAddrRowId);
			@SuppressWarnings("unchecked")
			List<BroadcastTracking> list = (List<BroadcastTracking>) query.getResultList();
			//em.lock(BroadcastTracking, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<BroadcastTracking> getByBroadcastDataRowId(int broadcastDataRowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastTracking t, BroadcastMessage bd " +
					" where bd=t.broadcastMessage and bd.rowId=:rowId ");
			query.setParameter("rowId", broadcastDataRowId);
			@SuppressWarnings("unchecked")
			List<BroadcastTracking> list = (List<BroadcastTracking>) query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(BroadcastTracking broadcast) {
		if (broadcast == null) return;
		try {
			em.remove(broadcast);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from BroadcastTracking t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(BroadcastTracking broadcast) {
		try {
			em.persist(broadcast);
			em.flush();
		}
		finally {
		}
	}

	public void update(BroadcastTracking broadcast) {
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
