package jpa.service.rule;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionPK;
import jpa.service.ReloadFlagsService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleActionService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleActionService {
	static Logger logger = Logger.getLogger(RuleActionService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	ReloadFlagsService reloadFlagsService;
	
	public List<RuleAction> getByRuleName(String ruleName) {
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al " +
				"and (r.ruleActionPK.senderData=c or r.ruleActionPK.senderData is null) " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"group by r " + // to get rid of duplicates from result set
				"order by r.ruleActionPK.actionSequence, r.ruleActionPK.startTime ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			@SuppressWarnings("unchecked")
			List<RuleAction> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<RuleAction> getByRuleName0(String ruleName) {
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al " +
				"and c.senderId=:senderId " +
				"and (r.ruleActionPK.senderData=c or r.ruleActionPK.senderData is null) " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"group by r, c.senderId " + // to get rid of duplicates from result set
				"order by r.ruleActionPK.actionSequence, c.senderId, r.ruleActionPK.startTime ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("senderId", Constants.DEFAULT_SENDER_ID);
			query.setParameter("ruleName", ruleName);
			@SuppressWarnings("unchecked")
			List<RuleAction> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<RuleAction> getByBestMatch(String ruleName, Timestamp startTime, String senderId) {
		if (startTime == null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		// TODO revisit when SenderDataRowId column is populated on RuleAction table
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and c.senderId=:senderId " +
				" and (r.ruleActionPK.senderData=c or r.ruleActionPK.senderData is null)" +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"and r.ruleActionPK.startTime<=:startTime and r.statusId=:statusId " +
				"order by r.ruleActionPK.actionSequence, c.senderId desc, r.ruleActionPK.startTime desc ";
		if (StringUtils.isBlank(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			query.setParameter("senderId", senderId);
			query.setParameter("startTime", startTime);
			query.setParameter("statusId", StatusId.ACTIVE.getValue());
			@SuppressWarnings("unchecked")
			List<RuleAction> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<RuleAction> getAll() {
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.senderData=c " +
				"order by r.ruleActionPK.actionSequence ";
		try {
			Query query = em.createQuery(sql);
			@SuppressWarnings("unchecked")
			List<RuleAction> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public RuleAction getByPrimaryKey(RuleActionPK pk) throws NoResultException {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.senderData=c " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"and (c.senderId=:senderId or r.ruleActionPK.senderData is null) " +
				"and r.ruleActionPK.startTime<=:startTime and r.ruleActionPK.actionSequence=:actionSequence " +
				"order by r.ruleActionPK.actionSequence, c.senderId desc, r.ruleActionPK.startTime desc ";
		String senderId = "";
		if (pk.getSenderData()!=null || pk.getSenderData().getSenderId()!=null) {
			senderId = pk.getSenderData().getSenderId();
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", pk.getRuleLogic().getRuleName());
			query.setParameter("actionSequence", pk.getActionSequence());
			query.setParameter("senderId", senderId);
			query.setParameter("startTime", pk.getStartTime());
			RuleAction action = (RuleAction) query.getSingleResult();
			//em.lock(action, LockModeType.OPTIMISTIC);
			return action;
		}
		finally {
		}
	}

	public RuleAction getMostCurrent(String ruleName, int actionSequence, String senderId) throws NoResultException {
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.senderData=c " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"and (c.senderId=:senderId or r.ruleActionPK.senderData is null) " +
				"and r.ruleActionPK.actionSequence=:actionSequence and r.statusId=:statusId " +
				"order by c.senderId desc, r.ruleActionPK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			query.setParameter("actionSequence", actionSequence);
			query.setParameter("senderId", senderId);
			query.setParameter("statusId", StatusId.ACTIVE.getValue());
			@SuppressWarnings("unchecked")
			List<RuleAction> list = query.getResultList();
			if (list.isEmpty()) {
				throw new NoResultException("Result not found by Rulename ("
						+ ruleName + ") ActionSequence (" + actionSequence
						+ ") SenderId (" + senderId + ").");
			}
			return list.get(0);
		}
		finally {
		}
	}

	public RuleAction getByRowId(int rowId) throws NoResultException {
		String sql =
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.senderData=c " +
				"and r.rowId = :rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			RuleAction action = (RuleAction) query.getSingleResult();
			//em.lock(action, LockModeType.OPTIMISTIC);
			return action;
		}
		finally {
		}
	}
	
	public void delete(RuleAction action) {
		if (action == null) return;
		try {
			em.remove(action);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public int deleteByRuleName(String ruleName) {
		try {
			Query query = em.createNativeQuery("delete from Rule_Action where RuleLogicRowId = " +
					"(select Row_Id from rule_logic rl where rl.ruleName=?1) ");
			query.setParameter(1, ruleName);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(RuleActionPK pk) {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		String sql =
				"delete from Rule_Action where " +
				"actionSequence=?1 and startTime=?2 and RuleLogicRowId = " +
				"(select Row_Id from rule_logic rl where rl.ruleName=?3) ";
		String senderId = "";
		if (pk.getSenderData()!=null && StringUtils.isNotBlank(pk.getSenderData().getSenderId())) {
			sql += "and SenderDataRowId = (select Row_Id from sender_data c where c.senderId=?4)";
		}
		else {
			sql += "and SenderDataRowId is null";
		}
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getActionSequence());
			query.setParameter(2, pk.getStartTime());
			query.setParameter(3, pk.getRuleLogic().getRuleName());
			if (StringUtils.isNotBlank(senderId)) {
				query.setParameter(4, senderId);
			}
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from RuleAction t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleAction action) {
		try {
			em.persist(action);
			reloadFlagsService.updateRuleReloadFlag();
		}
		finally {
		}
	}

	public void update(RuleAction action) {
		try {
			if (em.contains(action)) {
				em.persist(action);
			}
			else {
				em.merge(action);
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
