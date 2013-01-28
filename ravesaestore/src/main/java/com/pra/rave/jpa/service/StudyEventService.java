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

import com.pra.rave.jpa.model.StudyEvent;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("studyEventService")
@Transactional(propagation=Propagation.REQUIRED)
public class StudyEventService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public StudyEvent getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from StudyEvent t where t.id = :id");
			query.setParameter("id", id);
			StudyEvent studyEvent = (StudyEvent) query.getSingleResult();
			em.lock(studyEvent, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return studyEvent;
		}
		finally {
		}
	}
	
	public StudyEvent getByPrimaryKey(StudyPK studyPK, String subjectKey, String studyEventOid, String studyEventRepeatKey)
			throws NoResultException {
		try {
			Query query = em.createQuery("select se from Study stdy, Subject subj, StudyEvent se " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			StudyEvent studyEvent = (StudyEvent) query.getSingleResult();
			em.lock(studyEvent, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return studyEvent;
		}
		finally {
		}
	}
	
	public List<StudyEvent> getByAncestorKeys(StudyPK studyPK, String subjectKey) {
		try {
			Query query = em.createQuery("select se from Study stdy, Subject subj, StudyEvent se " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			@SuppressWarnings("unchecked")
			List<StudyEvent> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<StudyEvent> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select se from Study stdy, Subject subj, StudyEvent se " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<StudyEvent> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(StudyEvent studyEvent) {
		if (studyEvent == null) return;
		try {
			em.remove(studyEvent);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from StudyEvent t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(StudyEvent studyEvent) {
		try {
			em.persist(studyEvent);
		}
		finally {
		}
	}

	public void update(StudyEvent studyEvent) {
		try {
			em.persist(studyEvent);
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}
}
