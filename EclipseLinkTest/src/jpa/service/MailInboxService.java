package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.MailInbox;
import jpa.model.MailInboxPK;

@Component("mailInboxService")
@Transactional(propagation=Propagation.REQUIRED)
public class MailInboxService {
	static Logger logger = Logger.getLogger(MailInboxService.class);
	
	@Autowired
	EntityManager em;
	
	public MailInbox getByPrimaryKey(MailInboxPK pk) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MailInbox t " +
					" where t.mailInboxPK.userId=:userId and t.mailInboxPK.hostName=:hostName");
			query.setParameter("userId", pk.getUserId());
			query.setParameter("hostName", pk.getHostName());
			MailInbox inbox = (MailInbox) query.getSingleResult();
			return inbox;
		}
		finally {
		}
	}
	
	public MailInbox getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MailInbox t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			MailInbox inbox = (MailInbox) query.getSingleResult();
			return inbox;
		}
		finally {
		}
	}
	
	public List<MailInbox> getAll() {
		try {
			Query query = em.createQuery("select t from MailInbox t");
			@SuppressWarnings("unchecked")
			List<MailInbox> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(MailInbox inbox) {
		if (inbox == null) return;
		try {
			em.remove(inbox);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(MailInboxPK pk) {
		try {
			Query query = em.createQuery("delete from MailInbox t where " +
					" t.mailInboxPK.userId=:userId and t.mailInboxPK.hostName=:hostName");
			query.setParameter("userId", pk.getUserId());
			query.setParameter("hostName", pk.getHostName());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from MailInbox t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MailInbox inbox) {
		try {
			em.persist(inbox);
			em.flush();
		}
		finally {
		}
	}

	public void update(MailInbox inbox) {
		try {
			if (em.contains(inbox)) {
				em.persist(inbox);
			}
			else {
				em.merge(inbox);
			}
		}
		finally {
		}
	}
}
