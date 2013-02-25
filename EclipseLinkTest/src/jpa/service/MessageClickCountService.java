package jpa.service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.MessageClickCount;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageClickCountService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageClickCountService {
	static Logger logger = Logger.getLogger(MessageClickCountService.class);
	
	@Autowired
	EntityManager em;

	public MessageClickCount getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageClickCount t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageClickCount record = (MessageClickCount) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageClickCount getByMsgInboxId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageClickCount t, MessageInbox mi " +
				" where mi=t.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			MessageClickCount record = (MessageClickCount) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public void delete(MessageClickCount clickCount) {
		if (clickCount == null) return;
		try {
			em.remove(clickCount);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageClickCount t " +
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
				"delete from MessageClickCount t " +
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

	public void update(MessageClickCount clickCount) {
		try {
			if (em.contains(clickCount)) {
				em.persist(clickCount);
			}
			else {
				em.merge(clickCount);
			}
		}
		finally {
		}
	}

	public void insert(MessageClickCount clickCount) {
		try {
			em.persist(clickCount);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
