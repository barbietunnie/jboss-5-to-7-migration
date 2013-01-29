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

import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.util.logger.LoggerHelper;

@Component("subjectService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubjectService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public Subject getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Subject t where t.id = :id");
			query.setParameter("id", id);
			Subject subject = (Subject) query.getSingleResult();
			em.lock(subject, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return subject;
		}
		finally {
		}
	}
	
	public Subject getByPrimaryKey(StudyPK studyPK, String subjectKey) throws NoResultException {
		try {
			Query query = em.createQuery("select subj from Subject subj, Study stdy " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid and subj.subjectKey=:subjectKey");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			Subject subject = (Subject) query.getSingleResult();
			em.lock(subject, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return subject;
		}
		finally {
		}
	}
	
	public List<Subject> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select subj from Subject subj, Study stdy " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<Subject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<Subject> getBySubjectKey(String subjectKey) {
		try {
			Query query = em.createQuery("select t from Subject t where t.subjectKey = :subjectKey");
			query.setParameter("subjectKey", subjectKey);
			@SuppressWarnings("unchecked")
			List<Subject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<Subject> getBySiteOid(String siteOid) {
		try {
			Query query = em.createQuery("select t from Subject t where t.locationOID = :siteOid");
			query.setParameter("siteOid", siteOid);
			@SuppressWarnings("unchecked")
			List<Subject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<Subject> getAll() {
		try {
			Query query = em.createQuery("select t from Subject t");
			@SuppressWarnings("unchecked")
			List<Subject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(Subject subject) {
		if (subject == null) return;
		try {
			em.remove(subject);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from Subject t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(Subject subject) {
		try {
			em.persist(subject);
		}
		finally {
		}
	}

	public void update(Subject subject) {
		try {
			if (em.contains(subject)) {
				em.persist(subject);
			}
			else { // detached
				em.merge(subject);
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
