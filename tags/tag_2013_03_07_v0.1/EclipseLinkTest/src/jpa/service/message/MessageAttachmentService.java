package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageAttachment;
import jpa.model.message.MessageAttachmentPK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageAttachmentService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageAttachmentService {
	static Logger logger = Logger.getLogger(MessageAttachmentService.class);
	
	@Autowired
	EntityManager em;

	public MessageAttachment getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageAttachment t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageAttachment record = (MessageAttachment) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageAttachment getByPrimaryKey(MessageAttachmentPK pk) throws NoResultException {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from MessageAttachment t, MessageInbox mi where " +
					" mi=t.messageAttachmentPK.messageInbox and mi.rowId=:msgId " +
					" and t.messageAttachmentPK.attachmentDepth=:depth " +
					" and t.messageAttachmentPK.attachmentSequence=:sequence ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", pk.getMessageInbox().getRowId());
				query.setParameter("depth", pk.getAttachmentDepth());
				query.setParameter("sequence", pk.getAttachmentSequence());
				MessageAttachment record = (MessageAttachment) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageAttachment> getByMsgInboxId(int msgId) {
		String sql = 
				"select t " +
				"from MessageAttachment t, MessageInbox mi " +
				" where mi=t.messageAttachmentPK.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			@SuppressWarnings("unchecked")
			List<MessageAttachment> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(MessageAttachment msgAttch) {
		if (msgAttch == null) return;
		try {
			em.remove(msgAttch);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageAttachment t " +
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

	public int deleteByPrimaryKey(MessageAttachmentPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Message_Attachment where " +
				" MessageInboxRowId=?1 and attachmentDepth=?2 and attachmentSequence=?3 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageInbox().getRowId());
			query.setParameter(2, pk.getAttachmentDepth());
			query.setParameter(3, pk.getAttachmentSequence());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageAttachment t " +
				" where t.messageAttachmentPK.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageAttachment msgAttch) {
		try {
			if (em.contains(msgAttch)) {
				em.persist(msgAttch);
			}
			else {
				em.merge(msgAttch);
			}
		}
		finally {
		}
	}

	public void insert(MessageAttachment msgAttch) {
		try {
			em.persist(msgAttch);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
