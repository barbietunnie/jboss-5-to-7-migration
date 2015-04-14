package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.util.JpaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("senderDataService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class SenderDataService implements java.io.Serializable {
	private static final long serialVersionUID = -5718921335820858248L;

	static Logger logger = Logger.getLogger(SenderDataService.class);
	
	@Autowired
	//@PersistenceContext(name="MessageDB")
	EntityManager em;

	@Transactional(isolation=Isolation.READ_UNCOMMITTED) // did not help with "Lock wait timeout exceeded"
	public SenderData getBySenderId(String senderId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SenderData t where t.senderId = :senderId");
			query.setParameter("senderId", senderId);
			if (StringUtils.containsIgnoreCase(JpaUtil.getJpaDialect(), "EclipseLink")) {
				query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
			}
			SenderData sender = (SenderData) query.getSingleResult();
			return sender;
		}
		finally {
		}
	}
	
	public SenderData getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SenderData t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			SenderData sender = (SenderData) query.getSingleResult();
			//em.lock(sender, LockModeType.OPTIMISTIC);
			return sender;
		}
		finally {
		}
	}
	
	public SenderData getByDomainName(String domainName) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SenderData t where t.domainName = :domainName");
			query.setParameter("domainName", domainName);
			SenderData senders = (SenderData) query.getSingleResult();
			return senders;
		}
		finally {
		}
	}

	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<SenderData> getAll() {
		try {
			Query query = em.createQuery("select t from SenderData t");
			@SuppressWarnings("unchecked")
			List<SenderData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public String getSystemId() throws NoResultException {
		try {
			Query query = em.createQuery("select t.systemId from SenderData t where t.senderId = :senderId");
			query.setParameter("senderId", Constants.DEFAULT_SENDER_ID);
			String systemId = (String) query.getSingleResult();
			return systemId;
		}
		finally {
		}		
	}

	public String getSystemKey() throws NoResultException {
		try {
			Query query = em.createQuery("select t.systemKey from SenderData t where t.senderId = :senderId");
			query.setParameter("senderId", Constants.DEFAULT_SENDER_ID);
			String systemKey = (String) query.getSingleResult();
			return systemKey;
		}
		finally {
		}		
	}

	public void delete(SenderData sender) {
		if (sender==null) return;
		try {
			em.remove(sender);
		}
		finally {
		}
	}

	public int deleteBySenderId(String senderId) {
		try {
			Query query = em.createQuery("delete from SenderData t where t.senderId=:senderId");
			query.setParameter("senderId", senderId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from SenderData t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public void insert(SenderData sender) {
		try {
			validateSender(sender);
			em.persist(sender);
			reloadFlagsService.updateSenderReloadFlag();
			em.flush();
		}
		finally {
		}
	}
	
	public void update(SenderData sender) {
		try {
			validateSender(sender);
			if (em.contains(sender)) {
				em.persist(sender);
			}
			else {
				em.merge(sender);
			}
			reloadFlagsService.updateSenderReloadFlag();
			em.flush();
		}
		finally {
		}
	}
	
	private void validateSender(SenderData sender) {
		if (sender.isUseTestAddr()) {
			if (StringUtils.isBlank(sender.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (sender.isVerpEnabled()) {
			if (StringUtils.isBlank(sender.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtils.isBlank(sender.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}

}
