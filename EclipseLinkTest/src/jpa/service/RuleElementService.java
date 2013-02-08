package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.RuleElement;

@Component("ruleElementService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleElementService {
	static Logger logger = Logger.getLogger(RuleElementService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public RuleElement getByPrimaryKey(String ruleName, int elementSequence) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleElement t, RuleLogic rl " +
					" where t.ruleLogic=rl and rl.ruleName = :ruleName " +
					"and t.elementSequence=:elementSequence ");
			query.setParameter("ruleName", ruleName);
			query.setParameter("elementSequence", elementSequence);
			RuleElement element = (RuleElement) query.getSingleResult();
			em.lock(element, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return element;
		}
		finally {
		}
	}

	public List<RuleElement> getByRuleName(String ruleName) {
		try {
			Query query = em.createQuery("select t from RuleElement t, RuleLogic rl  " +
					" where t.ruleLogic=rl and rl.ruleName = :ruleName " +
					" order by t.elementSequence asc ");
			query.setParameter("ruleName", ruleName);
			@SuppressWarnings("unchecked")
			List<RuleElement> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public RuleElement getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleElement t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			RuleElement element = (RuleElement) query.getSingleResult();
			em.lock(element, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return element;
		}
		finally {
		}
	}
	
	public List<RuleElement> getAll() {
		try {
			Query query = em.createQuery("select t from RuleElement t " +
					"order by t.ruleLogic.ruleName asc, t.elementSequence asc");
			@SuppressWarnings("unchecked")
			List<RuleElement> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(RuleElement element) {
		if (element == null) return;
		try {
			em.remove(element);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public int deleteByRuleName(String ruleName) {
		try {
			Query query = em.createNativeQuery("delete from Rule_Element where RuleLogicRowid = " +
					"(select Row_id from rule_logic rl where rl.ruleName=?1)");
			query.setParameter(1, ruleName);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(String ruleName, int elementSequence) {
		try {
			Query query = em.createNativeQuery("delete from Rule_Element where RuleLogicRowid = " +
					"(select Row_id from rule_logic rl where rl.ruleName=?1) " +
					"and elementSequence=?2 ");
			query.setParameter(1, ruleName);
			query.setParameter(2, elementSequence);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from RuleElement t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleElement element) {
		try {
			em.persist(element);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public void update(RuleElement element) {
		try {
			if (em.contains(element)) {
				em.persist(element);
			}
			else {
				em.merge(element);
			}
			reloadFlagsService.updateRuleReloadFlag();
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}
}
