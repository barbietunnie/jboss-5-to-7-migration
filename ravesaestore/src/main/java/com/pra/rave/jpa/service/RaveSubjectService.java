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

import com.pra.rave.jpa.model.RaveSubject;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("raveSubjectService")
@Transactional(propagation=Propagation.REQUIRED)
public class RaveSubjectService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public RaveSubject getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RaveSubject t where t.id = :id");
			query.setParameter("id", id);
			RaveSubject subj = (RaveSubject) query.getSingleResult();
			em.lock(subj, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return subj;
		}
		finally {
		}
	}
	
	public RaveSubject getByItemGroupId(int itemGroupId) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from ItemGroup ig, RaveSubject ae " +
					"where ig=ae.itemGroup and ig.id=:itemGroupId ");
			query.setParameter("itemGroupId", itemGroupId);
			RaveSubject subj = (RaveSubject) query.getSingleResult();
			return subj;
		}
		finally {
		}
	}

	public List<RaveSubject> getByPatientId(String ptId) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from RaveSubject ae " +
					"where ae.pt_id=:ptId ");
			query.setParameter("ptId", ptId);
			@SuppressWarnings("unchecked")
			List<RaveSubject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveSubject> getByAncestors(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveSubject ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey " +
					"and fm=ig.formData and fm.formOID=:formOid and fm.formRepeatKey=:formRepeatKey " +
					"and ig=ae.itemGroup and ig.itemGroupOID=:itemGroupOid and ig.itemGroupRepeatKey=:itemGroupRepeatKey ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			query.setParameter("formOid", formOid);
			query.setParameter("formRepeatKey", formRepeatKey);
			query.setParameter("itemGroupOid", itemGroupOid);
			query.setParameter("itemGroupRepeatKey", itemGroupRepeatKey);
			@SuppressWarnings("unchecked")
			List<RaveSubject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveSubject> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveSubject ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<RaveSubject> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
		
	public void delete(RaveSubject subj) {
		if (subj == null) return;
		try {
			em.remove(subj);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from RaveSubject t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RaveSubject subj) {
		try {
			em.persist(subj);
		}
		finally {
		}
	}

	public void update(RaveSubject subj) {
		try {
			if (em.contains(subj)) {
				em.persist(subj);
			}
			else { // detached
				em.merge(subj);
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
