package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.ActionDetail;

@Component("actionDetailService")
@Transactional(propagation=Propagation.REQUIRED)
public class ActionDetailService {
	static Logger logger = Logger.getLogger(ActionDetailService.class);
	
	@Autowired
	EntityManager em;

	public ActionDetail getByActionId(String actionId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ActionDetail t where " +
					"t.actionId = :actionId ");
			query.setParameter("actionId", actionId);
			ActionDetail detail = (ActionDetail) query.getSingleResult();
			em.lock(detail, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return detail;
		}
		finally {
		}
	}
	
	public ActionDetail getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ActionDetail t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			ActionDetail detail = (ActionDetail) query.getSingleResult();
			em.lock(detail, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return detail;
		}
		finally {
		}
	}
	
	public List<ActionDetail> getAll() {
		try {
			Query query = em.createQuery("select t from ActionDetail t");
			@SuppressWarnings("unchecked")
			List<ActionDetail> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(ActionDetail detail) {
		if (detail==null) return;
		try {
			em.remove(detail);
		}
		finally {
		}
	}

	public int deleteByActionId(String actionId) {
		try {
			Query query = em.createQuery("delete from ActionDetail t where " +
					"t.actionId=:actionId ");
			query.setParameter("actionId", actionId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from ActionDetail t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(ActionDetail detail) {
		try {
			em.persist(detail);
		}
		finally {
		}
	}
	
	public void update(ActionDetail detail) {
		try {
			if (em.contains(detail)) {
				em.persist(detail);
			}
			else {
				em.merge(detail);
			}
		}
		finally {
		}
	}
	
}
