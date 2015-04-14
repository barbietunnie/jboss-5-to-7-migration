package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.constant.RuleCategory;
import jpa.constant.StatusId;
import jpa.model.rule.RuleLogic;
import jpa.service.ReloadFlagsService;
import jpa.util.JpaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleLogicService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class RuleLogicService implements java.io.Serializable {
	private static final long serialVersionUID = 2402907648611630261L;

	static Logger logger = Logger.getLogger(RuleLogicService.class);
	
	static final boolean IsOptimisticLocking = false;
	
	//@PersistenceContext(unitName="MessageDB")
	@Autowired
	EntityManager em;
	
	@Autowired
	ReloadFlagsService reloadFlagsService;
	
	public RuleLogic getByRuleName(String ruleName) throws NoResultException {
		String sql = 
				"select r from RuleLogic r where r.ruleName=:ruleName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			if (StringUtils.containsIgnoreCase(JpaUtil.getJpaDialect(), "EclipseLink")) {
				query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
			}
			RuleLogic logic = (RuleLogic) query.getSingleResult();
			if (IsOptimisticLocking) {
				//em.lock(logic, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
				em.lock(logic, LockModeType.OPTIMISTIC);
			}
			return logic;
		}
		finally {
		}
	}

	public List<RuleLogic> getAll(boolean builtinRules) {
		String sql = 
				"select r from RuleLogic r where r.isBuiltinRule=:builtinRules " +
				" order by r.evalSequence";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("builtinRules", builtinRules);
			if (StringUtils.containsIgnoreCase(JpaUtil.getJpaDialect(), "EclipseLink")) {
				query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
			}
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

	public List<RuleLogic> getSubrules(boolean excludeBuiltin) throws NoResultException {
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
			//em.lock(logic, LockModeType.OPTIMISTIC);
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
			em.flush();
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
			em.flush();
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}

	public List<String> getBuiltinRuleNames4Web() {
		String sql = 
			"select RuleName " +
			" from Rule_Logic " +
			" where IsBuiltInRule=true and IsSubRule=false and RuleCategory=?1 " +
			" group by RuleName " +
			" order by RuleName ";

		Query query = em.createNativeQuery(sql);
		query.setParameter(1, RuleCategory.MAIN_RULE.getValue());
		@SuppressWarnings("unchecked")
		List<String> list = query.getResultList();
		return list;
	}
	
	public List<String> getCustomRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) as ruleName " +
			" from Rule_Logic " +
			" where IsBuiltInRule=false and IsSubRule=false and RuleCategory=?1 " +
			" order by RuleName ";

		Query query = em.createNativeQuery(sql);
		query.setParameter(1, RuleCategory.MAIN_RULE.getValue());
		@SuppressWarnings("unchecked")
		List<String> list = query.getResultList();
		return list;
	}

}
