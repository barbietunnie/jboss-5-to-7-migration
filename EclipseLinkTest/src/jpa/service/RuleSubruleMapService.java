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

import jpa.model.RuleSubruleMap;

@Component("ruleSubruleMapService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleSubruleMapService {
	static Logger logger = Logger.getLogger(RuleSubruleMapService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public RuleSubruleMap getByPrimaryKey(String ruleName, String subruleName) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleSubruleMap t, RuleLogic rl1, RuleLogic rl2 " +
					"where t.ruleLogic = rl1 and t.subruleLogic = rl2 " +
					"and rl1.ruleName=:ruleName and rl2.ruleName=:subruleName ");
			query.setParameter("ruleName", ruleName);
			query.setParameter("subruleName", subruleName);
			RuleSubruleMap rsmap = (RuleSubruleMap) query.getSingleResult();
			em.lock(rsmap, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return rsmap;
		}
		finally {
		}
	}

	public List<RuleSubruleMap> getByRuleName(String ruleName) {
		try {
			Query query = em.createQuery("select t from RuleSubruleMap t, RuleLogic rl " +
					" where t.ruleLogic=rl and rl.ruleName = :ruleName " +
					" order by t.subruleSequence asc");
			query.setParameter("ruleName", ruleName);
			@SuppressWarnings("unchecked")
			List<RuleSubruleMap> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public RuleSubruleMap getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleSubruleMap t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			RuleSubruleMap rsmap = (RuleSubruleMap) query.getSingleResult();
			em.lock(rsmap, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return rsmap;
		}
		finally {
		}
	}
	
	public void delete(RuleSubruleMap rsmap) {
		if (rsmap == null) return;
		try {
			em.remove(rsmap);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public int deleteByRuleName(String ruleName) {
		try {
			Query query = em.createNativeQuery("delete from Rule_Subrule_Map where RuleLogicRowId = " +
					" (select Row_id from rule_logic rl where rl.ruleName=?1) ");
			query.setParameter(1, ruleName);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(String ruleName, String subruleName) {
		try {
			Query query = em.createNativeQuery("delete from Rule_Subrule_Map where " +
					"RuleLogicRowId = (select Row_Id from rule_logic rl1 where rl1.ruleName=?1) " +
					"and SubruleLogicRowId = (select Row_Id from rule_logic rl2 where rl2.ruleName=?2) ");
			query.setParameter(1, ruleName);
			query.setParameter(2, subruleName);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from RuleSubruleMap t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleSubruleMap rsmap) {
		try {
			em.persist(rsmap);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public void update(RuleSubruleMap rsmap) {
		try {
			if (em.contains(rsmap)) {
				em.persist(rsmap);
			}
			else {
				em.merge(rsmap);
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
