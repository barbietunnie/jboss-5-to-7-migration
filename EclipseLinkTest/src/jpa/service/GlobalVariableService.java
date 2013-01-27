package jpa.service;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.GlobalVariable;

@Component("globalVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableService {
	static Logger logger = Logger.getLogger(GlobalVariableService.class);
	
	@Autowired
	EntityManager em;

	public GlobalVariable getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"GlobalVariable t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			GlobalVariable record = (GlobalVariable) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	/**
	 * find the record by variable name and start time.
	 * @param variableName
	 * @param startTime
	 * @return record found or null
	 */
	public GlobalVariable getByPrimaryKey(String variableName, Date startTime) {
		String sql = 
			"select t " +
			"from " +
				"GlobalVariable t where t.variableName=:variableName";
		if (startTime!=null) {
			sql += " and t.startTime=:startTime ";
		}
		else {
			sql += " and t.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			if (startTime != null) {
				query.setParameter("startTime", startTime);
			}
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				GlobalVariable record = list.get(0);
				return record;
			}
			return null;
		}
		finally {
		}
	}

	/**
	 * find the best matched record by variable name and start time.
	 * @param variableName
	 * @param startTime
	 * @return the record best matched or null if not found
	 */
	public GlobalVariable getByBestMatch(String variableName, Date startTime) {
		if (startTime!=null) {
			startTime = new Date(System.currentTimeMillis());
		}
		String sql = 
				"select t " +
				"from " +
					"GlobalVariable t where t.variableName=:variableName " +
					" and (t.startTime<=:startTime or t.startTime is null) " +
					" order by t.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			query.setParameter("startTime", startTime);
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				GlobalVariable record = list.get(0);
				return record;
			}
			return null;
		}
		finally {
		}
	}
	public List<GlobalVariable> getByVariableName(String variableName) {
		String sql = 
				"select t " +
				" from " +
					" GlobalVariable t where t.variableName=:variableName " +
				" order by t.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<GlobalVariable> getCurrent() {
		String sql = 
				"select a.* " +
					" from GlobalVariable a " +
					" inner join ( " +
					"  select b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from GlobalVariable b " +
					"   where b.statusId = ?1 and b.startTime<=?2 " +
					"   group by b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime " +
					" order by a.variableName asc ";
		try {
			Query query = em.createNativeQuery(sql, GlobalVariable.class);
			query.setParameter(1, StatusId.ACTIVE.getCode());
			query.setParameter(2, new Date(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<GlobalVariable> getByStatusId(String statusId) {
		String sql = 
				"select t " +
					" from GlobalVariable t " +
					" where t.statusId = :statusId and t.startTime<=:startTime" +
					" order by t.variableName asc, t.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("statusId", statusId);
			query.setParameter("startTime", new Date(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.getResultList();
			List<GlobalVariable> list2 = new ArrayList<GlobalVariable>();
			String varName = null;
			for (Iterator<GlobalVariable> it=list.iterator(); it.hasNext(); ) {
				GlobalVariable var = it.next();
				if (!var.getVariableName().equals(varName)) {
					list2.add(var);
					varName = var.getVariableName();
				}
			}
			return list2;
		}
		finally {
		}
	}

	public void delete(GlobalVariable var) {
		if (var == null) return;
		try {
			em.remove(var);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(String variableName, Date startTime) {
		String sql = 
				"delete from GlobalVariable t where t.variableName=:variableName and t.startTime=:startTime ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			query.setParameter("startTime", startTime);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from GlobalVariable t where t.variableName=:variableName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(GlobalVariable var) {
		insert(var);
	}

	public void insert(GlobalVariable var) {
		try {
			em.persist(var);
			em.flush(); // Not required, useful for seeing what is happening
		}
		finally {
		}
	}

}
