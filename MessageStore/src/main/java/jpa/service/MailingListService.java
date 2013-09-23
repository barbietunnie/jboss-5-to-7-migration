package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.constant.StatusId;
import jpa.model.MailingList;
import jpa.util.EmailAddrUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("mailingListService")
@Transactional(propagation=Propagation.REQUIRED)
public class MailingListService {
	static Logger logger = Logger.getLogger(MailingListService.class);
	
	@Autowired
	EntityManager em;
	
	public MailingList getByListId(String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MailingList t " +
					" where t.listId = :listId");
			query.setParameter("listId", listId);
			MailingList mailingList = (MailingList) query.getSingleResult();
			//em.lock(mailingList, LockModeType.OPTIMISTIC);
			return mailingList;
		}
		finally {
		}
	}

	public MailingList getByListAddress(String address) throws NoResultException {
		String domain = EmailAddrUtil.getEmailDomainName(address);
		String acctUser = EmailAddrUtil.getEmailLeftPart(address);
		try {
			Query query = em.createQuery("select t from MailingList t, SenderData sd " +
					" where sd=t.senderData and t.acctUserName=:acctUser and sd.domainName=:domain ");
			query.setParameter("acctUser", acctUser);
			query.setParameter("domain", domain);
			MailingList mailingList = (MailingList) query.getSingleResult();
			return mailingList;
		}
		finally {
		}
	}

	public List<MailingList> getByEmailAddress(String address) throws NoResultException {
		String sql =
			"select t from MailingList t, Subscription sub, EmailAddress ea " +
				" where sub=t.subscriptions and sub.emailAddr=ea and ea.address=:address ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<MailingList> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	/*
	 * return an array with 4 elements:
	 * 1) MailingList
	 * 2) through 4) BigDecimal (MySQL) or BigInteger (PostgreSQL)
	 */
	public Object[] getByListIdWithCounts(String listId) throws NoResultException {
		String sql = "select a.*, " +
				" sum(b.SentCount) as sentCount, sum(b.OpenCount) as openCount," +
				" sum(b.ClickCount) as clickCount " +
				"from Mailing_List a " +
				" LEFT OUTER JOIN Subscription b on a.Row_Id = b.MailingListRowId " +
				" JOIN sender_data c on a.SenderDataRowId = c.Row_Id " +
				" where a.ListId = ?1 " +
				"group by " +
				" a.Row_Id, " +
				" a.ListId, " +
				" a.DisplayName, " +
				" a.AcctUserName, " +
				" a.Description, " +
				" a.StatusId, " +
				" a.IsBuiltin, " +
				" a.IsSendText, " +
				" a.CreateTime, " +
				" a.UpdtUserid, " +
				" a.UpdtTime, " +
				" a.SenderDataRowId, " +
				" a.ListMasterEmailAddr ";
		try {
			Query query = em.createNativeQuery(sql,MailingList.MAPPING_MAILING_LIST_WITH_COUNTS);
			query.setParameter(1, listId);
			Object[] mailingList = (Object[]) query.getSingleResult();
			return mailingList;
		}
		finally {
		}
	}
	
	public MailingList getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MailingList t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			MailingList mailingList = (MailingList) query.getSingleResult();
			//em.lock(mailingList, LockModeType.OPTIMISTIC);
			return mailingList;
		}
		finally {
		}
	}
	
	public List<MailingList> getAll(boolean onlyActive) {
		String sql = "select t from MailingList t ";
		if (onlyActive) {
			sql += " where t.statusId=:statusId ";
		}
		sql += " order by t.listId ";
		try {
			Query query = em.createQuery(sql);
			if (onlyActive) {
				query.setParameter("statusId", StatusId.ACTIVE.getValue());
			}
			@SuppressWarnings("unchecked")
			List<MailingList> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(MailingList mailingList) {
		if (mailingList == null) return;
		try {
			em.remove(mailingList);
		}
		finally {
		}
	}

	public int deleteByListId(String listId) {
		try {
			Query query = em.createQuery("delete from MailingList t where t.listId=:listId");
			query.setParameter("listId", listId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteBySenderId(String senderId) {
		try {
			Query query = em.createNativeQuery("delete from Mailing_List where senderDataRowId in " +
					" (select row_id from sender_data cd where cd.senderId=?1)");
			query.setParameter(1, senderId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from MailingList t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MailingList mailingList) {
		try {
			em.persist(mailingList);
		}
		finally {
		}
	}

	public void update(MailingList mailingList) {
		try {
			if (em.contains(mailingList)) {
				em.persist(mailingList);
			}
			else {
				em.merge(mailingList);
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
