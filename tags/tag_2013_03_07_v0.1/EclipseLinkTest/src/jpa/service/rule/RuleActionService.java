package jpa.service.rule;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionPK;
import jpa.service.ReloadFlagsService;

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
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, ClientData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.clientData=c " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"order by r.ruleActionPK.actionSequence, c.clientId, r.ruleActionPK.startTime ";
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

	public List<RuleAction> getByBestMatch(String ruleName, Timestamp startTime, String clientId) {
		if (startTime == null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, ClientData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.clientData=c " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"and (c.clientId=:clientId or r.ruleActionPK.clientData is null) " +
				"and r.ruleActionPK.startTime<=:startTime and r.statusId=:statusId " +
				"order by r.ruleActionPK.actionSequence, c.clientId desc, r.ruleActionPK.startTime desc ";
		if (clientId==null) {
			clientId = "";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			query.setParameter("clientId", clientId);
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
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, ClientData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.clientData=c " +
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
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, ClientData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.clientData=c " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"and (c.clientId=:clientId or r.ruleActionPK.clientData is null) " +
				"and r.ruleActionPK.startTime<=:startTime and r.ruleActionPK.actionSequence=:actionSequence " +
				"order by r.ruleActionPK.actionSequence, c.clientId desc, r.ruleActionPK.startTime desc ";
		String clientId = "";
		if (pk.getClientData()!=null || pk.getClientData().getClientId()!=null) {
			clientId = pk.getClientData().getClientId();
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", pk.getRuleLogic().getRuleName());
			query.setParameter("actionSequence", pk.getActionSequence());
			query.setParameter("clientId", clientId);
			query.setParameter("startTime", pk.getStartTime());
			RuleAction action = (RuleAction) query.getSingleResult();
			em.lock(action, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return action;
		}
		finally {
		}
	}

	public RuleAction getMostCurrent(String ruleName, int actionSequence, String clientId) throws NoResultException {
		String sql = 
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, ClientData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.clientData=c " +
				"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
				"and (c.clientId=:clientId or r.ruleActionPK.clientData is null) " +
				"and r.ruleActionPK.actionSequence=:actionSequence and r.statusId=:statusId " +
				"order by c.clientId desc, r.ruleActionPK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("ruleName", ruleName);
			query.setParameter("actionSequence", actionSequence);
			query.setParameter("clientId", clientId);
			query.setParameter("statusId", StatusId.ACTIVE.getValue());
			@SuppressWarnings("unchecked")
			List<RuleAction> list = query.getResultList();
			if (list.isEmpty()) {
				throw new NoResultException("Result not found by Rulename ("
						+ ruleName + ") ActionSequence (" + actionSequence
						+ ") ClientId (" + clientId + ").");
			}
			return list.get(0);
		}
		finally {
		}
	}

	public RuleAction getByRowId(int rowId) throws NoResultException {
		String sql =
				"select r from RuleAction r, RuleLogic rl, RuleActionDetail al, ClientData c " +
				"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.clientData=c " +
				"and r.rowId = :rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			RuleAction action = (RuleAction) query.getSingleResult();
			em.lock(action, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
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
		String clientId = "";
		if (pk.getClientData()!=null && StringUtils.isNotBlank(pk.getClientData().getClientId())) {
			sql += "and ClientDataRowId = (select Row_Id from client_data c where c.clientId=?4)";
		}
		else {
			sql += "and ClientDataRowId is null";
		}
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getActionSequence());
			query.setParameter(2, pk.getStartTime());
			query.setParameter(3, pk.getRuleLogic().getRuleName());
			if (StringUtils.isNotBlank(clientId)) {
				query.setParameter(4, clientId);
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
