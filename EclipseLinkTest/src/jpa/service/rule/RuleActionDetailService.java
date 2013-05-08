package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.rule.RuleActionDetail;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleActionDetailService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleActionDetailService {
	static Logger logger = Logger.getLogger(RuleActionDetailService.class);
	
	@Autowired
	EntityManager em;

	public RuleActionDetail getByActionId(String actionId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleActionDetail t where " +
					"t.actionId = :actionId ");
			query.setParameter("actionId", actionId);
			RuleActionDetail detail = (RuleActionDetail) query.getSingleResult();
			//em.lock(detail, LockModeType.OPTIMISTIC);
			return detail;
		}
		finally {
		}
	}
	
	public RuleActionDetail getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleActionDetail t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			RuleActionDetail detail = (RuleActionDetail) query.getSingleResult();
			//em.lock(detail, LockModeType.OPTIMISTIC);
			return detail;
		}
		finally {
		}
	}
	
	public List<RuleActionDetail> getAll() {
		try {
			Query query = em.createQuery("select t from RuleActionDetail t");
			@SuppressWarnings("unchecked")
			List<RuleActionDetail> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(RuleActionDetail detail) {
		if (detail==null) return;
		try {
			em.remove(detail);
		}
		finally {
		}
	}

	public int deleteByActionId(String actionId) {
		try {
			Query query = em.createQuery("delete from RuleActionDetail t where " +
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
			Query query = em.createQuery("delete from RuleActionDetail t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleActionDetail detail) {
		try {
			em.persist(detail);
		}
		finally {
		}
	}
	
	public void update(RuleActionDetail detail) {
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
	
	public List<String> getActionIdList() {
		String sql = 
			"select distinct(ActionId) as actionId from Rule_Action_Detail " +
			" order by ActionId";

		Query query = em.createNativeQuery(sql);
		@SuppressWarnings("unchecked")
		List<String> list = query.getResultList();
		return list;
	}

}
