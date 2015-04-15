package com.es.idtokens.ejb;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import jpa.service.common.IdTokensService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

/**
 * Session Bean implementation class IdTokens
 */
@Singleton(name = "IdTokens", mappedName = "ejb/IdTokens")
@Lock(LockType.READ) // allow concurrent access to the methods
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(IdTokensRemote.class)
@Local(IdTokensLocal.class)
public class IdTokens implements IdTokensRemote, IdTokensLocal {
	protected static final Logger logger = Logger.getLogger(IdTokens.class);
	@Resource
	SessionContext context;
	private IdTokensService idTokensDao;
    /**
     * Default constructor. 
     */
    public IdTokens() {
    	idTokensDao = SpringUtil.getAppContext().getBean(IdTokensService.class);
    }

    @Asynchronous
    @AccessTimeout(-1)
    public Future<?> stayBusy(CountDownLatch ready) {
    	long start = System.currentTimeMillis();
    	try {
    		//ready.await();
        	TimeUnit.MILLISECONDS.sleep(100);
    	}
    	catch (InterruptedException e) {
    		Thread.interrupted();
    	}
    	return new AsyncResult<Long>(System.currentTimeMillis() - start);
    }
    
    //@AccessTimeout(0)
    @AccessTimeout(value = 5, unit = TimeUnit.SECONDS)
	public jpa.model.IdTokens findBySenderId(String senderId) {
		jpa.model.IdTokens idTokensVo = idTokensDao.getBySenderId(senderId);
		return idTokensVo;
	}

    @AccessTimeout(value = 10, unit = TimeUnit.SECONDS)
	public List<jpa.model.IdTokens> findAll() {
		List<jpa.model.IdTokens> list = idTokensDao.getAll();
		return list;
	}

    @AccessTimeout(-1)
	public void insert(jpa.model.IdTokens idTokensVo) {
		idTokensDao.insert(idTokensVo);
	}

    @AccessTimeout(-1)
	public void update(jpa.model.IdTokens idTokensVo) {
		idTokensDao.update(idTokensVo);
	}

	@AccessTimeout(-1)
	public void delete(String senderId) {
		idTokensDao.deleteBySenderId(senderId);
	}
}
