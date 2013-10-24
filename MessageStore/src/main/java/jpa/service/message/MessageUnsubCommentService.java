package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageUnsubComment;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageUnsubCommentService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageUnsubCommentService implements java.io.Serializable {
	private static final long serialVersionUID = -4830933844969528333L;

	static Logger logger = Logger.getLogger(MessageUnsubCommentService.class);
	
	@Autowired
	EntityManager em;

	public MessageUnsubComment getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageUnsubComment t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageUnsubComment record = (MessageUnsubComment) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageUnsubComment getByMsgInboxId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageUnsubComment t, MessageInbox mi " +
				" where mi=t.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			MessageUnsubComment record = (MessageUnsubComment) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public List<MessageUnsubComment> getByFromAddress(String address) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageUnsubComment t, EmailAddress ea " +
				" where ea.rowId=t.emailAddrRowId and ea.address=:address ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<MessageUnsubComment> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(MessageUnsubComment unsubComment) {
		if (unsubComment == null) return;
		try {
			em.remove(unsubComment);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageUnsubComment t " +
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

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageUnsubComment t " +
				" where t.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageUnsubComment unsubComment) {
		try {
			if (em.contains(unsubComment)) {
				em.persist(unsubComment);
			}
			else {
				em.merge(unsubComment);
			}
		}
		finally {
		}
	}

	public void insert(MessageUnsubComment unsubComment) {
		try {
			em.persist(unsubComment);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
