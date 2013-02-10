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

import jpa.model.MsgDataName;

@Component("MsgDataNameService")
@Transactional(propagation=Propagation.REQUIRED)
public class MsgDataNameService {
	static Logger logger = Logger.getLogger(MsgDataNameService.class);
	
	@Autowired
	EntityManager em;

	public MsgDataName getByPrimaryKey(String dataType, String dataValue) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MsgDataName t where " +
					"t.dataType = :dataType and t.dataValue=:dataValue ");
			query.setParameter("dataType", dataType);
			query.setParameter("dataValue", dataValue);
			MsgDataName dataName = (MsgDataName) query.getSingleResult();
			em.lock(dataName, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return dataName;
		}
		finally {
		}
	}
	
	public MsgDataName getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from MsgDataName t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			MsgDataName dataName = (MsgDataName) query.getSingleResult();
			em.lock(dataName, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return dataName;
		}
		finally {
		}
	}
	
	public List<MsgDataName> getByDataType(String dataType) {
		try {
			Query query = em.createQuery("select t from MsgDataName t where " +
					"t.dataType = :dataType ");
			query.setParameter("dataType", dataType);
			@SuppressWarnings("unchecked")
			List<MsgDataName> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<MsgDataName> getAll() {
		try {
			Query query = em.createQuery("select t from MsgDataName t");
			@SuppressWarnings("unchecked")
			List<MsgDataName> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(MsgDataName dataName) {
		if (dataName==null) return;
		try {
			em.remove(dataName);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(String dataType, String dataValue) {
		try {
			Query query = em.createQuery("delete from MsgDataName t where " +
					"t.dataType=:dataType and t.dataValue=:dataValue ");
			query.setParameter("dataType", dataType);
			query.setParameter("dataValue", dataValue);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByDataType(String dataType) {
		try {
			Query query = em.createQuery("delete from MsgDataName t where t.dataType=:dataType");
			query.setParameter("dataType", dataType);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from MsgDataName t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MsgDataName dataName) {
		try {
			em.persist(dataName);
		}
		finally {
		}
	}
	
	public void update(MsgDataName dataName) {
		try {
			if (em.contains(dataName)) {
				em.persist(dataName);
			}
			else {
				em.merge(dataName);
			}
		}
		finally {
		}
	}
	
}
