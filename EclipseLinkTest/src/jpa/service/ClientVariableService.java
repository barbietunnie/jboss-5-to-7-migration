package jpa.service;

import java.util.Date;
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
import jpa.model.ClientVariable;
import jpa.model.ClientVariablePK;

@Component("clientVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class ClientVariableService {
	static Logger logger = Logger.getLogger(ClientVariableService.class);
	
	@Autowired
	EntityManager em;

	public ClientVariable getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"ClientVariable t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			ClientVariable record = (ClientVariable) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public ClientVariable getByPrimaryKey(ClientVariablePK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		String sql = 
			"select t " +
			"from " +
				"ClientVariable t, ClientData c " +
				"where c=t.clientVariablePK.clientData and c.clientId=:clientId and t.clientVariablePK.variableName=:variableName";
		if (pk.getStartTime()!=null) {
			sql += " and t.clientVariablePK.startTime=:starTtime ";
		}
		else {
			sql += " and t.clientVariablePK.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("clientId", pk.getClientData().getClientId());
			query.setParameter("variableName", pk.getVariableName());
			if (pk.getStartTime() != null) {
				query.setParameter("starTtime", pk.getStartTime());
			}
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public ClientVariable getByBestMatch(ClientVariablePK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Date(System.currentTimeMillis()));
		}
		String sql = 
				"select t " +
				"from " +
					"ClientVariable t, ClientData c " +
					" where c=t.clientVariablePK.clientData and c.clientId=:clientId and t.clientVariablePK.variableName=:variableName " +
					" and (t.clientVariablePK.startTime<=:startTime or t.clientVariablePK.startTime is null) " +
					" order by t.clientVariablePK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", pk.getVariableName());
			query.setParameter("startTime", pk.getStartTime());
			query.setParameter("clientId", pk.getClientData().getClientId());
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public List<ClientVariable> getByVariableName(String variableName) {
		String sql = 
				"select t " +
				" from " +
					" ClientVariable t, ClientData c " +
					" where c=t.clientVariablePK.clientData and t.clientVariablePK.variableName=:variableName " +
				" order by c.clientId, t.clientVariablePK.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<ClientVariable> getCurrentByClientId(String clientId) {
		String sql = 
				"select a.* " +
					" from Client_Variable a " +
					" inner join ( " +
					"  select b.clientDataRowId, b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from Client_Variable b, Client_Data cd " +
					"   where b.statusId = ? and b.startTime<=? and b.clientDataRowId=cd.row_Id and cd.clientId=? " +
					"   group by b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime and a.clientDataRowId=c.clientDataRowId " +
					" order by a.row_id asc ";
		try {
			Query query = em.createNativeQuery(sql, ClientVariable.class);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new Date(System.currentTimeMillis()));
			query.setParameter(3, clientId);
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(ClientVariable var) {
		if (var == null) return;
		try {
			em.remove(var);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(ClientVariablePK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Client_Variable " +
				" where variableName=?1 and startTime=?2 " +
				" and clientDataRowId in " +
				" (select row_id from client_data cd where cd.clientId=?3)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getVariableName());
			query.setParameter(2, pk.getStartTime());
			query.setParameter(3, pk.getClientData().getClientId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from ClientVariable t where t.clientVariablePK.variableName=:variableName ";
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
				"delete from Client_Variable where clientDataRowId in " +
				" (select row_id from client_data cd where cd.clientId=?1)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(ClientVariable var) {
		try {
			if (em.contains(var)) {
				em.persist(var);
			}
			else {
				em.merge(var);
			}
		}
		finally {
		}
	}

	public void insert(ClientVariable var) {
		try {
			em.persist(var);
			em.flush();
		}
		finally {
		}
	}

}
