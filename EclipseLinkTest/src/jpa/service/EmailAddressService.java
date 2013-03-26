package jpa.service;

import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.util.EmailAddrUtil;
import jpa.util.JpaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailAddressService")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailAddressService {
	static Logger logger = Logger.getLogger(EmailAddressService.class);
	
	@Autowired
	EntityManager em;

	public EmailAddress getByAddress(String addr) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailAddress t where t.address = :address");
			query.setParameter("address", EmailAddrUtil.removeDisplayName(addr));
			EmailAddress emailAddr = (EmailAddress) query.getSingleResult();
			//em.lock(emailAddr, LockModeType.OPTIMISTIC);
			return emailAddr;
		}
		finally {
		}
	}
	
	/*
	 * return an array with 4 elements:
	 * 1) EmailAddress
	 * 2) through 4) BigDecimal
	 */
	public Object[] getByAddressWithCounts(String addr) throws NoResultException {
		String sql = "select a.*, " +
				" sum(b.SentCount) as sentCount, sum(b.OpenCount) as openCount," +
				" sum(b.ClickCount) as clickCount " +
				"from Email_Address a " +
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
				" a.origAddress, " +
				" a.UpdtUserid, " +
				" a.UpdtTime ";
		try {
			Query query = em.createNativeQuery(sql, EmailAddress.MAPPING_EMAIL_ADDR_WITH_COUNTS);
			query.setParameter(1, EmailAddrUtil.removeDisplayName(addr));
			Object[] emailAddr = (Object[]) query.getSingleResult();
			return emailAddr;
		}
		finally {
		}
	}
	
	public EmailAddress findSertAddress(String addr) {
		return findSertAddress(addr, 0);
	}

	private EmailAddress findSertAddress(String addr, int retries) {
		try {
			EmailAddress emailAddr = null;
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
			EmailAddress emailAddr = new EmailAddress();
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

	public EmailAddress getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailAddress t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			EmailAddress emailAddr = (EmailAddress) query.getSingleResult();
			//em.lock(emailAddr, LockModeType.OPTIMISTIC);
			return emailAddr;
		}
		finally {
		}
	}

	public List<EmailAddress> getByAddressDomain(String domain) {
		return getByAddressPattern(domain + "$");
	}
	
	public List<EmailAddress> getByAddressUser(String user) {
		return getByAddressPattern("^" + user);
	}
	
	/*
	 * Sample address regex patterns
	 * 1) find by domain name - '@test.com$' or '@yahoo.com'
	 * 2) find by email user name - '^myname@' or 'noreply@'
	 */
	public List<EmailAddress> getByAddressPattern(String addressPattern) {
		String sql = "select t.* from Email_Address t where t.address REGEXP '" + addressPattern + "' ";
		if (Constants.DB_PRODNAME_PSQL.equalsIgnoreCase(JpaUtil.getDBProductName())) {
			sql = "select t.* from Email_Address t where t.address ~ '" + addressPattern + "' ";
		}
		else if (Constants.DB_PRODNAME_DERBY.equalsIgnoreCase(JpaUtil.getDBProductName())) {
			String pattern = StringUtils.remove(addressPattern, "^");
			pattern = StringUtils.remove(pattern, "$");
			pattern = StringUtils.removeStart(pattern, "(");
			pattern = StringUtils.removeEnd(pattern, ")");
			sql = "select t.* from Email_Address t " ; //where address like '" + pattern + "' ";
			String whereClause = "";
			StringTokenizer st = new StringTokenizer(pattern, "|");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (addressPattern.startsWith("^")) {
					token = token + "%";
				}
				else if (addressPattern.endsWith("$")) {
					token = "%" + token;
				}
				if (StringUtils.isBlank(whereClause)) {
					whereClause = " where (t.address like '" + token + "') ";
				}
				else {
					whereClause += " or (t.address like '" + token + "') ";
				}
			}
			sql += whereClause;
		}
		try {
			Query query = em.createNativeQuery(sql, EmailAddress.MAPPING_EMAIL_ADDR_ENTITY);
			@SuppressWarnings("unchecked")
			List<EmailAddress> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(EmailAddress emailAddr) {
		if (emailAddr==null) return;
		try {
			em.remove(emailAddr);
			em.flush();
		}
		finally {
		}
	}

	public int deleteByAddress(String addr) {
		try {
			Query query = em.createQuery("delete from EmailAddress t where t.address=:address");
			query.setParameter("address", EmailAddrUtil.removeDisplayName(addr));
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from EmailAddress t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(EmailAddress emailAddr) {
		if (EmailAddrUtil.hasDisplayName(emailAddr.getAddress())) {
			emailAddr.setAddress(EmailAddrUtil.removeDisplayName(emailAddr.getAddress()));
		}
		try {
			em.persist(emailAddr);
			em.flush();
		}
		finally {
		}
	}
	
	public void update(EmailAddress emailAddr) {
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
			em.flush();
		}
		finally {
		}
	}
	
	public void updateLastRcptTime(int rowId) {
		try {
			EmailAddress ea = getByRowId(rowId);
			ea.setLastRcptTime(new java.sql.Timestamp(System.currentTimeMillis()));
			update(ea);
		}
		catch (NoResultException e) {}
		finally {
		}
	}

	public void updateLastSentTime(int rowId) {
		try {
			EmailAddress ea = getByRowId(rowId);
			ea.setLastSentTime(new java.sql.Timestamp(System.currentTimeMillis()));
			update(ea);
		}
		catch (NoResultException e) {}
		finally {
		}
	}

	public void updateBounceCount(EmailAddress emailAddr) {
		emailAddr.setBounceCount(emailAddr.getBounceCount()+1);
		if (emailAddr.getBounceCount() >= Constants.BOUNCE_SUSPEND_THRESHOLD) {
			if (!StatusId.SUSPENDED.getValue().equals(emailAddr.getStatusId())) {
				emailAddr.setStatusId(StatusId.SUSPENDED.getValue());
				if (StringUtils.isNotBlank(emailAddr.getUpdtUserId())) {
					emailAddr.setStatusChangeUserId(emailAddr.getUpdtUserId());
				} else {
					emailAddr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				}
				emailAddr.setStatusChangeTime(new java.sql.Timestamp(System.currentTimeMillis()));
			}
		}
		update(emailAddr);
	}
}
