package jpa.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.model.EmailVariable;
import jpa.util.JpaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailVariableService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class EmailVariableService implements java.io.Serializable {
	private static final long serialVersionUID = 2189513886746930320L;

	static Logger logger = Logger.getLogger(EmailVariableService.class);
	
	@Autowired
	EntityManager em;

	public EmailVariable getByVariableName(String variableName) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailVariable t where t.variableName = :variableName");
			query.setParameter("variableName", variableName);
			EmailVariable variable = (EmailVariable) query.getSingleResult();
			//em.lock(variable, LockModeType.OPTIMISTIC);
			return variable;
		}
		finally {
		}
	}
	
	public EmailVariable getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailVariable t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			EmailVariable variable = (EmailVariable) query.getSingleResult();
			//em.lock(variable, LockModeType.OPTIMISTIC);
			return variable;
		}
		finally {
		}
	}
	
	public List<EmailVariable> getAll() {
		try {
			Query query = em.createQuery("select t from EmailVariable t");
			@SuppressWarnings("unchecked")
			List<EmailVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<EmailVariable> getAllBuiltinVariables() {
		return getAllVariablesBy(true);
	}
	
	public List<EmailVariable> getAllCustomVariables() {
		return getAllVariablesBy(false);
	}
	
	private List<EmailVariable> getAllVariablesBy(boolean isBuiltin) {
		try {
			Query query = em.createQuery("select t from EmailVariable t where t.isBuiltin=:isBuiltin " +
					" order by t.rowId ");
			query.setParameter("isBuiltin", isBuiltin);
			@SuppressWarnings("unchecked")
			List<EmailVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public String getByQuery(String queryStr, int addrId) {
		if (Constants.DB_PRODNAME_DERBY.equalsIgnoreCase(JpaUtil.getDBProductName())) {
			// Derby, replace CONCAT function with concatenate operators
			 Pattern p = Pattern.compile("^(\\w{1,20} )(CONCAT\\(.*\\))(.*)$",
					 Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			 Matcher m = p.matcher(queryStr);
			 if (m.find() && m.groupCount()>=3) {
				 String concat = "";
				 for (int i=0; i<=m.groupCount(); i++) {
					 if (i==2) {
						 concat = StringUtils.removeStartIgnoreCase(m.group(i), "CONCAT");
						 concat = StringUtils.replace(concat, ",", "||");
					 }
				 }
				queryStr = m.group(1) + concat + m.group(3);
			 }
		}
		try {
			Query query = em.createNativeQuery(queryStr);
			query.setParameter(1, addrId);
			String result = (String) query.getSingleResult();
			return result;
		}
		catch (NoResultException e) {
			return null;
		}
		finally {
		}
	}
	
	public void delete(EmailVariable variable) {
		if (variable==null) return;
		try {
			em.remove(variable);
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		try {
			Query query = em.createQuery("delete from EmailVariable t where t.variableName=:variableName");
			query.setParameter("variableName", variableName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from EmailVariable t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(EmailVariable variable) {
		try {
			em.persist(variable);
		}
		finally {
		}
	}
	
	public void update(EmailVariable variable) {
		try {
			if (em.contains(variable)) {
				em.persist(variable);
			}
			else {
				em.merge(variable);
			}
		}
		finally {
		}
	}
	
}
