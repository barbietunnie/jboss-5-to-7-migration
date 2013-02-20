package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.MessageAddress;
import jpa.model.MessageAddressPK;

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

	public MessageAddress getByPrimaryKey(MessageAddressPK pk) throws NoResultException {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from MessageAddress t, MessageInbox mi where " +
					" mi=t.messageAddressPK.messageInbox and mi.rowId=:rowId " +
					" and t.addressSequence=:sequence ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("rowId", pk.getMessageInbox().getRowId());
				query.setParameter("sequence", pk.getAddressSequence());
				MessageAddress record = (MessageAddress) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<MessageAddress> getByMsgInboxId(int rowId) {
		String sql = 
				"select t " +
				"from MessageAddress t, MessageInbox mi " +
				" where mi=t.messageAddressPK.messageInbox and mi.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
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
			Query query = em.createNativeQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(MessageAddressPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Message_Address where " +
				" MessageInboxRowId = ?1 and addressSequence=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageInbox().getRowId());
			query.setParameter(2, pk.getAddressSequence());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByMsgInboxId(int rowId) {
		String sql = 
				"delete from MessageAddress t " +
				" where t.messageAddressPK.messageInbox.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
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
