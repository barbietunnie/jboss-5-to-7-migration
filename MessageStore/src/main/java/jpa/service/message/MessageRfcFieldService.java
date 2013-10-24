package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageRfcField;
import jpa.model.message.MessageRfcFieldPK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageRfcFieldService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageRfcFieldService implements java.io.Serializable {
	private static final long serialVersionUID = 4201242257880602396L;

	static Logger logger = Logger.getLogger(MessageRfcFieldService.class);
	
	@Autowired
	EntityManager em;

	public MessageRfcField getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageRfcField t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageRfcField record = (MessageRfcField) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageRfcField getByPrimaryKey(MessageRfcFieldPK pk) throws NoResultException {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from MessageRfcField t, MessageInbox mi where " +
					" mi=t.messageRfcFieldPK.messageInbox and mi.rowId=:msgId " +
					" and t.messageRfcFieldPK.rfcType=:rfcType ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", pk.getMessageInbox().getRowId());
				query.setParameter("rfcType", pk.getRfcType());
				MessageRfcField record = (MessageRfcField) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageRfcField> getByMsgInboxId(int msgId) {
		String sql = 
				"select t " +
				"from MessageRfcField t, MessageInbox mi " +
				" where mi=t.messageRfcFieldPK.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			@SuppressWarnings("unchecked")
			List<MessageRfcField> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(MessageRfcField rfcField) {
		if (rfcField == null) return;
		try {
			em.remove(rfcField);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageRfcField t " +
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

	public int deleteByPrimaryKey(MessageRfcFieldPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Message_RfcField where " +
				" MessageInboxRowId=?1 and rfcType=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageInbox().getRowId());
			query.setParameter(2, pk.getRfcType());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageRfcField t " +
				" where t.messageRfcFieldPK.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageRfcField rfcField) {
		try {
			if (em.contains(rfcField)) {
				em.persist(rfcField);
			}
			else {
				em.merge(rfcField);
			}
		}
		finally {
		}
	}

	public void insert(MessageRfcField rfcField) {
		try {
			em.persist(rfcField);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
