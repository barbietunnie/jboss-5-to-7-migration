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

import com.pra.rave.jpa.model.RaveSrf1;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("raveSrf1Service")
@Transactional(propagation=Propagation.REQUIRED)
public class RaveSrf1Service {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public RaveSrf1 getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RaveSrf1 t where t.id = :id");
			query.setParameter("id", id);
			RaveSrf1 srf1 = (RaveSrf1) query.getSingleResult();
			em.lock(srf1, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return srf1;
		}
		finally {
		}
	}
	
	public RaveSrf1 getByItemGroupId(int itemGroupId) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from ItemGroup ig, RaveSrf1 ae " +
					"where ig=ae.itemGroup and ig.id=:itemGroupId ");
			query.setParameter("itemGroupId", itemGroupId);
			RaveSrf1 srf1 = (RaveSrf1) query.getSingleResult();
			return srf1;
		}
		finally {
		}
	}

	public List<RaveSrf1> getByCaseNumber(String caseNum) throws NoResultException {
		try {
			Query query = em.createQuery("select ae from RaveSrf1 ae " +
					"where ae.srcasnum=:caseNum ");
			query.setParameter("caseNum", caseNum);
			@SuppressWarnings("unchecked")
			List<RaveSrf1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveSrf1> getByAncestors(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveSrf1 ae " +
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
			List<RaveSrf1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RaveSrf1> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select ae from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, RaveSrf1 ae " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<RaveSrf1> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
		
	public void delete(RaveSrf1 srf1) {
		if (srf1 == null) return;
		try {
			em.remove(srf1);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from RaveSrf1 t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RaveSrf1 srf1) {
		try {
			em.persist(srf1);
		}
		finally {
		}
	}

	public void update(RaveSrf1 srf1) {
		try {
			if (em.contains(srf1)) {
				em.persist(srf1);
			}
			else { // detached
				em.merge(srf1);
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
