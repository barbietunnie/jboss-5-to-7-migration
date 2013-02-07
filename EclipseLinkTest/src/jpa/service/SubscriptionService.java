package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddr;
import jpa.model.MailingList;
import jpa.model.Subscription;

@Component("subscriptionService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriptionService {
	static Logger logger = Logger.getLogger(SubscriptionService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	EmailAddrService emailAddrService;
	
	@Autowired
	MailingListService mailingListService;
	
	public List<Subscription> getByListId(String listId) {
		try {
			Query query = em.createQuery("select t from Subscription t, MailingList l " +
					" where l=t.mailingList and l.listId = :listId");
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<Subscription> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<Subscription> getByAddress(String address) {
		try {
			Query query = em.createQuery("select t from Subscription t, EmailAddr e " +
					" where e=t.emailAddr and e.address = :address");
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<Subscription> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
//	public Subscription getByPrimaryKey(int emailAddrRowId, int mailingListRowId) throws NoResultException {
//		try {
//			Query query = em.createQuery("select t from Subscription t, EmailAddr e, MailingList m " +
//					" where e=t.emailAddr and m=t.mailingList and e.rowId=:emailAddrRowId and m.rowId=:mailingListRowId");
//			query.setParameter("emailAddrRowId", emailAddrRowId);
//			query.setParameter("mailingListRowId", mailingListRowId);
//			Subscription subscription = (Subscription) query.getSingleResult();
//			em.lock(subscription, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
//			return subscription;
//		}
//		finally {
//		}
//	}
	
	public Subscription getByAddressAndListId(String address, String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Subscription t, EmailAddr e, MailingList m " +
					" where e=t.emailAddr and m=t.mailingList and e.address=:address and m.listId=:listId");
			query.setParameter("address", address);
			query.setParameter("listId", listId);
			Subscription subscription = (Subscription) query.getSingleResult();
			em.lock(subscription, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return subscription;
		}
		finally {
		}
	}
	
	public Subscription getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Subscription t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			Subscription subscription = (Subscription) query.getSingleResult();
			em.lock(subscription, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return subscription;
		}
		finally {
		}
	}
	
	public Subscription subscribe(String address, String listId) {
		EmailAddr emailAddr = emailAddrService.findSertAddress(address);
		MailingList list = null;
		try {
			list = mailingListService.getByListId(listId);
		}
		catch (NoResultException e) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (!sub.isSubscribed()) {
				sub.setSubscribed(true);
				sub.setStatusId(StatusId.ACTIVE.getValue());
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				update(sub);
			}
		}
		catch (NoResultException e) {
			sub = new Subscription();
			sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
			sub.setEmailAddr(emailAddr);
			sub.setMailingList(list);
			sub.setSubscribed(true);
			sub.setStatusId(StatusId.ACTIVE.getValue());
			sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
			insert(sub);
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public Subscription unsubscribe(String address, String listId) {
		// to harvest email address from the request
		EmailAddr emailAddr = emailAddrService.findSertAddress(address);
		MailingList list = null;
		try {
			list = mailingListService.getByListId(listId);
		}
		catch (NoResultException e) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (sub.isSubscribed()) {
				sub.setSubscribed(false);
				sub.setStatusId(StatusId.INACTIVE.getValue());
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				update(sub);
			}
		}
		catch (NoResultException e) {
			sub = new Subscription();
			sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
			sub.setEmailAddr(emailAddr);
			sub.setMailingList(list);
			sub.setSubscribed(false);
			sub.setStatusId(StatusId.INACTIVE.getValue());
			sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
			insert(sub);
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public Subscription optInRequest(String address, String listId) {
		EmailAddr emailAddr = emailAddrService.findSertAddress(address);
		MailingList list = null;
		try {
			list = mailingListService.getByListId(listId);
		}
		catch (NoResultException e) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (!sub.isSubscribed() && !Boolean.TRUE.equals(sub.getIsOptIn())) {
				sub.setIsOptIn(Boolean.TRUE);
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				update(sub);
			}
		}
		catch (NoResultException e) {
			sub = new Subscription();
			sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
			sub.setEmailAddr(emailAddr);
			sub.setMailingList(list);
			sub.setSubscribed(false);
			sub.setIsOptIn(Boolean.TRUE);
			sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
			insert(sub);
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public Subscription optInConfirm(String address, String listId) {
		EmailAddr emailAddr = emailAddrService.findSertAddress(address);
		emailAddr.setStatusId(StatusId.ACTIVE.getValue());
		MailingList list = null;
		try {
			list = mailingListService.getByListId(listId);
		}
		catch (NoResultException e) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (!sub.isSubscribed() && Boolean.TRUE.equals(sub.getIsOptIn())) {
				sub.setSubscribed(true);
				sub.setStatusId(StatusId.ACTIVE.getValue());
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				update(sub);
			}
		}
		catch (NoResultException e) {
			sub = new Subscription();
			sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
			sub.setEmailAddr(emailAddr);
			sub.setMailingList(list);
			sub.setSubscribed(true);
			sub.setStatusId(StatusId.ACTIVE.getValue());
			sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
			insert(sub);
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public void delete(Subscription subscription) {
		if (subscription == null) return;
		try {
			em.remove(subscription);
		}
		finally {
		}
	}

	public int deleteByListId(String listId) {
		try {
			Query query = em.createNativeQuery("delete from Subscription where MailingListRowid in " +
					" (select row_id from mailing_List ml where ml.listId=?1)");
			query.setParameter(1, listId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByAddress(String address) {
		try {
			Query query = em.createNativeQuery("delete from Subscription where EmailAddrRowid in " +
					" (select row_id from email_addr ea where ea.address=?1)");
			query.setParameter(1, address);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

//	public int deleteByPrimaryKey(int emailAddrRowId, int mailingListRowId) {
//		try {
//			Query query = em.createNativeQuery("delete from Subscription where " +
//					" emailAddrRowId = (select row_id from email_addr ea where ea.row_Id=?1) " +
//					" and mailingListRowid = (select row_id from mailing_list ml where ml.row_Id=?2 )");
//			query.setParameter(1, emailAddrRowId);
//			query.setParameter(2, mailingListRowId);
//			int rows = query.executeUpdate();
//			return rows;
//		}
//		finally {
//		}
//	}

	public int deleteByAddressAndListId(String address, String listId) {
		try {
			Query query = em.createNativeQuery("delete from Subscription where " +
					" emailAddrRowId = (select row_id from email_addr ea where ea.address=?1) " +
					" and mailingListRowid = (select row_id from mailing_list ml where ml.listId=?2 )");
			query.setParameter(1, address);
			query.setParameter(2, listId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from Subscription t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(Subscription subscription) {
		try {
			em.persist(subscription);
		}
		finally {
		}
	}

	public void update(Subscription subscription) {
		try {
			if (em.contains(subscription)) {
				em.persist(subscription);
			}
			else {
				em.merge(subscription);
			}
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}

}
