package jpa.service;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.UserData;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("userDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class UserDataService implements Serializable {
	private static final long serialVersionUID = 6193420138194185032L;

	static Logger logger = Logger.getLogger(UserDataService.class);
	
	@Autowired
	EntityManager em;

	public UserData getByUserId(String userId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from UserData t where t.userId = :userId");
			query.setParameter("userId", userId);
			UserData user = (UserData) query.getSingleResult();
			//em.lock(user, LockModeType.OPTIMISTIC);
			return user;
		}
		finally {
		}
	}
	
	public UserData getForLogin(String userId, String password) throws NoResultException {
		try {
			Query query = em.createQuery("select t from UserData t where t.userId = :userId and t.password=:password");
			query.setParameter("userId", userId);
			query.setParameter("password", password);
			UserData user = (UserData) query.getSingleResult();
			return user;
		}
		finally {
		}
	}
	
	public UserData getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from UserData t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			UserData user = (UserData) query.getSingleResult();
			//em.lock(user, LockModeType.OPTIMISTIC);
			return user;
		}
		finally {
		}
	}
	
	public List<UserData> getAll() {
		try {
			Query query = em.createQuery("select t from UserData t");
			@SuppressWarnings("unchecked")
			List<UserData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(UserData user) {
		if (user==null) return;
		try {
			em.remove(user);
		}
		finally {
		}
	}

	public int deleteByUserId(String userId) {
		try {
			Query query = em.createQuery("delete from UserData t where t.userId=:userId");
			query.setParameter("userId", userId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from UserData t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(UserData user) {
		try {
			em.persist(user);
		}
		finally {
		}
	}
	
	public void update(UserData user) {
		try {
			if (em.contains(user)) {
				em.persist(user);
			}
			else {
				em.merge(user);
			}
		}
		finally {
		}
	}

	public int update4Web(UserData user) {
		try {
			String sql = "update UserData t set " +
				"t.sessionId=:sessionId, " +
				"t.lastVisitTime=:lastVisitTime, " +
				"t.hits=:hits " +
				"where t.rowId=:rowId";
	
			Query query = em.createQuery(sql);
			query.setParameter("sessionId", user.getSessionId());
			query.setParameter("lastVisitTime", user.getLastVisitTime());
			query.setParameter("hits", user.getHits());
			query.setParameter("rowId", user.getRowId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

}
