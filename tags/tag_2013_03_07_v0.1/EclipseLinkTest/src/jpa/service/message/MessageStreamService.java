package jpa.service.message;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageStreamService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageStreamService {
	static Logger logger = Logger.getLogger(MessageStreamService.class);
	
	@Autowired
	EntityManager em;

	public MessageStream getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageStream t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageStream record = (MessageStream) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageStream getByFromAddress(String address) throws NoResultException {
		String sql = 
			"select t " +
			"from MessageStream t, EmailAddress ea where " +
			" ea.rowId=t.fromAddrRowId and ea.address=:address";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("address", address);
			MessageStream record = (MessageStream) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageStream getByMsgInboxId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from " +
					"MessageStream t where t.messageInbox.rowId=:msgId";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", msgId);
				MessageStream record = (MessageStream) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public MessageStream getLastRecord() throws NoResultException {
		String sql = 
				"select t.* " +
				"from " +
					"Message_Stream t " +
				" where t.Row_Id = (select max(t2.Row_Id) from Message_Stream t2) ";
		try {
			Query query = em.createNativeQuery(sql, MessageStream.MAPPING_MESSAGE_STREAM);
			MessageStream record = (MessageStream) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public void delete(MessageStream stream) {
		if (stream == null) return;
		try {
			em.remove(stream);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageStream t " +
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
				"delete from MessageStream t " +
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

	public void update(MessageStream stream) {
		try {
			if (em.contains(stream)) {
				em.persist(stream);
			}
			else {
				em.merge(stream);
			}
		}
		finally {
		}
	}

	public void insert(MessageStream stream) {
		try {
			em.persist(stream);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
