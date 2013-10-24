package jpa.service.msgout;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.constant.MailServerType;
import jpa.constant.StatusId;
import jpa.model.SmtpServer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("smtpServerService")
@Transactional(propagation=Propagation.REQUIRED)
public class SmtpServerService implements java.io.Serializable {
	private static final long serialVersionUID = 5535796998527412454L;

	static Logger logger = Logger.getLogger(SmtpServerService.class);
	
	@Autowired
	EntityManager em;

	public SmtpServer getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"SmtpServer t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			SmtpServer record = (SmtpServer) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public SmtpServer getByServerName(String serverName) throws NoResultException {
		String sql = 
				"select t " +
				" from " +
					" SmtpServer t where t.serverName=:serverName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("serverName", serverName);
			SmtpServer record = (SmtpServer) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public List<SmtpServer> getAll(boolean onlyActive, Boolean isSecure) {
		String sql = 
				"select t " +
					" from SmtpServer t ";
		if (onlyActive) {
			sql += " where t.statusId=:statusId ";
		}
		if (isSecure!=null) {
			if (sql.indexOf("where")>0) {
				sql += " and t.isUseSsl=:isUseSsl ";
			}
			else {
				sql += " where t.isUseSsl=:isUseSsl ";
			}
		}
		try {
			Query query = em.createQuery(sql);
			if (onlyActive) {
				query.setParameter("statusId", StatusId.ACTIVE.getValue());
			}
			if (isSecure!=null) {
				query.setParameter("isUseSsl", isSecure);
			}
			@SuppressWarnings("unchecked")
			List<SmtpServer> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<SmtpServer> getByServerType(MailServerType type, boolean onlyActive) {
		String sql = 
				"select t " +
					" from SmtpServer t " +
					" where t.serverType=:serverType ";
		if (onlyActive) {
			sql += " and t.statusId=:statusId ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("serverType", type.getValue());
			if (onlyActive) {
				query.setParameter("statusId", StatusId.ACTIVE.getValue());
			}
			@SuppressWarnings("unchecked")
			List<SmtpServer> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(SmtpServer var) {
		if (var == null) return;
		try {
			em.remove(var);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from SmtpServer t where t.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByServerName(String serverName) {
		String sql = 
				"delete from SmtpServer t where t.serverName=:serverName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("serverName", serverName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(SmtpServer var) {
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

	public void insert(SmtpServer var) {
		try {
			em.persist(var);
			em.flush(); // Not required, useful for seeing what is happening
		}
		finally {
		}
	}

}
