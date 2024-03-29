package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.rule.RuleDataValue;
import jpa.model.rule.RuleDataValuePK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDataValueService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataValueService implements java.io.Serializable {
	private static final long serialVersionUID = -4091890320222391000L;

	static Logger logger = Logger.getLogger(RuleDataValueService.class);
	
	@Autowired
	EntityManager em;

	public RuleDataValue getByPrimaryKey(RuleDataValuePK pk) throws NoResultException {
		if (pk.getRuleDataType()==null) {
			throw new IllegalArgumentException("A RuleDataType instance must be provided in Primary Key object.");
		}
		try {
			Query query = em.createQuery("select t from RuleDataValue t where " +
					"t.ruleDataValuePK.ruleDataType.dataType = :dataType " +
					"and t.ruleDataValuePK.dataValue=:dataValue ");
			query.setParameter("dataType", pk.getRuleDataType().getDataType());
			query.setParameter("dataValue", pk.getDataValue());
			RuleDataValue dType = (RuleDataValue) query.getSingleResult();
			//em.lock(dType, LockModeType.OPTIMISTIC);
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
			//em.lock(dataType, LockModeType.OPTIMISTIC);
			return dataType;
		}
		finally {
		}
	}
	
	public List<RuleDataValue> getByDataType(String dataType) {
		try {
			Query query = em.createQuery("select t from RuleDataValue t where " +
					"t.ruleDataValuePK.ruleDataType.dataType = :dataType ");
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

	/*
	 * @deprecated - works with EclipseLink, but not Hibernate (causes MySQLSyntaxErrorException)
	 */
	public int deleteByPrimaryKey_v0(RuleDataValuePK pk) {
		if (pk.getRuleDataType()==null) {
			throw new IllegalArgumentException("A RuleDataType instance must be provided in Primary Key object.");
		}
		try {
			Query query = em.createQuery("delete from RuleDataValue t where " +
					"t.ruleDataValuePK.ruleDataType.dataType=:dataType " +
					"and t.ruleDataValuePK.dataValue=:dataValue ");
			query.setParameter("dataType", pk.getRuleDataType().getDataType());
			query.setParameter("dataValue", pk.getDataValue());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(RuleDataValuePK pk) {
		if (pk.getRuleDataType()==null) {
			throw new IllegalArgumentException("A RuleDataType instance must be provided in Primary Key object.");
		}
		try {
			Query query = em.createNativeQuery("delete from Rule_Data_Value where " +
					"RuleDataTypeRowId in (select Row_Id from Rule_Data_Type where DataType = ?) " +
					"and DataValue=? ");
			query.setParameter(1, pk.getRuleDataType().getDataType());
			query.setParameter(2, pk.getDataValue());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByDataType(String dataType) {
		try {
			Query query = em.createNativeQuery("delete from Rule_Data_Value where " +
					"RuleDataTypeRowId in (select Row_Id from Rule_Data_Type where DataType = ?)");
			query.setParameter(1, dataType);
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
