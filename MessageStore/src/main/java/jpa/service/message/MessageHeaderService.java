package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageHeader;
import jpa.model.message.MessageHeaderPK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageHeaderService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageHeaderService {
	static Logger logger = Logger.getLogger(MessageHeaderService.class);
	
	@Autowired
	EntityManager em;

	public MessageHeader getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageHeader t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageHeader record = (MessageHeader) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageHeader getByPrimaryKey(MessageHeaderPK pk) throws NoResultException {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from MessageHeader t, MessageInbox mi where " +
					" mi=t.messageHeaderPK.messageInbox and mi.rowId=:msgId " +
					" and t.messageHeaderPK.headerSequence=:sequence ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", pk.getMessageInbox().getRowId());
				query.setParameter("sequence", pk.getHeaderSequence());
				MessageHeader record = (MessageHeader) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageHeader> getByMsgInboxId(int msgId) {
		String sql = 
				"select t " +
				"from MessageHeader t, MessageInbox mi " +
				" where mi=t.messageHeaderPK.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			@SuppressWarnings("unchecked")
			List<MessageHeader> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(MessageHeader msgHeader) {
		if (msgHeader == null) return;
		try {
			em.remove(msgHeader);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageHeader t " +
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

	public int deleteByPrimaryKey(MessageHeaderPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Message_Header where " +
				" MessageInboxRowId=?1 and headerSequence=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageInbox().getRowId());
			query.setParameter(2, pk.getHeaderSequence());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageHeader t " +
				" where t.messageHeaderPK.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageHeader msgHeader) {
		try {
			if (em.contains(msgHeader)) {
				em.persist(msgHeader);
			}
			else {
				em.merge(msgHeader);
			}
		}
		finally {
		}
	}

	public void insert(MessageHeader msgHeader) {
		try {
			em.persist(msgHeader);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
