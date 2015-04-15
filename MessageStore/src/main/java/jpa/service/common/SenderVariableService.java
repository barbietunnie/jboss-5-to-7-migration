package jpa.service.common;

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
import jpa.model.SenderVariable;
import jpa.model.SenderVariablePK;

@Component("senderVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class SenderVariableService implements java.io.Serializable {
	private static final long serialVersionUID = -2936730931671540437L;

	static Logger logger = Logger.getLogger(SenderVariableService.class);
	
	@Autowired
	EntityManager em;

	public SenderVariable getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"SenderVariable t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			SenderVariable record = (SenderVariable) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public SenderVariable getByPrimaryKey(SenderVariablePK pk) throws NoResultException {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		String sql = 
			"select t " +
			"from " +
				"SenderVariable t, SenderData c " +
				"where c=t.senderVariablePK.senderData and c.senderId=:senderId and t.senderVariablePK.variableName=:variableName";
		if (pk.getStartTime()!=null) {
			sql += " and t.senderVariablePK.startTime=:starTtime ";
		}
		else {
			sql += " and t.senderVariablePK.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("senderId", pk.getSenderData().getSenderId());
			query.setParameter("variableName", pk.getVariableName());
			if (pk.getStartTime() != null) {
				query.setParameter("starTtime", pk.getStartTime());
			}
			SenderVariable sender = (SenderVariable) query.getSingleResult();
			return sender;
		}
		finally {
		}
	}

	public SenderVariable getByBestMatch(SenderVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Date(System.currentTimeMillis()));
		}
		String sql = 
				"select t " +
				"from " +
					"SenderVariable t, SenderData c " +
					" where c=t.senderVariablePK.senderData and c.senderId=:senderId " +
					" and t.senderVariablePK.variableName=:variableName " +
					" and (t.senderVariablePK.startTime<=:startTime or t.senderVariablePK.startTime is null) " +
					" order by t.senderVariablePK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", pk.getVariableName());
			query.setParameter("startTime", pk.getStartTime());
			query.setParameter("senderId", pk.getSenderData().getSenderId());
			@SuppressWarnings("unchecked")
			List<SenderVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public List<SenderVariable> getByVariableName(String variableName) {
		String sql = 
				"select t " +
				" from " +
					" SenderVariable t, SenderData c " +
					" where c=t.senderVariablePK.senderData and t.senderVariablePK.variableName=:variableName " +
				" order by c.senderId, t.senderVariablePK.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			@SuppressWarnings("unchecked")
			List<SenderVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<SenderVariable> getCurrentBySenderId(String senderId) {
		String sql = 
				"select a.* " +
					" from sender_variable a " +
					" inner join ( " +
					"  select b.senderDataRowId as senderDataRowId, b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from sender_variable b, sender_data cd " +
					"   where b.statusId = ?1 and b.startTime<=?2 and b.senderDataRowId=cd.row_Id and cd.senderId=?3 " +
					"   group by b.senderDataRowId, b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime and a.senderDataRowId=c.senderDataRowId " +
					" order by a.row_id asc ";
		try {
			Query query = em.createNativeQuery(sql, SenderVariable.class);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new Date(System.currentTimeMillis()));
			query.setParameter(3, senderId);
			@SuppressWarnings("unchecked")
			List<SenderVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(SenderVariable var) {
		if (var == null) return;
		try {
			em.remove(var);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(SenderVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from sender_variable " +
				" where variableName=?1 and startTime=?2 " +
				" and senderDataRowId in " +
				" (select row_id from sender_data cd where cd.senderId=?3)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getVariableName());
			query.setParameter(2, pk.getStartTime());
			query.setParameter(3, pk.getSenderData().getSenderId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from SenderVariable t where t.senderVariablePK.variableName=:variableName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteBySenderId(String senderId) {
		String sql = 
				"delete from sender_variable where senderDataRowId in " +
				" (select row_id from sender_data cd where cd.senderId=?1)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, senderId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(SenderVariable var) {
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

	public void insert(SenderVariable var) {
		try {
			em.persist(var);
			em.flush();
		}
		finally {
		}
	}

}
