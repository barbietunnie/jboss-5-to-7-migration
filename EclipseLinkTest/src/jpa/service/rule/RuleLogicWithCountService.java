package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.constant.StatusId;
import jpa.model.rule.RuleLogic;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleLogicWithCountService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleLogicWithCountService {
	static Logger logger = Logger.getLogger(RuleLogicService.class);
	
	@Autowired
	EntityManager em;
	
	/*
	 * Returns an array of following elements:
	 * 1) RuleLogic
	 * 2) BigDecimal for sub-rule count
	 */
	public Object[] getByRuleName(String ruleName) throws NoResultException {
		String sql = 
				"select r.*, " +
					"count(s.RuleLogicRowId) as subruleCount " +
				" from Rule_Logic r " +
					" left outer join Rule_SubRule_Map s on r.Row_Id=s.RuleLogicRowId " +
				" where r.ruleName=?1 " +
				" group by " +
					"r.Row_Id, " +
					"r.RuleName, " +
					"r.EvalSequence, " +
					"r.RuleType, " +
					"r.StatusId, " +
					"r.StartTime, " +
					"r.MailType, " +
					"r.RuleCategory, " +
					"r.IsSubrule, " +
					"r.IsBuiltinRule, " +
					"r.Description ";
		try {
			Query query = em.createNativeQuery(sql, RuleLogic.MAPPING_RULE_LOGIC_WITH_COUNT);
			query.setParameter(1, ruleName);
			Object[] logic = (Object[]) query.getSingleResult();
			return logic;
		}
		finally {
		}
	}

	public List<Object[]> getByActiveRules() throws NoResultException {
		String sql = 
				"select r.*, " +
					"count(s.RuleLogicRowId) as subruleCount " +
				" from Rule_Logic r " +
					" left outer join Rule_SubRule_Map s on r.Row_Id=s.RuleLogicRowId " +
				" where r.statusId=?1 and r.startTime<=?2 " +
				" group by " +
					"r.Row_Id, " +
					"r.RuleName, " +
					"r.EvalSequence, " +
					"r.RuleType, " +
					"r.StatusId, " +
					"r.StartTime, " +
					"r.MailType, " +
					"r.RuleCategory, " +
					"r.IsSubrule, " +
					"r.IsBuiltinRule, " +
					"r.Description ";
		try {
			Query query = em.createNativeQuery(sql, RuleLogic.MAPPING_RULE_LOGIC_WITH_COUNT);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new java.sql.Timestamp(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<Object[]> list = query.getResultList();
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
	
	public List<Object[]> getAll(boolean builtinRules) {
		String sql = 
				"select r.*, " +
					"count(s.RuleLogicRowId) as subruleCount " +
				" from Rule_Logic r " +
					" left outer join Rule_SubRule_Map s on r.Row_Id=s.RuleLogicRowId " +
				" where r.IsBuiltinRule=1? " +
				" group by " +
					"r.Row_Id, " +
					"r.RuleName, " +
					"r.EvalSequence, " +
					"r.RuleType, " +
					"r.StatusId, " +
					"r.StartTime, " +
					"r.MailType, " +
					"r.RuleCategory, " +
					"r.IsSubrule, " +
					"r.IsBuiltinRule, " +
					"r.Description ";
		try {
			Query query = em.createNativeQuery(sql, RuleLogic.MAPPING_RULE_LOGIC_WITH_COUNT);
			query.setParameter(1, builtinRules);
			@SuppressWarnings("unchecked")
			List<Object[]> list = query.getResultList();
			return list;
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
		}
		finally {
		}
	}

	public int deleteByRuleName(String ruleName) {
		try {
			Query query = em.createQuery("delete from RuleLogic t where t.ruleName=:ruleName ");
			query.setParameter("ruleName", ruleName);
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
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleLogic logic) {
		try {
			em.persist(logic);
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
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}

}
