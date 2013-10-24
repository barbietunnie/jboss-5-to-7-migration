package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageActionLog;
import jpa.model.message.MessageActionLogPK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageActionLogService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageActionLogService implements java.io.Serializable {
	private static final long serialVersionUID = -3216111798576623837L;

	static Logger logger = Logger.getLogger(MessageActionLogService.class);
	
	@Autowired
	EntityManager em;

	public MessageActionLog getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageActionLog t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageActionLog record = (MessageActionLog) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageActionLog getByPrimaryKey(MessageActionLogPK pk) throws NoResultException {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from MessageActionLog t, MessageInbox mi where " +
					" mi=t.messageActionLogPK.messageInbox and mi.rowId=:msgId " +
					" and t.messageActionLogPK.leadMessageRowId=:leadMsgId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", pk.getMessageInbox().getRowId());
				query.setParameter("leadMsgId", pk.getLeadMessageRowId());
				MessageActionLog record = (MessageActionLog) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageActionLog> getByMsgInboxId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageActionLog t, MessageInbox mi where " +
					" mi=t.messageActionLogPK.messageInbox and mi.rowId=:msgId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", msgId);
				@SuppressWarnings("unchecked")
				List<MessageActionLog> list = query.getResultList();
				return list;
			}
			finally {
			}
	}

	public List<MessageActionLog> getByLeadMsgId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageActionLog t where " +
					" t.messageActionLogPK.leadMessageRowId=:msgId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", msgId);
				@SuppressWarnings("unchecked")
				List<MessageActionLog> list = query.getResultList();
				return list;
			}
			finally {
			}
	}

	public void delete(MessageActionLog actionLog) {
		if (actionLog == null) return;
		try {
			em.remove(actionLog);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageActionLog t " +
				" where t.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(MessageActionLogPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Message_Action_Log where " +
				" MessageInboxRowId=?1 and LeadMessageRowId=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageInbox().getRowId());
			query.setParameter(2, pk.getLeadMessageRowId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageActionLog t " +
				" where t.messageActionLogPK.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByLeadMsgId(int msgId) {
		String sql = 
				"delete from MessageActionLog t " +
				" where t.messageActionLogPK.leadMessageRowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageActionLog actionLog) {
		try {
			if (em.contains(actionLog)) {
				em.persist(actionLog);
			}
			else {
				em.merge(actionLog);
			}
		}
		finally {
		}
	}

	public void insert(MessageActionLog actionLog) {
		try {
			em.persist(actionLog);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
