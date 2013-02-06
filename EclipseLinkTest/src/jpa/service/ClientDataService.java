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

import jpa.constant.Constants;
import jpa.model.ClientData;
import jpa.util.StringUtil;

@Component("clientDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class ClientDataService {
	static Logger logger = Logger.getLogger(ClientDataService.class);
	
	@Autowired
	EntityManager em;

	public ClientData getByClientId(String clientId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ClientData t where t.clientId = :clientId");
			query.setParameter("clientId", clientId);
			ClientData client = (ClientData) query.getSingleResult();
			em.lock(client, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return client;
		}
		finally {
		}
	}
	
	public ClientData getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ClientData t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			ClientData client = (ClientData) query.getSingleResult();
			em.lock(client, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return client;
		}
		finally {
		}
	}
	
	public ClientData getByDomainName(String domainName) throws NoResultException {
		try {
			Query query = em.createQuery("select t from ClientData t where t.domainName = :domainName");
			query.setParameter("domainName", domainName);
			ClientData clients = (ClientData) query.getSingleResult();
			return clients;
		}
		finally {
		}
	}

	public List<ClientData> getAll() {
		try {
			Query query = em.createQuery("select t from ClientData t");
			@SuppressWarnings("unchecked")
			List<ClientData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public String getSystemId() throws NoResultException {
		try {
			Query query = em.createQuery("select t.systemId from ClientData t where t.clientId = :clientId");
			query.setParameter("clientId", Constants.DEFAULT_CLIENTID);
			String systemId = (String) query.getSingleResult();
			return systemId;
		}
		finally {
		}		
	}

	public String getSystemKey() throws NoResultException {
		try {
			Query query = em.createQuery("select t.systemKey from ClientData t where t.clientId = :clientId");
			query.setParameter("clientId", Constants.DEFAULT_CLIENTID);
			String systemKey = (String) query.getSingleResult();
			return systemKey;
		}
		finally {
		}		
	}

	public void delete(ClientData client) {
		if (client==null) return;
		try {
			em.remove(client);
		}
		finally {
		}
	}

	public int deleteByClientId(String clientId) {
		try {
			Query query = em.createQuery("delete from ClientData t where t.clientId=:clientId");
			query.setParameter("clientId", clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from ClientData t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public void insert(ClientData client) {
		try {
			validateClient(client);
			em.persist(client);
			reloadFlagsService.updateClientReloadFlag();
		}
		finally {
		}
	}
	
	public void update(ClientData client) {
		try {
			insert(client);
		}
		finally {
		}
	}
	
	private void validateClient(ClientData client) {
		if (client.isUseTestAddr()) {
			if (StringUtil.isEmpty(client.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (client.isVerpEnabled()) {
			if (StringUtil.isEmpty(client.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtil.isEmpty(client.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}

}
