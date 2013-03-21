package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.model.rule.RuleSubruleMap;
import jpa.model.rule.RuleSubruleMapPK;
import jpa.service.ReloadFlagsService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleSubruleMapService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleSubruleMapService {
	static Logger logger = Logger.getLogger(RuleSubruleMapService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public RuleSubruleMap getByPrimaryKey(RuleSubruleMapPK pk) throws NoResultException {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		if (pk.getSubruleLogic()==null) {
			throw new IllegalArgumentException("A SubruleLogic instance must be provided in Primary Key object.");
		}
		try {
			Query query = em.createQuery("select t from RuleSubruleMap t, RuleLogic rl1, RuleLogic rl2 " +
					"where t.ruleSubruleMapPK.ruleLogic = rl1 and t.ruleSubruleMapPK.subruleLogic = rl2 " +
					"and rl1.ruleName=:ruleName and rl2.ruleName=:subruleName ");
			query.setParameter("ruleName", pk.getRuleLogic().getRuleName());
			query.setParameter("subruleName", pk.getSubruleLogic().getRuleName());
			RuleSubruleMap rsmap = (RuleSubruleMap) query.getSingleResult();
			//em.lock(rsmap, LockModeType.OPTIMISTIC);
			return rsmap;
		}
		finally {
		}
	}

	public List<RuleSubruleMap> getByRuleName(String ruleName) {
		try {
			Query query = em.createQuery("select t from RuleSubruleMap t, RuleLogic rl " +
					" where t.ruleSubruleMapPK.ruleLogic=rl and rl.ruleName = :ruleName " +
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
			//em.lock(rsmap, LockModeType.OPTIMISTIC);
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

	public int deleteByPrimaryKey(RuleSubruleMapPK pk) {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		if (pk.getSubruleLogic()==null) {
			throw new IllegalArgumentException("A SubruleLogic instance must be provided in Primary Key object.");
		}
		try {
			Query query = em.createNativeQuery("delete from Rule_Subrule_Map where " +
					"RuleLogicRowId = (select Row_Id from rule_logic rl1 where rl1.ruleName=?1) " +
					"and SubruleLogicRowId = (select Row_Id from rule_logic rl2 where rl2.ruleName=?2) ");
			query.setParameter(1, pk.getRuleLogic().getRuleName());
			query.setParameter(2, pk.getSubruleLogic().getRuleName());
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
