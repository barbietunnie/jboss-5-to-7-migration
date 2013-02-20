package jpa.service;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.TemplateVariable;
import jpa.model.TemplateVariablePK;

@Component("templateVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class TemplateVariableService {
	static Logger logger = Logger.getLogger(TemplateVariableService.class);
	
	@Autowired
	EntityManager em;

	public TemplateVariable getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"TemplateVariable t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			TemplateVariable record = (TemplateVariable) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public TemplateVariable getByPrimaryKey(TemplateVariablePK pk) throws NoResultException {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		String sql = 
			"select t " +
			"from " +
				"TemplateVariable t, ClientData c " +
				"where c=t.templateVariablePK.clientData and c.clientId=:clientId " +
				"and t.templateVariablePK.variableId=:variableId " +
				"and t.templateVariablePK.variableName=:variableName ";
		if (pk.getStartTime()!=null) {
			sql += " and t.templateVariablePK.startTime=:starTtime ";
		}
		else {
			sql += " and t.templateVariablePK.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("clientId", pk.getClientData().getClientId());
			query.setParameter("variableId", pk.getVariableId());
			query.setParameter("variableName", pk.getVariableName());
			if (pk.getStartTime() != null) {
				query.setParameter("starTtime", pk.getStartTime());
			}
			TemplateVariable template = (TemplateVariable) query.getSingleResult();
			return template;
		}
		finally {
		}
	}

	public TemplateVariable getByBestMatch(TemplateVariablePK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Timestamp(System.currentTimeMillis()));
		}
		String sql = 
				"select t " +
				"from " +
					"TemplateVariable t,ClientData c " +
					" where c=t.templateVariablePK.clientData and c.clientId=:clientId " +
					" and t.templateVariablePK.variableId=:variableId " +
					" and t.templateVariablePK.variableName=:variableName " +
					" and (t.templateVariablePK.startTime<=:startTime or t.templateVariablePK.startTime is null) " +
					" order by t.templateVariablePK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableId", pk.getVariableId());
			query.setParameter("startTime", pk.getStartTime());
			query.setParameter("clientId", pk.getClientData().getClientId());
			query.setParameter("variableName", pk.getVariableName());
			@SuppressWarnings("unchecked")
			List<TemplateVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public List<TemplateVariable> getByVariableId(String variableId) {
		String sql = 
				"select t " +
				" from " +
					" TemplateVariable t, ClientData c " +
					" where c=t.templateVariablePK.clientData " +
					" and t.templateVariablePK.variableId=:variableId " +
				" order by c.clientId, t.templateVariablePK.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableId", variableId);
			@SuppressWarnings("unchecked")
			List<TemplateVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<TemplateVariable> getCurrentByVariableId(String variableId) {
		String sql = 
				"select a.* " +
					" from template_variable a " +
					" inner join ( " +
					"  select b.variableId as variableId, b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from template_variable b " +
					"   where b.statusId = ?1 and b.startTime<=?2 and b.variableId=?3 " +
					"   group by b.variableId, b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime and a.variableId=c.variableId " +
					" order by a.row_id asc ";
		try {
			Query query = em.createNativeQuery(sql, TemplateVariable.class);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new Timestamp(System.currentTimeMillis()));
			query.setParameter(3, variableId);
			@SuppressWarnings("unchecked")
			List<TemplateVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<TemplateVariable> getCurrentByClientId(String clientId) {
		String sql = 
				"select a.* " +
					" from template_variable a " +
					" inner join ( " +
					"  select b.clientDataRowId as clientDataRowId, b.variableId as variableId, b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from template_variable b, Client_Data cd " +
					"   where b.statusId = ?1 and b.startTime<=?2 and b.clientDataRowId=cd.row_Id and cd.clientId=?3 " +
					"   group by b.clientDataRowId, b.variableId, b.variableName " +
					" ) as c " +
					"  on a.variableId=c.variableId and a.variableName=c.variableName and a.startTime=c.maxTime and a.clientDataRowId=c.clientDataRowId " +
					" order by a.row_id asc ";
		try {
			Query query = em.createNativeQuery(sql, TemplateVariable.class);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new Timestamp(System.currentTimeMillis()));
			query.setParameter(3, clientId);
			@SuppressWarnings("unchecked")
			List<TemplateVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(TemplateVariable template) {
		if (template == null) return;
		try {
			em.remove(template);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(TemplateVariablePK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Template_Variable " +
				" where variableId=?1 and variableName=?2 and startTime=?3 " +
				" and clientDataRowId in " +
				" (select row_id from client_data cd where cd.clientId=?4) ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getVariableId());
			query.setParameter(2, pk.getVariableName());
			query.setParameter(3, pk.getStartTime());
			query.setParameter(4, pk.getClientData().getClientId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableId(String variableId) {
		String sql = 
				"delete from TemplateVariable t where t.templateVariablePK.variableId=:variableId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableId", variableId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from TemplateVariable t where t.templateVariablePK.variableName=:variableName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByClientId(String clientId) {
		String sql = 
				"delete from Template_Variable where clientDataRowId in " +
				" (select row_id from client_data cd where cd.clientId=?1) ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(TemplateVariable variable) {
		try {
			if (em.contains(variable)) {
				em.persist(variable);
			}
			else {
				em.merge(variable);
			}
		}
		finally {
		}
	}

	public void insert(TemplateVariable variable) {
		try {
			em.persist(variable);
			em.flush();
		}
		finally {
		}
	}

}
