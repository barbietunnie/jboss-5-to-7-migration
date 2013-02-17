package com.legacytojava.message.ejb.idtokens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;

import org.jboss.logging.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.idtokens.IdTokensDao;
import com.legacytojava.message.jpa.service.IdTokensService;
import com.legacytojava.message.vo.IdTokensVo;

/**
 * Session Bean implementation class IdTokens
 */
@Stateless(mappedName = "ejb/IdTokens")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(IdTokensRemote.class)
@Local(IdTokensLocal.class)
public class IdTokens implements IdTokensRemote, IdTokensLocal {
	protected static final Logger logger = Logger.getLogger(IdTokens.class);
	@Resource
	SessionContext context;
	private IdTokensDao idTokensDao;
	private IdTokensService tokenService;
	private static Map<String, IdTokensVo> idTokensCache = new HashMap<String, IdTokensVo>();
    /**
     * Default constructor. 
     */
    public IdTokens() {
    	idTokensDao = (IdTokensDao)SpringUtil.getAppContext().getBean("idTokensDao");
    	tokenService = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");
    }

	public IdTokensVo findByClientId(String senderId) {
		if (!idTokensCache.containsKey(senderId)) {
			IdTokensVo idTokensVo = idTokensDao.getByClientId(senderId);
			idTokensCache.put(senderId, idTokensVo);
		}
		return (IdTokensVo)idTokensCache.get(senderId);
	}

	public com.legacytojava.message.jpa.model.IdTokens getByClientId(String senderId) {
		try {
			return tokenService.getByClientId(senderId);
		}
		catch (NoResultException e) {
			return null;
		}
	}

	public List<IdTokensVo> findAll() {
		List<IdTokensVo> list = idTokensDao.getAll();
		return list;
	}

	public int insert(IdTokensVo idTokensVo) {
		int rowsInserted = idTokensDao.insert(idTokensVo);
		if (rowsInserted>0)
			idTokensCache.remove(idTokensVo.getClientId());
		return rowsInserted;
	}

	public int update(IdTokensVo idTokensVo) {
		int rowsUpdated = idTokensDao.update(idTokensVo);
		if (rowsUpdated>0)
			idTokensCache.remove(idTokensVo.getClientId());
		return rowsUpdated;
	}

	public int delete(String senderId) {
		int rowsDeleted = idTokensDao.delete(senderId);
		if (rowsDeleted>0)
			idTokensCache.remove(senderId);
		return rowsDeleted;
	}
}
