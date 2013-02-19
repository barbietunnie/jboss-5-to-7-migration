package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.MessageSource;

@Component("messageSourceService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageSourceService {
	static Logger logger = Logger.getLogger(MessageSourceService.class);
	
	@Autowired
	EntityManager em;

	public MessageSource getByMsgSourceId(String sourceId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MessageSource t where t.msgSourceId = :sourceId");
			query.setParameter("sourceId", sourceId);
			MessageSource source = (MessageSource) query.getSingleResult();
			em.lock(source, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return source;
		}
		finally {
		}
	}
	
	public MessageSource getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MessageSource t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			MessageSource source = (MessageSource) query.getSingleResult();
			em.lock(source, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return source;
		}
		finally {
		}
	}
	
	public List<MessageSource> getByFromAddress(String address) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MessageSource t, EmailAddr ea " +
					" where ea=t.fromAddress and ea.address = :address ");
			query.setParameter("address", address);
			@SuppressWarnings("unchecked")
			List<MessageSource> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<MessageSource> getAll() {
		try {
			Query query = em.createQuery("select t from MessageSource t");
			@SuppressWarnings("unchecked")
			List<MessageSource> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(MessageSource source) {
		if (source==null) return;
		try {
			em.remove(source);
		}
		finally {
		}
	}

	public int deleteByMsgSourceId(String sourceId) {
		try {
			Query query = em.createQuery("delete from MessageSource t where t.msgSourceId=:sourceId");
			query.setParameter("sourceId", sourceId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from MessageSource t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MessageSource source) {
		try {
			em.persist(source);
		}
		finally {
		}
	}
	
	public void update(MessageSource source) {
		try {
			insert(source);
		}
		finally {
		}
	}

}
