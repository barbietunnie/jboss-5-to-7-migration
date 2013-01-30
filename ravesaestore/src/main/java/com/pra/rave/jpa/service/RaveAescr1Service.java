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

import com.pra.rave.jpa.model.RaveAescr1;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("raveAescr1Service")
@Transactional(propagation=Propagation.REQUIRED)
public class RaveAescr1Service {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public RaveAescr1 getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RaveAescr1 t where t.id = :id");
			query.setParameter("id", id);
			RaveAescr1 aescr1 = (RaveAescr1) query.getSingleResult();
			em.lock(aescr1, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return aescr1;
		}
		finally {
		}
	}
	
	public RaveAescr1 getByItemGroupId(int itemGroupId) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from ItemGroup ig, RaveAescr1 ae " +
					"where ig=ae.itemGroup and ig.id=:itemGroupId ");
			query.setParameter("itemGroupId", itemGroupId);
			RaveAescr1 aescr1 = (RaveAescr1) query.getSingleResult();
			return aescr1;
		}
		finally {
		}
	}

	public List<RaveAescr1> getByCaseNumber(String caseNum) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from RaveAescr1 ae " +
					"where ae.aecasnum=:caseNum ");
			query.setParameter("caseNum", caseNum);
			@SuppressWarnings("unchecked")
			List<RaveAescr1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveAescr1> getByAncestors(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveAescr1 ae " +
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
			List<RaveAescr1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveAescr1> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveAescr1 ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<RaveAescr1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
		
	public void delete(RaveAescr1 aescr1) {
		if (aescr1 == null) return;
		try {
			em.remove(aescr1);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from RaveAescr1 t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RaveAescr1 aescr1) {
		try {
			em.persist(aescr1);
		}
		finally {
		}
	}

	public void update(RaveAescr1 aescr1) {
		try {
			if (em.contains(aescr1)) {
				em.persist(aescr1);
			}
			else { // detached
				em.merge(aescr1);
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
