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
	
	public BroadcastTracking getByPrimaryKey(int emailAddrRowId, int broadcastMsgRowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastTracking t, EmailAddress ea, BroadcastMessage bm "
					+ " where ea=t.emailAddress and ea.rowId = :emailAddrRowId"
					+ " and bm=t.broadcastMessage and bm.rowId = :broadcastMsgRowId ");
			query.setParameter("emailAddrRowId", emailAddrRowId);
			query.setParameter("broadcastMsgRowId", broadcastMsgRowId);
			BroadcastTracking broadcast = (BroadcastTracking) query.getSingleResult();
			//em.lock(broadcast, LockModeType.OPTIMISTIC);
			return broadcast;
		}
		finally {
		}
	}
	
	public List<BroadcastTracking> getByEmailAddress(String address) {
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

	public List<BroadcastTracking> getByEmailAddrRowId(int emailAddrRowId) {
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

	public List<BroadcastTracking> getByBroadcastMessageRowId(int bcstMsgRowId) {
		try {
			Query query = em.createQuery("select t from BroadcastTracking t, BroadcastMessage bd " +
					" where bd=t.broadcastMessage and bd.rowId=:rowId ");
			query.setParameter("rowId", bcstMsgRowId);
			@SuppressWarnings("unchecked")
			List<BroadcastTracking> list = (List<BroadcastTracking>) query.getResultList();
			return list;
		}
		finally {
		}
	}

	public int updateSentCount(int rowId, int count) {
		String sql = "update BroadcastTracking t set t.sentCount = (t.sentCount + :count), t.updtTime = :time "
				+ " where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("count", count);
		query.setParameter("rowId", rowId);
		query.setParameter("time", new java.sql.Timestamp(System.currentTimeMillis()));
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
	}
	
	public int updateSentCount(int rowId) {
		return updateSentCount(rowId, 1);
	}

	public int updateOpenCount(int rowId) {
		String sql = "update BroadcastTracking t set t.openCount = (t.openCount + 1), t.lastOpenTime = :time "
				+ ", t.updtTime = :time where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("rowId", rowId);
		query.setParameter("time", new java.sql.Timestamp(System.currentTimeMillis()));
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
	}

	public int updateClickCount(int rowId) {
		String sql = "update BroadcastTracking t set t.clickCount = (t.clickCount + 1), t.lastClickTime = :time "
				+ ", t.updtTime = :time where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("rowId", rowId);
		query.setParameter("time", new java.sql.Timestamp(System.currentTimeMillis()));
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
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
