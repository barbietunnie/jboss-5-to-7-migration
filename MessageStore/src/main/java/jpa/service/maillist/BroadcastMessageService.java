package jpa.service.maillist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import jpa.model.BroadcastMessage;
import jpa.msgui.vo.PagingVo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("broadcastMessageService")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastMessageService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = Logger.getLogger(BroadcastMessageService.class);
	
	@Autowired
	EntityManager em;
	
	public BroadcastMessage getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastMessage t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			BroadcastMessage broadcast = (BroadcastMessage) query.getSingleResult();
			//em.lock(broadcast, LockModeType.OPTIMISTIC);
			return broadcast;
		}
		finally {
		}
	}
	
	public List<BroadcastMessage> getByMailingListId(String listId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastMessage t, MailingList ml " +
					" where ml=t.mailingList and ml.listId = :listId");
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<BroadcastMessage> list = (List<BroadcastMessage>) query.getResultList();
			//em.lock(BroadcastMessage, LockModeType.OPTIMISTIC);
			return list;
		}
		finally {
		}
	}

	public List<BroadcastMessage> getByEmailTemplateId(String templateId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from BroadcastMessage t, EmailTemplate et " +
					" where et=t.emailTemplate and et.templateId=:templateId ");
			query.setParameter("templateId", templateId);
			@SuppressWarnings("unchecked")
			List<BroadcastMessage> list = (List<BroadcastMessage>) query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<BroadcastMessage> getAll() {
		String sql = "select t from BroadcastMessage t order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			@SuppressWarnings("unchecked")
			List<BroadcastMessage> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public int updateOpenCount(int rowId) {
		String sql = "update BroadcastMessage t set t.openCount = (t.openCount + 1) where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("rowId", rowId);
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
	}
	
	public int updateClickCount(int rowId) {
		String sql = "update BroadcastMessage t set t.clickCount = (t.clickCount + 1) where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("rowId", rowId);
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
	}
	
	public int updateReferalCount(int rowId) {
		String sql = "update BroadcastMessage t set t.referralCount = (t.referralCount + 1) where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("rowId", rowId);
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
	}
	
	public int updateUnsubscribeCount(int rowId) {
		String sql = "update BroadcastMessage t set t.unsubscribeCount = (t.unsubscribeCount + 1) where t.rowId = :rowId";
		Query query = em.createQuery(sql);
		query.setParameter("rowId", rowId);
		int rowsupdated = query.executeUpdate();
		return rowsupdated;
	}
	
	public int getMessageCountForWeb() {
		String sql = "select count(t) from " +
				"BroadcastMessage t where t.sentCount > 0 and t.startTime is not null ";
		try {
			Query query = em.createQuery(sql);
			Long count = (Long) query.getSingleResult();
			return count.intValue();
		}
		finally {
		}
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and " };
	
	public List<BroadcastMessage> getBroadcastsWithPaging(PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = "";
		/*
		 * paging logic
		 */
		String fetchOrder = "desc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id < ? ";
				parms.add(vo.getIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id > ? ";
				parms.add(vo.getIdFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<BroadcastMessage> lastList = new ArrayList<BroadcastMessage>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<BroadcastMessage> nextList = getBroadcastsWithPaging(vo);
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
				whereSql += CRIT[parms.size()] + " a.Row_Id <= ? ";
				parms.add(vo.getIdFirst());
			}
		}
		whereSql += CRIT[parms.size()] + " a.SentCount > ? ";
		parms.add(0);
		
		String sql = "select a.* " +
			" from Broadcast_Message a " +
			whereSql +
			" and a.StartTime is not null " +
			" order by a.Row_Id " + fetchOrder;
		Query query = em.createNativeQuery(sql, BroadcastMessage.MAPPING_BROADCAST_MESSAGE_ENTITY);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		//query.setFirstResult(0);
		query.setMaxResults(vo.getPageSize());
		@SuppressWarnings("unchecked")
		List<BroadcastMessage> list = query.getResultList();
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	public void delete(BroadcastMessage broadcast) {
		if (broadcast == null) return;
		try {
			em.remove(broadcast);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from BroadcastMessage t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(BroadcastMessage broadcast) {
		try {
			em.persist(broadcast);
			em.flush();
		}
		finally {
		}
	}

	public void update(BroadcastMessage broadcast) {
		try {
			if (em.contains(broadcast)) {
				em.persist(broadcast);
			}
			else {
				em.merge(broadcast);
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
