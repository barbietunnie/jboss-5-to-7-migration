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

import jpa.model.RuleDataValue;

@Component("ruleDataValueService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataValueService {
	static Logger logger = Logger.getLogger(RuleDataValueService.class);
	
	@Autowired
	EntityManager em;

	public RuleDataValue getByPrimaryKey(String dataType, String dataValue) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleDataValue t where " +
					"t.dataType = :dataType and t.dataValue=:dataValue ");
			query.setParameter("dataType", dataType);
			query.setParameter("dataValue", dataValue);
			RuleDataValue dType = (RuleDataValue) query.getSingleResult();
			em.lock(dType, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return dType;
		}
		finally {
		}
	}
	
	public RuleDataValue getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleDataValue t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			RuleDataValue dataType = (RuleDataValue) query.getSingleResult();
			em.lock(dataType, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return dataType;
		}
		finally {
		}
	}
	
	public List<RuleDataValue> getByDataType(String dataType) {
		try {
			Query query = em.createQuery("select t from RuleDataValue t where " +
					"t.dataType = :dataType ");
			query.setParameter("dataType", dataType);
			@SuppressWarnings("unchecked")
			List<RuleDataValue> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<RuleDataValue> getAll() {
		try {
			Query query = em.createQuery("select t from RuleDataValue t");
			@SuppressWarnings("unchecked")
			List<RuleDataValue> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(RuleDataValue dataType) {
		if (dataType==null) return;
		try {
			em.remove(dataType);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(String dataType, String dataValue) {
		try {
			Query query = em.createQuery("delete from RuleDataValue t where " +
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
			Query query = em.createQuery("delete from RuleDataValue t where t.dataType=:dataType");
			query.setParameter("dataType", dataType);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from RuleDataValue t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleDataValue dataType) {
		try {
			em.persist(dataType);
		}
		finally {
		}
	}
	
	public void update(RuleDataValue dataType) {
		try {
			if (em.contains(dataType)) {
				em.persist(dataType);
			}
			else {
				em.merge(dataType);
			}
		}
		finally {
		}
	}
	
}
