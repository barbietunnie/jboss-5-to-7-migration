package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.MessageAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageAddressService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageAddressService {
	static Logger logger = Logger.getLogger(MessageAddressService.class);
	
	@Autowired
	EntityManager em;

	public MessageAddress getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageAddress t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageAddress record = (MessageAddress) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageAddress getByPrimaryKey(int msgId, String addrType, String address) throws NoResultException {
		String sql = 
				"select t " +
				"from MessageAddress t, MessageInbox mi, EmailAddress ea where " +
					" mi=t.messageInbox and mi.rowId=:msgId " +
					" and ea.rowId=t.emailAddrRowId and ea.address=:address " +
					" and t.addressType=:addrType ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("msgId", msgId);
				query.setParameter("addrType", addrType);
				query.setParameter("address", address);
				MessageAddress record = (MessageAddress) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageAddress> getByMsgInboxId(int msgId) {
		String sql = 
				"select t " +
				"from MessageAddress t, MessageInbox mi " +
				" where mi=t.messageInbox and mi.rowId=:msgId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("msgId", msgId);
			@SuppressWarnings("unchecked")
			List<MessageAddress> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(MessageAddress msgAddress) {
		if (msgAddress == null) return;
		try {
			em.remove(msgAddress);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageAddress t " +
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

	public int deleteByPrimaryKey(int msgId, String addrType, String addrValue) {
		String sql = 
				"delete from Message_Address where " +
				" MessageInboxRowId = ?1 and addressType=?2 and EmailAddrRowId = " +
				" (select row_id from email_address ea where ea.address=?3) ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, msgId);
			query.setParameter(2, addrType);
			query.setParameter(3, addrValue);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int msgId) {
		String sql = 
				"delete from MessageAddress t " +
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

	public void update(MessageAddress msgAddress) {
		try {
			if (em.contains(msgAddress)) {
				em.persist(msgAddress);
			}
			else {
				em.merge(msgAddress);
			}
		}
		finally {
		}
	}

	public void insert(MessageAddress msgAddress) {
		try {
			em.persist(msgAddress);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}
