package jpa.service;

import java.sql.Timestamp;
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
import jpa.model.TemplateData;
import jpa.model.TemplateDataPK;

@Component("templateDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class TemplateDataService {
	static Logger logger = Logger.getLogger(TemplateDataService.class);
	
	@Autowired
	EntityManager em;

	public TemplateData getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"TemplateData t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			TemplateData record = (TemplateData) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public TemplateData getByPrimaryKey(TemplateDataPK pk) throws NoResultException {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		String sql = 
			"select t " +
			"from " +
				"TemplateData t, ClientData c " +
				"where c=t.templateDataPK.clientData and c.clientId=:clientId " +
				"and t.templateDataPK.templateId=:templateId ";
		if (pk.getStartTime()!=null) {
			sql += " and t.templateDataPK.startTime=:starTtime ";
		}
		else {
			sql += " and t.templateDataPK.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("clientId", pk.getClientData().getClientId());
			query.setParameter("templateId", pk.getTemplateId());
			if (pk.getStartTime() != null) {
				query.setParameter("starTtime", pk.getStartTime());
			}
			TemplateData template = (TemplateData) query.getSingleResult();
			return template;
		}
		finally {
		}
	}

	public TemplateData getByBestMatch(TemplateDataPK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Timestamp(System.currentTimeMillis()));
		}
		String sql = 
				"select t " +
				"from " +
					"TemplateData t, ClientData c " +
					" where c=t.templateDataPK.clientData and c.clientId=:clientId " +
					" and t.templateDataPK.templateId=:templateId " +
					" and (t.templateDataPK.startTime<=:startTime or t.templateDataPK.startTime is null) " +
					" order by t.templateDataPK.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("templateId", pk.getTemplateId());
			query.setParameter("startTime", pk.getStartTime());
			query.setParameter("clientId", pk.getClientData().getClientId());
			@SuppressWarnings("unchecked")
			List<TemplateData> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public List<TemplateData> getByTemplateId(String templateId) {
		String sql = 
				"select t " +
				" from " +
					" TemplateData t, ClientData c " +
					" where c=t.templateDataPK.clientData and t.templateDataPK.templateId=:templateId " +
				" order by c.clientId, t.templateDataPK.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("templateId", templateId);
			@SuppressWarnings("unchecked")
			List<TemplateData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<TemplateData> getCurrentByClientId(String clientId) {
		String sql = 
				"select a.* " +
					" from template_data a " +
					" inner join ( " +
					"  select b.clientDataRowId as clientDataRowId, b.templateId as templateId, max(b.startTime) as maxTime " +
					"   from template_data b, Client_Data cd " +
					"   where b.statusId = ?1 and b.startTime<=?2 and b.clientDataRowId=cd.row_Id and cd.clientId=?3 " +
					"   group by b.clientDataRowId, b.templateId " +
					" ) as c " +
					"  on a.templateId=c.templateId and a.startTime=c.maxTime and a.clientDataRowId=c.clientDataRowId " +
					" order by a.row_id asc ";
		try {
			Query query = em.createNativeQuery(sql, TemplateData.class);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new Timestamp(System.currentTimeMillis()));
			query.setParameter(3, clientId);
			@SuppressWarnings("unchecked")
			List<TemplateData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(TemplateData template) {
		if (template == null) return;
		try {
			em.remove(template);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(TemplateDataPK pk) {
		if (pk.getClientData()==null) {
			throw new IllegalArgumentException("A ClientData instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Template_Data " +
				" where templateId=?1 and startTime=?2 " +
				" and clientDataRowId in " +
				" (select row_id from client_data cd where cd.clientId=?3)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getTemplateId());
			query.setParameter(2, pk.getStartTime());
			query.setParameter(3, pk.getClientData().getClientId());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByTemplateId(String templateId) {
		String sql = 
				"delete from TemplateData t where t.templateDataPK.templateId=:templateId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("templateId", templateId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByClientId(String clientId) {
		String sql = 
				"delete from template_data where clientDataRowId in " +
				" (select row_id from client_data cd where cd.clientId=?1)";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(TemplateData template) {
		try {
			if (em.contains(template)) {
				em.persist(template);
			}
			else {
				em.merge(template);
			}
		}
		finally {
		}
	}

	public void insert(TemplateData template) {
		try {
			em.persist(template);
			em.flush();
		}
		finally {
		}
	}

}
