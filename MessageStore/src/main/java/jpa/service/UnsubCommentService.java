package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.UnsubComment;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("unsubCommentService")
@Transactional(propagation=Propagation.REQUIRED)
public class UnsubCommentService implements java.io.Serializable {
	private static final long serialVersionUID = -6069568690334155415L;

	static Logger logger = Logger.getLogger(UnsubCommentService.class);
	
	@Autowired
	EntityManager em;

	public List<UnsubComment> getByAddress(String address) {
		try {
			Query query = em.createQuery("select t from UnsubComment t, EmailAddress e " +
					"where e=t.emailAddr and e.address=:address ");
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<UnsubComment> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public UnsubComment getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from UnsubComment t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			UnsubComment comment = (UnsubComment) query.getSingleResult();
			//em.lock(comment, LockModeType.OPTIMISTIC);
			return comment;
		}
		finally {
		}
	}
	
	public List<UnsubComment> getByMailingListId(String listId) {
		try {
			Query query = em.createQuery("select t from UnsubComment t, MailingList ml " +
					"where t.mailingList=ml and ml.listId=:listId ");
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<UnsubComment> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(UnsubComment comment) {
		if (comment==null) return;
		try {
			em.remove(comment);
		}
		finally {
		}
	}

	public int deleteByAddress(String address) {
		try {
			Query query = em.createNativeQuery("delete from Unsub_Comment where emailAddrRowId in " +
					"(select Row_id from email_address e where e.address = ?1) ");
			query.setParameter(1, address);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from UnsubComment t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(UnsubComment comment) {
		try {
			em.persist(comment);
		}
		finally {
		}
	}
	
	public void update(UnsubComment comment) {
		try {
			if (em.contains(comment)) {
				em.persist(comment);
			}
			else {
				em.merge(comment);
			}
		}
		finally {
		}
	}
	
}
