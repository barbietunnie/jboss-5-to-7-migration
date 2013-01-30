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

import com.pra.rave.jpa.model.RaveIpadmin1;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("raveIpadmin1Service")
@Transactional(propagation=Propagation.REQUIRED)
public class RaveIpadmin1Service {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public RaveIpadmin1 getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RaveIpadmin1 t where t.id = :id");
			query.setParameter("id", id);
			RaveIpadmin1 ipadmin1 = (RaveIpadmin1) query.getSingleResult();
			em.lock(ipadmin1, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return ipadmin1;
		}
		finally {
		}
	}
	
	public RaveIpadmin1 getByItemGroupId(int itemGroupId) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from ItemGroup ig, RaveIpadmin1 ae " +
					"where ig=ae.itemGroup and ig.id=:itemGroupId ");
			query.setParameter("itemGroupId", itemGroupId);
			RaveIpadmin1 ipadmin1 = (RaveIpadmin1) query.getSingleResult();
			return ipadmin1;
		}
		finally {
		}
	}

	public List<RaveIpadmin1> getByAncestors(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveIpadmin1 ae " +
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
			List<RaveIpadmin1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveIpadmin1> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveIpadmin1 ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<RaveIpadmin1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
		
	public void delete(RaveIpadmin1 ipadmin1) {
		if (ipadmin1 == null) return;
		try {
			em.remove(ipadmin1);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from RaveIpadmin1 t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RaveIpadmin1 ipadmin1) {
		try {
			em.persist(ipadmin1);
		}
		finally {
		}
	}

	public void update(RaveIpadmin1 ipadmin1) {
		try {
			if (em.contains(ipadmin1)) {
				em.persist(ipadmin1);
			}
			else { // detached
				em.merge(ipadmin1);
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
