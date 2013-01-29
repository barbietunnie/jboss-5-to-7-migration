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

import com.pra.rave.jpa.model.FormData;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("formDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class FormDataService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public FormData getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from FormData t where t.id = :id");
			query.setParameter("id", id);
			FormData formData = (FormData) query.getSingleResult();
			em.lock(formData, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return formData;
		}
		finally {
		}
	}
	
	public FormData getByPrimaryKey(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey) throws NoResultException {
		try {
			Query query = em.createQuery("select fm from Study stdy, Subject subj, StudyEvent se, FormData fm " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey " +
					"and fm.formOID=:formOid and fm.formRepeatKey=:formRepeatKey");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			query.setParameter("formOid", formOid);
			query.setParameter("formRepeatKey", formRepeatKey);
			FormData formData = (FormData) query.getSingleResult();
			em.lock(formData, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return formData;
		}
		finally {
		}
	}
	
	public List<FormData> getByAncestorKeys(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey) {
		try {
			Query query = em.createQuery("select fm from Study stdy, Subject subj, StudyEvent se, FormData fm " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			@SuppressWarnings("unchecked")
			List<FormData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<FormData> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select fm from Study stdy, Subject subj, StudyEvent se, FormData fm " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<FormData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(FormData formData) {
		if (formData == null) return;
		try {
			em.remove(formData);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from FormData t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(FormData formData) {
		try {
			em.persist(formData);
		}
		finally {
		}
	}

	public void update(FormData formData) {
		try {
			if (em.contains(formData)) {
				em.persist(formData);
			}
			else { // detached
				em.merge(formData);
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
