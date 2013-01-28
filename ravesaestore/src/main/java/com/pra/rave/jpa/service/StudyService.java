package com.pra.rave.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("studyService")
@Transactional(propagation=Propagation.REQUIRED)
public class StudyService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public Study getByStudyPK(StudyPK studyPK) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Study t where t.studyPK.studyOID = :studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			Study study = (Study) query.getSingleResult();
			em.lock(study, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return study;
		}
		finally {
		}
	}
	
	public Study getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Study t where t.id = :id");
			query.setParameter("id", id);
			Study study = (Study) query.getSingleResult();
			em.lock(study, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return study;
		}
		finally {
		}
	}
	
	public List<Study> getAll() {
		try {
			Query query = em.createQuery("select t from Study t");
			@SuppressWarnings("unchecked")
			List<Study> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(Study study) {
		if (study == null) return;
		try {
			em.remove(study);
		}
		finally {
		}
	}

	public int deleteByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("delete from Study t where t.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from Study t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(Study study) {
		try {
			em.persist(study);
		}
		finally {
		}
	}

	public void update(Study study) {
		try {
			if (em.contains(study)) {
				em.persist(study);
			}
			else { // entity is detached
				em.merge(study);
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
