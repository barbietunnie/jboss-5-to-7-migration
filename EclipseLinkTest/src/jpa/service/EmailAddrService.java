package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.EmailAddr;

@Component("emailAddrService")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailAddrService {
	static Logger logger = Logger.getLogger(EmailAddrService.class);
	
	@Autowired
	EntityManager em;

	public EmailAddr getByEmailAddr(String addr) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailAddr t where t.emailAddr = :emailAddr");
			query.setParameter("emailAddr", addr);
			EmailAddr emailAddr = (EmailAddr) query.getSingleResult();
			em.lock(emailAddr, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return emailAddr;
		}
		finally {
		}
	}
	
	public EmailAddr getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailAddr t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			EmailAddr emailAddr = (EmailAddr) query.getSingleResult();
			em.lock(emailAddr, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return emailAddr;
		}
		finally {
		}
	}
	
	public List<EmailAddr> getAll() {
		try {
			Query query = em.createQuery("select t from EmailAddr t");
			@SuppressWarnings("unchecked")
			List<EmailAddr> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(EmailAddr emailAddr) {
		if (emailAddr==null) return;
		try {
			em.remove(emailAddr);
		}
		finally {
		}
	}

	public int deleteByEmailAddr(String addr) {
		try {
			Query query = em.createQuery("delete from EmailAddr t where t.emailAddr=:emailAddr");
			query.setParameter("emailAddr", addr);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from EmailAddr t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(EmailAddr emailAddr) {
		try {
			em.persist(emailAddr);
		}
		finally {
		}
	}
	
	public void update(EmailAddr emailAddr) {
		try {
			if (em.contains(emailAddr)) {
				em.persist(emailAddr);
			}
			else {
				em.merge(emailAddr);
			}
		}
		finally {
		}
	}
	
}
