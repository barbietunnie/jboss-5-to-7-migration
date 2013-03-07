package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.MobileCarrier;

@Component("mobileCarrierService")
@Transactional(propagation=Propagation.REQUIRED)
public class MobileCarrierService {
	static Logger logger = Logger.getLogger(MobileCarrierService.class);
	
	@Autowired
	EntityManager em;
	
	public MobileCarrier getByCarrierId(String carrierId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MobileCarrier t " +
					" where t.carrierId = :carrierId");
			query.setParameter("carrierId", carrierId);
			MobileCarrier carrier = (MobileCarrier) query.getSingleResult();
			em.lock(carrier, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return carrier;
		}
		finally {
		}
	}
	
	public MobileCarrier getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MobileCarrier t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			MobileCarrier carrier = (MobileCarrier) query.getSingleResult();
			em.lock(carrier, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return carrier;
		}
		finally {
		}
	}
	
	public List<MobileCarrier> getAll() {
		try {
			Query query = em.createQuery("select t from MobileCarrier t");
			@SuppressWarnings("unchecked")
			List<MobileCarrier> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(MobileCarrier carrier) {
		if (carrier == null) return;
		try {
			em.remove(carrier);
		}
		finally {
		}
	}

	public int deleteByCarrierId(String carrierId) {
		try {
			Query query = em.createQuery("delete from MobileCarrier t where " +
					" t.carrierId=:carrierId");
			query.setParameter("carrierId", carrierId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from MobileCarrier t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MobileCarrier carrier) {
		try {
			em.persist(carrier);
			em.flush();
		}
		finally {
		}
	}

	public void update(MobileCarrier carrier) {
		try {
			if (em.contains(carrier)) {
				em.persist(carrier);
			}
			else {
				em.merge(carrier);
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
