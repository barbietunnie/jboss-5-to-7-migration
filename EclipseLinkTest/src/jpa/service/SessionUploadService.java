package jpa.service;

import java.sql.Timestamp;
import java.util.Calendar;
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

import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;

@Component("sessionUploadService")
@Transactional(propagation=Propagation.REQUIRED)
public class SessionUploadService {
	static Logger logger = Logger.getLogger(SessionUploadService.class);
	
	@Autowired
	EntityManager em;

	public List<SessionUpload> getBySessionId(String sessionId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SessionUpload t where t.sessionUploadPK.sessionId = :sessionId");
			query.setParameter("sessionId", sessionId);
			@SuppressWarnings("unchecked")
			List<SessionUpload> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public SessionUpload getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SessionUpload t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			SessionUpload session = (SessionUpload) query.getSingleResult();
			em.lock(session, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return session;
		}
		finally {
		}
	}
	
	public SessionUpload getByPrimaryKey(SessionUploadPK pk) throws NoResultException {
		try {
			Query query = em.createQuery("select t from SessionUpload t where " +
					"t.sessionUploadPK.sessionId=:sessionId and t.sessionUploadPK.sessionSequence=:sessionSequence ");
			query.setParameter("sessionId", pk.getSessionId());
			query.setParameter("sessionSequence", pk.getSessionSequence());
			SessionUpload session = (SessionUpload) query.getSingleResult();
			em.lock(session, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return session;
		}
		finally {
		}
	}
	
	public List<SessionUpload> getByUserId(String userId) {
		try {
			Query query = em.createQuery("select t from SessionUpload t, UserData u where " +
					"t.userData=u and u.userId=:userId ");
			query.setParameter("userId", userId);
			@SuppressWarnings("unchecked")
			List<SessionUpload> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(SessionUpload session) {
		if (session==null) return;
		try {
			em.remove(session);
		}
		finally {
		}
	}

	public int deleteAll() {
		try {
			Query query = em.createQuery("delete from SessionUpload t ");
			int rows = query.executeUpdate();
			em.flush();
			return rows;
		}
		finally {
		}
	}

	public int deleteExpired(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes); // roll back time
		Timestamp rollback = new Timestamp(cal.getTimeInMillis());
		try {
			Query query = em.createQuery("delete from SessionUpload t where t.updtTime<:rollback");
			query.setParameter("rollback", rollback);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByUserId(String sessionId) {
		try {
			Query query = em.createQuery("delete from SessionUpload t where t.sessionUploadPK.sessionId=:sessionId");
			query.setParameter("sessionId", sessionId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(SessionUploadPK pk) {
		try {
			Query query = em.createQuery("delete from SessionUpload t where " +
					"t.sessionUploadPK.sessionId=:sessionId and t.sessionUploadPK.sessionSequence=:sessionSequence ");
			query.setParameter("sessionId", pk.getSessionId());
			query.setParameter("sessionSequence", pk.getSessionSequence());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from SessionUpload t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(SessionUpload session) {
		try {
			em.persist(session);
		}
		finally {
		}
	}
	
	public void insertLast(SessionUpload session) {
		String lastSeqSql = 
				"select max(t.sessionUploadPK.sessionSequence) from SessionUpload t " +
				"where t.sessionUploadPK.sessionId=:sessionId ";
		try {
			Query query = em.createQuery(lastSeqSql);
			query.setParameter("sessionId", session.getSessionUploadPK().getSessionId());
			Integer lastSeq = (Integer) query.getSingleResult();
			session.getSessionUploadPK().setSessionSequence(lastSeq + 1);
			em.persist(session);
		}
		finally {
		}
	}
	
	public void update(SessionUpload session) {
		try {
			if (em.contains(session)) {
				em.persist(session);
			}
			else {
				em.merge(session);
			}
		}
		finally {
		}
	}
	
}
