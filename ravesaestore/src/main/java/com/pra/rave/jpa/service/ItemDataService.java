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

import com.pra.rave.jpa.model.ItemData;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.util.logger.LoggerHelper;

@Component("ItemDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class ItemDataService {
	static Logger logger = LoggerHelper.getLogger();
	
	@PersistenceContext(unitName="ravestore")
	EntityManager em;
	
	public ItemData getById(int id) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ItemData t where t.id = :id");
			query.setParameter("id", id);
			ItemData itemData = (ItemData) query.getSingleResult();
			em.lock(itemData, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return itemData;
		}
		finally {
		}
	}
	
	public ItemData getByPrimaryKey(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey,
			String itemDataOid) throws NoResultException {
		try {
			Query query = em.createQuery("select id from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, ItemData id " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid " +
					"and subj=se.subject and subj.subjectKey=:subjectKey " +
					"and se=fm.studyEvent and se.studyEventOID=:studyEventOid and se.studyEventRepeatKey=:studyEventRepeatKey " +
					"and fm=ig.formData and fm.formOID=:formOid and fm.formRepeatKey=:formRepeatKey " +
					"and ig=id.itemGroup and ig.itemGroupOID=:itemGroupOid and ig.itemGroupRepeatKey=:itemGroupRepeatKey " +
					"and id.itemOID=:itemDataOid ");
			query.setParameter("studyOid", studyPK.getStudyOID());
			query.setParameter("subjectKey", subjectKey);
			query.setParameter("studyEventOid", studyEventOid);
			query.setParameter("studyEventRepeatKey", studyEventRepeatKey);
			query.setParameter("formOid", formOid);
			query.setParameter("formRepeatKey", formRepeatKey);
			query.setParameter("itemGroupOid", itemGroupOid);
			query.setParameter("itemGroupRepeatKey", itemGroupRepeatKey);
			query.setParameter("itemDataOid", itemDataOid);
			ItemData itemData = (ItemData) query.getSingleResult();
			em.lock(itemData, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return itemData;
		}
		finally {
		}
	}
	
	public List<ItemData> getByAncestorKeys(StudyPK studyPK, String subjectKey,
			String studyEventOid, String studyEventRepeatKey, String formOid,
			String formRepeatKey, String itemGroupOid, String itemGroupRepeatKey) {
		try {
			Query query = em.createQuery("select id from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, ItemData id " +
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
			List<ItemData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<ItemData> getByStudyPK(StudyPK studyPK) {
		try {
			Query query = em.createQuery("select id from Study stdy, Subject subj, StudyEvent se, FormData fm, ItemGroup ig, ItemData id " +
					"where stdy=subj.study and stdy.studyPK.studyOID=:studyOid");
			query.setParameter("studyOid", studyPK.getStudyOID());
			@SuppressWarnings("unchecked")
			List<ItemData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(ItemData itemData) {
		if (itemData == null) return;
		try {
			em.remove(itemData);
		}
		finally {
		}
	}

	public int deleteById(int id) {
		try {
			Query query = em.createQuery("delete from ItemData t where t.id=:id");
			query.setParameter("id", id);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(ItemData itemData) {
		try {
			em.persist(itemData);
		}
		finally {
		}
	}

	public void update(ItemData itemData) {
		try {
			em.persist(itemData);
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}
}
