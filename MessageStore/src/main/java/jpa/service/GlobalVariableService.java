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
import jpa.model.GlobalVariablePK;

@Component("globalVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableService implements java.io.Serializable {
	private static final long serialVersionUID = 6628495287347386534L;

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
	public GlobalVariable getByPrimaryKey(GlobalVariablePK pk) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"GlobalVariable t where t.globalVariablePK.variableName=:variableName";
		if (pk.getStartTime()!=null) {
			sql += " and t.globalVariablePK.startTime=:startTime ";
		}
		else {
			sql += " and t.globalVariablePK.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", pk.getVariableName());
			if (pk.getStartTime() != null) {
				query.setParameter("startTime", pk.getStartTime());
			}
			GlobalVariable var = (GlobalVariable) query.getSingleResult();
			return var;
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
	public GlobalVariable getByBestMatch(GlobalVariablePK pk) {
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Date(System.currentTimeMillis()));
		}
		String sql = 
				"select t " +
				"from " +
					"GlobalVariable t where t.globalVariablePK.variableName=:variableName " +
					" and (t.globalVariablePK.startTime<=:startTime or t.globalVariablePK.startTime is null) " +
					" order by t.globalVariablePK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", pk.getVariableName());
			query.setParameter("startTime", pk.getStartTime());
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
					" GlobalVariable t where t.globalVariablePK.variableName=:variableName " +
				" order by t.globalVariablePK.startTime asc ";
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
					" from Global_Variable a " +
					" inner join ( " +
					"  select b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from Global_Variable b " +
					"   where b.statusId = ?1 and b.startTime<=?2 " +
					"   group by b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime " +
					" order by a.variableName asc ";
		try {
			Query query = em.createNativeQuery(sql, GlobalVariable.class);
			query.setParameter(1, StatusId.ACTIVE.getValue());
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
					" where t.statusId = :statusId and t.globalVariablePK.startTime<=:startTime" +
					" order by t.globalVariablePK.variableName asc, t.globalVariablePK.startTime desc ";
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
				if (!var.getGlobalVariablePK().getVariableName().equals(varName)) {
					list2.add(var);
					varName = var.getGlobalVariablePK().getVariableName();
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

	public int deleteByPrimaryKey(GlobalVariablePK pk) {
		String sql = 
				"delete from GlobalVariable t where t.globalVariablePK.variableName=:variableName " +
				" and t.globalVariablePK.startTime=:startTime ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", pk.getVariableName());
			query.setParameter("startTime", pk.getStartTime());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from GlobalVariable t where t.globalVariablePK.variableName=:variableName ";
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
		try {
			if (em.contains(var)) {
				em.persist(var);
			}
			else {
				em.merge(var);
			}
		}
		finally {
		}
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
