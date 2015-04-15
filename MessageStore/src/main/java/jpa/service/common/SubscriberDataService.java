package jpa.service.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.constant.MobileCarrierEnum;
import jpa.exception.DataValidationException;
import jpa.model.SubscriberData;
import jpa.msgui.vo.PagingSubscriberData;
import jpa.msgui.vo.PagingVo;
import jpa.util.EmailSender;
import jpa.util.PhoneNumberUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("subscriberDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriberDataService implements java.io.Serializable {
	private static final long serialVersionUID = 744183660636136777L;

	static Logger logger = Logger.getLogger(SubscriberDataService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	EmailAddressService emailService;

	public SubscriberData getBySubscriberId(String subscriberId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SubscriberData t where " +
					" t.subscriberId = :subscriberId");
			query.setParameter("subscriberId", subscriberId);
			SubscriberData subscriber = (SubscriberData) query.getSingleResult();
			//em.lock(subscriber, LockModeType.OPTIMISTIC);
			return subscriber;
		}
		finally {
		}
	}
	
	public SubscriberData getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SubscriberData t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			SubscriberData subscriber = (SubscriberData) query.getSingleResult();
			//em.lock(subscriber, LockModeType.OPTIMISTIC);
			return subscriber;
		}
		finally {
		}
	}
	
	public SubscriberData getByEmailAddress(String address) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SubscriberData t, EmailAddress ea where " +
					" ea=t.emailAddr and ea.address = :address");
			query.setParameter("address", address);
			SubscriberData subscriber = (SubscriberData) query.getSingleResult();
			//em.lock(subscriber, LockModeType.OPTIMISTIC);
			return subscriber;
		}
		finally {
		}
	}
	
	public List<SubscriberData> getAll() {
		try {
			Query query = em.createQuery("select t from SubscriberData t");
			@SuppressWarnings("unchecked")
			List<SubscriberData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(SubscriberData subscriber) {
		if (subscriber==null) return;
		try {
			em.remove(subscriber);
		}
		finally {
		}
	}

	public int deleteBySubscriberId(String subscriberId) {
		try {
			Query query = em.createQuery("delete from SubscriberData t where t.subscriberId=:subscriberId");
			query.setParameter("subscriberId", subscriberId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from SubscriberData t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(SubscriberData subscriber) {
		verifySubscriberData(subscriber);
		try {
			em.persist(subscriber);
			em.flush();
		}
		finally {
		}
	}
	
	public void update(SubscriberData subscriber) {
		verifySubscriberData(subscriber);
		try {
			if (em.contains(subscriber)) {
				em.persist(subscriber);
			}
			else {
				em.merge(subscriber);
			}
		}
		finally {
			em.flush();
		}
	}
	
	private void verifySubscriberData(SubscriberData subscriber) throws DataValidationException {
		if (StringUtils.isNotBlank(subscriber.getMobilePhone())) {
			if (!PhoneNumberUtil.isValidPhoneNumber(subscriber.getMobilePhone())) {
				throw new DataValidationException("Invalid Mobile phone number passed in: " + subscriber.getMobilePhone());
			}
			try {
				MobileCarrierEnum.getByValue(subscriber.getMobileCarrier());
			}
			catch (IllegalArgumentException e) {
				// could be a new carrier not yet entered in system, notify programming
				String msg = "Invalid Mobile carrier passed in: " + subscriber.getMobileCarrier();
				String subj = "(" + subscriber.getMobileCarrier() + ") need to be added to the system - {0}";
				EmailSender.sendEmail(subj, msg, ExceptionUtils.getStackTrace(e), EmailSender.EmailList.ToDevelopers);
			}
		}
		if (subscriber.getEmailAddr()==null) {
			throw new IllegalArgumentException("An EmailAddress instance must be provided in the entity.");
		}
	}

	public List<SubscriberData> getSubscribersWithPaging(PagingSubscriberData vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
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
				parms.add(vo.getStrIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.Row_Id < ? ";
				parms.add(vo.getStrIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<SubscriberData> lastList = new ArrayList<SubscriberData>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<SubscriberData> nextList = getSubscribersWithPaging(vo);
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
				parms.add(vo.getStrIdFirst());
			}
		}
		String sql = 
			"select a.* " +
			" from Subscriber_Data a " +
				" JOIN Sender_Data s on s.Row_Id = a.SenderDataRowId " +
				" LEFT JOIN Email_Address b on a.EmailAddrRowId=b.Row_Id ";
		// search by email address
		if (StringUtils.isNotBlank(vo.getEmailAddr())) {
			String addr = vo.getEmailAddr().trim();
			sql += " and b.Address LIKE '%" + addr + "%' ";
		}
		sql += whereSql +
			" order by a.Row_Id " + fetchOrder;
		//if (Constants.DB_PRODNAME_MYSQL.equals(JpaUtil.getDBProductName())) {
		//	sql += " limit " + vo.getPageSize();
		//}
		Query query = em.createNativeQuery(sql, SubscriberData.MAPPING_SUBSCRIBER_DATA_ENTITY);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		@SuppressWarnings("unchecked")
		List<SubscriberData> list = query.setMaxResults(vo.getPageSize()).getResultList();
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };
	
	private String buildWhereClause(PagingSubscriberData vo, List<Object> parms) {
		String whereSql = "";
		if (StringUtils.isNotBlank(vo.getSenderId())) {
			whereSql += CRIT[parms.size()] + " (s.SenderId = ?) ";
			parms.add(vo.getSenderId());
		}
		if (StringUtils.isNotBlank(vo.getSsnNumber())) {
			whereSql += CRIT[parms.size()] + " a.SsnNumber = ? ";
			parms.add(vo.getSsnNumber());
		}
		if (StringUtils.isNotBlank(vo.getLastName())) {
			whereSql += CRIT[parms.size()] + " a.LastName = ? ";
			parms.add(vo.getLastName());
		}
		if (StringUtils.isNotBlank(vo.getFirstName())) {
			whereSql += CRIT[parms.size()] + " a.FirstName = ? ";
			parms.add(vo.getFirstName());
		}
		if (StringUtils.isNotBlank(vo.getDayPhone())) {
			whereSql += CRIT[parms.size()] + " a.DayPhone = ? ";
			parms.add(vo.getDayPhone());
		}
		if (StringUtils.isNotBlank(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		return whereSql;
	}

	public int getSubscriberCount(PagingSubscriberData vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = 
			"select count(*) as subr_count from Subscriber_Data a " +
				" JOIN Sender_Data s on s.Row_Id = a.SenderDataRowId " +
			whereSql;
		Query query = em.createNativeQuery(sql);
		for (int i=0; i<parms.size(); i++) {
			query.setParameter(i+1, parms.get(i));
		}
		Number count = (Number) query.getSingleResult();
		return count.intValue();
	}

}
