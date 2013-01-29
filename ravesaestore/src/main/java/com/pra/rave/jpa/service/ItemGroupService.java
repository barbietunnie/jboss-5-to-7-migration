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

import com.pra.rave.jpa.model.ItemGroup;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("itemGroupService")
@Transactional(propagation=Propagation.REQUIRED)
public class ItemGroupService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public ItemGroup getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ItemGroup t where t.id = :id");
			query.setParameter("id", id);
			ItemGroup itemGroup = (ItemGroup) query.getSingleResult();
			em.lock(itemGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return itemGroup;
		}
		finally {
		}
	}
	
	public ItemGroup getByPrimaryKey(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) throws NoResultException {
		try {
			Query query = em.createQuery("select ig from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey " +
					"and fm=ig.formData and fm.formOID=:formOid and fm.formRepeatKey=:formRepeatKey " +
					"and ig.itemGroupOID=:itemGroupOid and ig.itemGroupRepeatKey=:itemGroupRepeatKey ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			query.setParameter("formOid", formOid);
			query.setParameter("formRepeatKey", formRepeatKey);
			query.setParameter("itemGroupOid", itemGroupOid);
			query.setParameter("itemGroupRepeatKey", itemGroupRepeatKey);
			ItemGroup itemGroup = (ItemGroup) query.getSingleResult();
			em.lock(itemGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return itemGroup;
		}
		finally {
		}
	}
	
	public List<ItemGroup> getByAncestorKeys(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey) {
		try {
			Query query = em.createQuery("select ig from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey " +
					"and fm=ig.formData and fm.formOID=:formOid and fm.formRepeatKey=:formRepeatKey ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			query.setParameter("formOid", formOid);
			query.setParameter("formRepeatKey", formRepeatKey);
			@SuppressWarnings("unchecked")
			List<ItemGroup> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<ItemGroup> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select ig from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<ItemGroup> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(ItemGroup itemGroup) {
		if (itemGroup == null) return;
		try {
			em.remove(itemGroup);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from ItemGroup t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(ItemGroup itemGroup) {
		try {
			em.persist(itemGroup);
		}
		finally {
		}
	}

	public void update(ItemGroup itemGroup) {
		try {
			if (em.contains(itemGroup)) {
				em.persist(itemGroup);
			}
			else { // detached
				em.merge(itemGroup);
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
