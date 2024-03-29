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

import jpa.model.IdTokens;

@Component("idTokensService")
@Transactional(propagation=Propagation.REQUIRED)
public class IdTokensService {
	static Logger logger = Logger.getLogger(IdTokensService.class);
	
	/*
	 * XXX This next annotation triggers JBoss to look for the persistence unit name "MessageDB"
	 * when it is bundled and deployed in a war file. Use spring @Autowired injection instead.
	 */
	//@PersistenceContext(unitName="MessageDB")
	@Autowired
	EntityManager em;
	
	public IdTokens getByClientId(String clientId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from IdTokens t, ClientData cd " +
					" where cd=t.clientData and cd.clientId = :clientId");
			query.setParameter("clientId", clientId);
			IdTokens idTokens = (IdTokens) query.getSingleResult();
			em.lock(idTokens, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return idTokens;
		}
		finally {
		}
	}
	
	public IdTokens getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from IdTokens t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			IdTokens idTokens = (IdTokens) query.getSingleResult();
			em.lock(idTokens, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return idTokens;
		}
		finally {
		}
	}
	
	public List<IdTokens> getAll() {
		try {
			Query query = em.createQuery("select t from IdTokens t");
			@SuppressWarnings("unchecked")
			List<IdTokens> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(IdTokens idTokens) {
		if (idTokens == null) return;
		try {
			em.remove(idTokens);
		}
		finally {
		}
	}

	public int deleteByClientId(String clientId) {
		try {
			Query query = em.createNativeQuery("delete from Id_Tokens where clientDataRowId in " +
					" (select row_id from client_data cd where cd.clientId=?1)");
			query.setParameter(1, clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from IdTokens t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(IdTokens idTokens) {
		try {
			em.persist(idTokens);
		}
		finally {
		}
	}

	public void update(IdTokens idTokens) {
		try {
			if (em.contains(idTokens)) {
				em.persist(idTokens);
			}
			else {
				em.merge(idTokens);
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
