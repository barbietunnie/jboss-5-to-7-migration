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
import jpa.util.EmailAddrUtil;
import jpa.util.StringUtil;

@Component("emailAddrService")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailAddrService {
	static Logger logger = Logger.getLogger(EmailAddrService.class);
	
	@Autowired
	EntityManager em;

	public EmailAddr getByAddress(String addr) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailAddr t where t.address = :address");
			query.setParameter("address", EmailAddrUtil.removeDisplayName(addr));
			EmailAddr emailAddr = (EmailAddr) query.getSingleResult();
			em.lock(emailAddr, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return emailAddr;
		}
		finally {
		}
	}
	
	/*
	 * return an array with 4 elements:
	 * 1) EmailAddr
	 * 2) through 4) BigDecimal
	 */
	public Object[] getByAddressWithCounts(String addr) throws NoResultException {
		String sql = "select a.*, " +
				" sum(b.SentCount) as sentCount, sum(b.OpenCount) as openCount," +
				" sum(b.ClickCount) as clickCount " +
				"from Email_Addr a " +
				" LEFT OUTER JOIN Subscription b on a.Row_Id = b.EmailAddrRowId " +
				" where a.address = ?1 " +
				"group by " +
				" a.Row_Id, " +
				" a.Address, " +
				" a.statusChangeTime, " +
				" a.statusChangeUserId, " +
				" a.bounceCount, " +
				" a.StatusId, " +
				" a.lastBounceTime, " +
				" a.lastSentTime, " +
				" a.lastRcptTime, " +
				" a.isAcceptHtml, " +
				" a.UpdtUserid, " +
				" a.UpdtTime ";
		try {
			Query query = em.createNativeQuery(sql, EmailAddr.MAPPING_EMAIL_ADDR_WITH_COUNTS);
			query.setParameter(1, EmailAddrUtil.removeDisplayName(addr));
			Object[] emailAddr = (Object[]) query.getSingleResult();
			return emailAddr;
		}
		finally {
		}
	}
	
	public EmailAddr findSertAddress(String addr) {
		return findSertAddress(addr, 0);
	}

	private EmailAddr findSertAddress(String addr, int retries) {
		try {
			EmailAddr emailAddr = null;
			try {
				emailAddr = getByAddress(addr);
			}
			catch (OptimisticLockException e) {
				logger.warn("OptimisticLockException caught, clear EntityManager and try again...");
				em.clear();
				emailAddr = getByAddress(addr);
			}
			return emailAddr;
		}
		catch (NoResultException e) {
			logger.debug("Email Address (" + addr + ") not found, insert...");
			EmailAddr emailAddr = new EmailAddr();
			emailAddr.setAddress(EmailAddrUtil.removeDisplayName(addr));
			emailAddr.setOrigAddress(addr);
			emailAddr.setAcceptHtml(true);
			emailAddr.setStatusId(StatusId.ACTIVE.getValue());
			//emailAddr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
			//emailAddr.setStatusChangeTime(new java.sql.Timestamp(System.currentTimeMillis()));
			emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
			try {
				insert(emailAddr);
				em.flush();
				return getByAddress(addr);
			} catch (DuplicateKeyException dke) {
				logger.error("findByAddress() - DuplicateKeyException caught", dke);
				if (retries < 0) {
					// retry once may overcome concurrency issue. (the retry
					// never worked and the reason might be that it is under
					// a same transaction). So no retry from now on.
					logger.info("findSertEmailAddr - duplicate key error, retry...");
					return findSertAddress(addr, retries + 1);
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

	public List<EmailAddr> getByAddressDomain(String domain) {
		return getByAddressPattern(domain + "$");
	}
	
	public List<EmailAddr> getByAddressUser(String user) {
		return getByAddressPattern("^" + user);
	}
	
	/*
	 * Sample address regex patterns
	 * 1) find by domain name - '@test.com$' or '@yahoo.com'
	 * 2) find by email user name - '^myname@' or 'noreply@'
	 */
	public List<EmailAddr> getByAddressPattern(String addressPattern) {
		String sql = "select t.* from Email_Addr t where address REGEXP '" + addressPattern + "' ";
		try {
			Query query = em.createNativeQuery(sql, EmailAddr.MAPPING_EMAIL_ADDR_ENTITY);
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

	public int deleteByAddress(String addr) {
		try {
			Query query = em.createQuery("delete from EmailAddr t where t.address=:address");
			query.setParameter("address", EmailAddrUtil.removeDisplayName(addr));
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
		if (EmailAddrUtil.hasDisplayName(emailAddr.getAddress())) {
			emailAddr.setAddress(EmailAddrUtil.removeDisplayName(emailAddr.getAddress()));
		}
		try {
			em.persist(emailAddr);
		}
		finally {
		}
	}
	
	public void update(EmailAddr emailAddr) {
		if (EmailAddrUtil.hasDisplayName(emailAddr.getAddress())) {
			emailAddr.setAddress(EmailAddrUtil.removeDisplayName(emailAddr.getAddress()));
		}
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
	
	public void updateBounceCount(EmailAddr emailAddr) {
		emailAddr.setBounceCount(emailAddr.getBounceCount()+1);
		if (emailAddr.getBounceCount() >= Constants.BOUNCE_SUSPEND_THRESHOLD) {
			if (!StatusId.SUSPENDED.getValue().equals(emailAddr.getStatusId())) {
				emailAddr.setStatusId(StatusId.SUSPENDED.getValue());
				if (!StringUtil.isEmpty(emailAddr.getUpdtUserId())) {
					emailAddr.setStatusChangeUserId(emailAddr.getUpdtUserId());
				} else {
					emailAddr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				}
				emailAddr.setStatusChangeTime(emailAddr.getUpdtTime());
			}
		}
		update(emailAddr);
	}
}
