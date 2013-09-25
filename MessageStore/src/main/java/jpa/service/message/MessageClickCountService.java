package jpa.service.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageClickCount;
import jpa.msgui.vo.PagingVo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageClickCountService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageClickCountService {
	static Logger logger = Logger.getLogger(MessageClickCountService.class);
	
	@Autowired
	EntityManager em;

	public MessageClickCount getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageClickCount t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageClickCount record = (MessageClickCount) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageClickCount getByMsgInboxId(int msgId) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageClickCount t, MessageInbox mi " +
				" where mi=t.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			MessageClickCount record = (MessageClickCount) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageClickCount getLastRecord() throws NoResultException {
		String sql = 
				"select t.* " +
				"from " +
					"Message_Click_Count t " +
				" where t.Row_Id = (select max(t2.Row_Id) from Message_Click_Count t2) ";
		try {
			Query query = em.createNativeQuery(sql, MessageClickCount.MAPPING_MSG_CLICK_COUNT_ENTITY);
			MessageClickCount record = (MessageClickCount) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public void delete(MessageClickCount clickCount) {
		if (clickCount == null) return;
		try {
			em.remove(clickCount);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageClickCount t " +
				" where t.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageClickCount t " +
				" where t.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageClickCount clickCount) {
		try {
			if (em.contains(clickCount)) {
				em.persist(clickCount);
			}
			else {
				em.merge(clickCount);
			}
		}
		finally {
		}
	}

	public int updateStartTime(int msgId) {
		String sql = 
				"update MessageClickCount t " +
				" set t.startTime = CURRENT_TIMESTAMP " +
				" where t.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int updateSentCount(int msgId) {
		return updateSentCount(msgId, 1);
	}

	public int updateSentCount(int msgId, int mailsSent) {
		String sql = 
				"update MessageClickCount t " +
				" set t.sentCount = (t.sentCount + :mailsSent) " +
				" where t.messageInbox.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("mailsSent", mailsSent);
			query.setParameter("msgId", msgId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MessageClickCount clickCount) {
		try {
			em.persist(clickCount);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and " };

	public List<MessageClickCount> getBroadcastsWithPaging(PagingVo vo) {
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
			List<MessageClickCount> lastList = new ArrayList<MessageClickCount>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<MessageClickCount> nextList = getBroadcastsWithPaging(vo);
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
		
		String sql = 
			"select a.* " +
			" from Message_Click_Count a " +
			whereSql +
			" and a.StartTime is not null " +
			" order by a.Row_Id " + fetchOrder;
		//if (Constants.DB_PRODNAME_MYSQL.equals(JpaUtil.getDBProductName())) {
		//	sql += " limit " + vo.getPageSize();
		//}
		Query query = em.createNativeQuery(sql, MessageClickCount.MAPPING_MSG_CLICK_COUNT_ENTITY);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		@SuppressWarnings("unchecked")
		List<MessageClickCount> list = query.setMaxResults(vo.getPageSize()).getResultList();
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	public int getMessageCountForWeb() {
		String sql = 
			"select count(*) " +
			"from " +
				"Message_Click_Count where SentCount > 0 and StartTime is not null ";
		Query query = em.createNativeQuery(sql);
		Number count = (Number) query.getSingleResult();
		return count.intValue();
	}
	
}
