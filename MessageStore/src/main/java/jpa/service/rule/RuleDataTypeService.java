package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.rule.RuleDataType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDataTypeService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataTypeService implements java.io.Serializable {
	private static final long serialVersionUID = 7713274333671397066L;

	static Logger logger = Logger.getLogger(RuleDataTypeService.class);
	
	@Autowired
	EntityManager em;

	public RuleDataType getByDataType(String dataType) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleDataType t where " +
					"t.dataType = :dataType ");
			query.setParameter("dataType", dataType);
			RuleDataType dataName = (RuleDataType) query.getSingleResult();
			//em.lock(dataName, LockModeType.OPTIMISTIC);
			return dataName;
		}
		finally {
		}
	}
	
	public RuleDataType getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from RuleDataType t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			RuleDataType dataName = (RuleDataType) query.getSingleResult();
			//em.lock(dataName, LockModeType.OPTIMISTIC);
			return dataName;
		}
		finally {
		}
	}
	
	public List<RuleDataType> getAll() {
		try {
			Query query = em.createQuery("select t from RuleDataType t");
			@SuppressWarnings("unchecked")
			List<RuleDataType> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(RuleDataType dataName) {
		if (dataName==null) return;
		try {
			em.remove(dataName);
		}
		finally {
		}
	}

	public int deleteByDataType(String dataType) {
		try {
			Query query = em.createQuery("delete from RuleDataType t where t.dataType=:dataType");
			query.setParameter("dataType", dataType);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from RuleDataType t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(RuleDataType dataName) {
		try {
			em.persist(dataName);
		}
		finally {
		}
	}
	
	public void update(RuleDataType dataName) {
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
	
	public List<String> getDataTypeList() {
		String sql = 
			"select distinct(DataType) as dataType " +
			"from " +
				"Rule_Data_Type " +
			" order by DataType asc ";
		
		Query query = em.createNativeQuery(sql);
		@SuppressWarnings("unchecked")
		List<String> list = query.getResultList();
		return list;
	}
	
}
