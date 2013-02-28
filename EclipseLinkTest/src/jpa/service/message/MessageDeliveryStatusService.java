package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageDeliveryStatus;
import jpa.model.message.MessageDeliveryStatusPK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageDeliveryStatusService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageDeliveryStatusService {
	static Logger logger = Logger.getLogger(MessageDeliveryStatusService.class);
	
	@Autowired
	EntityManager em;

	public MessageDeliveryStatus getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageDeliveryStatus t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageDeliveryStatus record = (MessageDeliveryStatus) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageDeliveryStatus getByPrimaryKey(MessageDeliveryStatusPK pk) throws NoResultException {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from MessageDeliveryStatus t, MessageInbox mi where " +
					" mi=t.messageDeliveryStatusPK.messageInbox and mi.rowId=:msgId " +
					" and t.messageDeliveryStatusPK.finalRcptAddrRowId=:finalRcptRowId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", pk.getMessageInbox().getRowId());
				query.setParameter("finalRcptRowId", pk.getFinalRcptAddrRowId());
				MessageDeliveryStatus record = (MessageDeliveryStatus) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageDeliveryStatus> getByMsgInboxId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageDeliveryStatus t, MessageInbox mi where " +
					" mi=t.messageDeliveryStatusPK.messageInbox and mi.rowId=:msgId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", msgId);
				@SuppressWarnings("unchecked")
				List<MessageDeliveryStatus> list = query.getResultList();
				return list;
			}
			finally {
			}
	}

	public void delete(MessageDeliveryStatus dlvrStatus) {
		if (dlvrStatus == null) return;
		try {
			em.remove(dlvrStatus);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageDeliveryStatus t " +
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

	public int deleteByPrimaryKey(MessageDeliveryStatusPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Message_Delivery_Status where " +
				" MessageInboxRowId=?1 and FinalRcptAddrRowId=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageInbox().getRowId());
			query.setParameter(2, pk.getFinalRcptAddrRowId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageDeliveryStatus t " +
				" where t.messageDeliveryStatusPK.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageDeliveryStatus dlvrStatus) {
		try {
			if (em.contains(dlvrStatus)) {
				em.persist(dlvrStatus);
			}
			else {
				em.merge(dlvrStatus);
			}
		}
		finally {
		}
	}

	public void insert(MessageDeliveryStatus dlvrStatus) {
		try {
			em.persist(dlvrStatus);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

	public void insertWithDelete(MessageDeliveryStatus dlvrStatus) {
		try {
			delete(dlvrStatus);
			insert(dlvrStatus);
		}
		finally {
		}
	}
}
