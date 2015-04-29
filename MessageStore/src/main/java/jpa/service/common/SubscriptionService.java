package jpa.service.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.msgui.vo.PagingVo;
import jpa.service.maillist.MailingListService;
import jpa.util.StringUtil;

@Component("subscriptionService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriptionService implements java.io.Serializable {
	private static final long serialVersionUID = 2020862404406193032L;

	static Logger logger = Logger.getLogger(SubscriptionService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	EmailAddressService emailAddrService;
	
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
	
	public List<Subscription> getByListIdSubscribersOnly(String listId) {
		String sql = "select t from Subscription t, MailingList l, " +
				" SubscriberData sub, EmailAddress ea " +
				" where l=t.mailingList and l.listId = :listId " +
				" and ea=t.emailAddr and sub=ea.subscriberData and sub is not null";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<Subscription> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<Subscription> getByListIdProsperctsOnly(String listId) {
		String sql = "select t from Subscription t, MailingList l, " +
				" EmailAddress ea " +
				" where l=t.mailingList and l.listId = :listId " +
				" and ea=t.emailAddr and not exists "
				+ "(select sub from SubscriberData sub where sub=ea.subscriberData)";
		try {
			Query query = em.createQuery(sql);
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
			Query query = em.createQuery("select t from Subscription t, EmailAddress e " +
					" where e=t.emailAddr and e.address = :address");
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<Subscription> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public Subscription getByUniqueKey(int emailAddrRowId, String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Subscription t, EmailAddress e, MailingList m " +
					" where e=t.emailAddr and m=t.mailingList and e.rowId=:emailAddrRowId and m.listId=:listId");
			query.setParameter("emailAddrRowId", emailAddrRowId);
			query.setParameter("listId", listId);
			Subscription subscription = (Subscription) query.getSingleResult();
			return subscription;
		}
		finally {
		}
	}
	
	public Subscription getByAddressAndListId(String address, String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from Subscription t, EmailAddress e, MailingList m " +
					" where e=t.emailAddr and m=t.mailingList and e.address=:address and m.listId=:listId");
			query.setParameter("address", address);
			query.setParameter("listId", listId);
			Subscription subscription = (Subscription) query.getSingleResult();
			//em.lock(subscription, LockModeType.OPTIMISTIC);
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
			//em.lock(subscription, LockModeType.OPTIMISTIC);
			return subscription;
		}
		finally {
		}
	}
	
	public Subscription subscribe(String address, String listId) {
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
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
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
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
	
	public Subscription addToList(String sbsrEmailAddr, String listEmailAddr) {
		try {
			MailingList mlist = mailingListService.getByListAddress(listEmailAddr);
			return subscribe(sbsrEmailAddr, mlist.getListId());
		}
		catch (NoResultException e) {
			throw new IllegalArgumentException("Mailing List Email Address (" + listEmailAddr + ") not found.");
		}
	}
	
	public Subscription removeFromList(String sbsrEmailAddr, String listEmailAddr) {
		try {
			MailingList mlist = mailingListService.getByListAddress(listEmailAddr);
			return unsubscribe(sbsrEmailAddr, mlist.getListId());
		}
		catch (NoResultException e) {
			throw new IllegalArgumentException("Mailing List Email Address (" + listEmailAddr + ") not found.");
		}
	}
	
	public Subscription optInRequest(String address, String listId) {
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
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
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
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
					" (select row_id from email_address ea where ea.address=?1)");
			query.setParameter(1, address);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByUniqueKey(int emailAddrRowId, String listId) {
		try {
			Query query = em.createNativeQuery("delete from Subscription where " +
					" emailAddrRowId = (select row_id from email_address ea where ea.row_Id=?1) " +
					" and mailingListRowid = (select row_id from mailing_list ml where ml.listId=?2 )");
			query.setParameter(1, emailAddrRowId);
			query.setParameter(2, listId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByAddressAndListId(String address, String listId) {
		try {
			Query query = em.createNativeQuery("delete from Subscription where " +
					" emailAddrRowId = (select row_id from email_address ea where ea.address=?1) " +
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
			em.flush(); // to populate the @Id field
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
			em.flush();
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}

	public int updateSentCount(int rowId) {
		return updateSentCount(rowId, 1);
	}

	public int updateSentCount(int rowId, int mailsSent) {
		String sql = 
				"update Subscription t " +
				" set t.sentCount = (t.sentCount + :mailsSent) " +
				"where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("mailsSent", mailsSent);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int updateClickCount(int emailAddrRowId, String listId) {
		String sql = 
				"update Subscription " +
				" set clickCount = (clickCount + 1) " +
				"where emailAddrRowId = ?1 "
				+ "and mailingListRowId = (select row_id from mailing_list where listId = ?2)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, emailAddrRowId);
			query.setParameter(2, listId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int updateOpenCount(int emailAddrRowId, String listId) {
		String sql = 
				"update Subscription " +
				" set openCount = (openCount + 1) " +
				"where emailAddrRowId = ?1 "
				+ "and mailingListRowId = (select row_id from mailing_list where listId = ?2)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, emailAddrRowId);
			query.setParameter(2, listId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };

	public List<Subscription> getSubscriptionsWithPaging(String listId, PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(listId, vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "asc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id > ? ";
				parms.add(vo.getIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id < ? ";
				parms.add(vo.getIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<Subscription> lastList = new ArrayList<Subscription>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<Subscription> nextList = getSubscriptionsWithPaging(listId, vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setIdLast(nextList.get(nextList.size() - 1).getRowId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id >= ? ";
				parms.add(vo.getIdFirst());
			}
		}
		String sql = 
			"select a.* " +
			" from Subscription a" +
				" JOIN Email_Address b ON a.EmailAddrRowId=b.Row_Id " +
				" JOIN Mailing_List m ON a.MailingListRowId=m.Row_Id " +
				" LEFT JOIN Subscriber_Data c on a.EmailAddrRowId=c.EmailAddrRowId " +
			whereSql +
			" order by a.Row_Id " + fetchOrder;
		//if (Constants.DB_PRODNAME_MYSQL.equals(JpaUtil.getDBProductName())) {
		//	sql += " limit " + vo.getPageSize();
		//}
		Query query = em.createNativeQuery(sql, Subscription.MAPPING_SUBSCRIPTION_ENTITY);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		@SuppressWarnings("unchecked")
		List<Subscription> list = query.setMaxResults(vo.getPageSize()).getResultList();
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	private String buildWhereClause(String listId, PagingVo vo, List<Object> parms) {
		String whereSql = CRIT[parms.size()] + " m.ListId = ? ";
		parms.add(listId);
		if (StringUtils.isNotBlank(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " b.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		// search by address
		if (StringUtils.isNotBlank(vo.getSearchString())) {
			String addr = vo.getSearchString().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " b.OrigAddress LIKE ? ";
				parms.add("%" + addr + "%");
			}
			else {
				String regex = StringUtil.replaceAll(addr, " ", ".+");
				whereSql += CRIT[parms.size()] + " b.OrigAddress REGEXP '" + regex + "' ";
			}
		}
		return whereSql;
	}

	public int getSubscriptionCount(String listId, PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(listId, vo, parms);
		String sql = 
			"select count(*) as subp_count " +
			" from Subscription a " +
				" JOIN Email_Address b ON a.EmailAddrRowId=b.Row_Id " +
				" JOIN Mailing_List m ON a.MailingListRowId=m.Row_Id " +
			whereSql;
		Query query = em.createNativeQuery(sql);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		Number count = (Number) query.getSingleResult();
		return count.intValue();
	}

}
