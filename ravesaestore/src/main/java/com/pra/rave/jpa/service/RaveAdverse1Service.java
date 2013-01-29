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

import com.pra.rave.jpa.model.RaveAdverse1;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("raveAdverse1Service")
@Transactional(propagation=Propagation.REQUIRED)
public class RaveAdverse1Service {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public RaveAdverse1 getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RaveAdverse1 t where t.id = :id");
			query.setParameter("id", id);
			RaveAdverse1 adverse1 = (RaveAdverse1) query.getSingleResult();
			em.lock(adverse1, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return adverse1;
		}
		finally {
		}
	}
	
	public List<RaveAdverse1> getByCombinedKeys(String itemGroupId, String caseNum) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from ItemGroup ig, RaveAdverse1 ae " +
					"where ig=ae.itemGroup and ae.itemGroupId=:itemGroupId and ae.aecasnum=:caseNum ");
			query.setParameter("itemGroupId", itemGroupId);
			query.setParameter("caseNum", caseNum);
			@SuppressWarnings("unchecked")
			List<RaveAdverse1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveAdverse1> getByAncestorKeys(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveAdverse1 ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey " +
					"and fm=ig.formData and fm.formOID=:formOid and fm.formRepeatKey=:formRepeatKey " +
					"and ig=id.itemGroup and ig.itemGroupOID=:itemGroupOid and ig.itemGroupRepeatKey=:itemGroupRepeatKey ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			query.setParameter("formOid", formOid);
			query.setParameter("formRepeatKey", formRepeatKey);
			query.setParameter("itemGroupOid", itemGroupOid);
			query.setParameter("itemGroupRepeatKey", itemGroupRepeatKey);
			@SuppressWarnings("unchecked")
			List<RaveAdverse1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveAdverse1> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveAdverse1 ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<RaveAdverse1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
		
	public void delete(RaveAdverse1 adverse1) {
		if (adverse1 == null) return;
		try {
			em.remove(adverse1);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from RaveAdverse1 t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RaveAdverse1 adverse1) {
		try {
			em.persist(adverse1);
		}
		finally {
		}
	}

	public void update(RaveAdverse1 adverse1) {
		try {
			if (em.contains(adverse1)) {
				em.persist(adverse1);
			}
			else { // detached
				em.merge(adverse1);
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
