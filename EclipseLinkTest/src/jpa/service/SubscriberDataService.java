package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.constant.MobileCarrierEnum;
import jpa.exception.DataValidationException;
import jpa.model.SubscriberData;
import jpa.util.PhoneNumberUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("subscriberDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriberDataService {
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
				//throw new DataValidationException("Invalid Mobile carrier passed in: " + subscriber.getMobileCarrier());
				// TODO could be a new carrier not yet entered in system, notify programming
				// TODO define a mobile carrier table to store the information.
			}
		}
		if (subscriber.getEmailAddr()==null) {
			if (StringUtils.isNotBlank(subscriber.getEmailAddress())) {
				subscriber.setEmailAddr(emailService.findSertAddress(subscriber.getEmailAddress()));
			}
			else {
				throw new IllegalArgumentException("An EmailAddress instance must be provided in the entity.");
			}
		}
	}
	
}
