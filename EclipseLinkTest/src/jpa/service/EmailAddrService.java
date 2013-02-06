package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.constant.StatusId;
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
	
	public EmailAddr findSertEmailAddr(String addr) {
		return findSertEmailAddr(addr, 0);
	}

	private EmailAddr findSertEmailAddr(String addr, int retries) {
		try {
			EmailAddr emailAddr = null;
			try {
				emailAddr = getByEmailAddr(addr);
			}
			catch (OptimisticLockException e) {
				logger.warn("OptimisticLockException caught, clear EntityManager and try again...");
				em.clear();
				emailAddr = getByEmailAddr(addr);
			}
			return emailAddr;
		}
		catch (NoResultException e) {
			logger.debug("Email Address (" + addr + ") not found, insert...");
			EmailAddr emailAddr = new EmailAddr();
			emailAddr.setEmailAddr(addr);
			emailAddr.setEmailOrigAddr(addr);
			emailAddr.setAcceptHtml(true);
			emailAddr.setStatusId(StatusId.ACTIVE.getValue());
			emailAddr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
			emailAddr.setStatusChangeTime(new java.sql.Timestamp(System.currentTimeMillis()));
			emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
			try {
				insert(emailAddr);
				em.flush();
				return getByEmailAddr(addr);
			} catch (DuplicateKeyException dke) {
				logger.error("findByAddress() - DuplicateKeyException caught", dke);
				if (retries < 0) {
					// retry once may overcome concurrency issue. (the retry
					// never worked and the reason might be that it is under
					// a same transaction). So no retry from now on.
					logger.info("findSertEmailAddr - duplicate key error, retry...");
					return findSertEmailAddr(addr, retries + 1);
				} else {
					throw e;
				}
			}
		}
		finally {}
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
