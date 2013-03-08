package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.rule.RuleLogic;
import jpa.service.ReloadFlagsService;

@Component("ruleLogicService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleLogicService {
	static Logger logger = Logger.getLogger(RuleLogicService.class);
	
	@PersistenceContext(unitName="MessageDB")
	//@Autowired
	EntityManager em;
	
	@Autowired
	ReloadFlagsService reloadFlagsService;
	
	public RuleLogic getByRuleName(String ruleName) throws NoResultException {
		String sql = 
				"select r from RuleLogic r where r.ruleName=:ruleName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			RuleLogic logic = (RuleLogic) query.getSingleResult();
			em.lock(logic, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return logic;
		}
		finally {
		}
	}

	public List<RuleLogic> getAll(boolean builtinRules) {
		String sql = 
				"select r from RuleLogic r where r.isBuiltinRule=:builtinRules " +
				" order by r.rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("builtinRules", builtinRules);
			@SuppressWarnings("unchecked")
			List<RuleLogic> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RuleLogic> getActiveRules() throws NoResultException {
		String sql = 
				"select r from RuleLogic r where r.statusId=:statusId and r.startTime<=:startTime " +
				" order by r.rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("statusId", StatusId.ACTIVE.getValue());
			query.setParameter("startTime", new java.sql.Timestamp(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<RuleLogic> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<RuleLogic> getSubRules(boolean excludeBuiltin) throws NoResultException {
		String sql = 
				"select r from RuleLogic r where r.isSubrule=:isSubrule ";
		if (excludeBuiltin) {
			sql += " and r.isBuiltinRule=:isBuiltinRule ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("isSubrule", true);
			if (excludeBuiltin) {
				query.setParameter("isBuiltinRule", false);
			}
			@SuppressWarnings("unchecked")
			List<RuleLogic> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public RuleLogic getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleLogic t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			RuleLogic logic = (RuleLogic) query.getSingleResult();
			em.lock(logic, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return logic;
		}
		finally {
		}
	}
	
	public int getNextEvalSequence() {
		try {
			Query query = em.createNativeQuery("select max(evalSequence) from Rule_Logic");
			Integer result = (Integer) query.getSingleResult();
			return (result==null?0:result);
		}
		finally {
		}
	}

	public void delete(RuleLogic logic) {
		if (logic == null) return;
		try {
			em.remove(logic);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public int deleteByRuleName(String ruleName) {
		try {
			Query query = em.createQuery("delete from RuleLogic t where t.ruleName=:ruleName ");
			query.setParameter("ruleName", ruleName);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from RuleLogic t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			reloadFlagsService.updateRuleReloadFlag();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleLogic logic) {
		try {
			em.persist(logic);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public void update(RuleLogic logic) {
		try {
			if (em.contains(logic)) {
				em.persist(logic);
			}
			else {
				em.merge(logic);
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
