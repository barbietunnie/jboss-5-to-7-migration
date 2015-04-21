package com.es.ejb.idtokens;

import java.util.ArrayList;
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
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import jpa.service.common.IdTokensService;
import jpa.util.SpringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.es.tomee.util.TomeeCtxUtil;

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

@WebService (portName = "IdTokens", serviceName = "IdTokensService", targetNamespace = "http://com.es.ws.idtokens/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class IdTokens implements IdTokensRemote, IdTokensLocal, IdTokensWs {
	protected static final Logger logger = Logger.getLogger(IdTokens.class);
	@Resource
	SessionContext context;
	private IdTokensService idTokensDao;
    /**
     * Default constructor. 
     */
    public IdTokens() {
    	idTokensDao = SpringUtil.getAppContext().getBean(IdTokensService.class);
    	TomeeCtxUtil.registerBeanUtilsConverters();
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
    
    @WebMethod
    //@AccessTimeout(0)
    @AccessTimeout(value = 5, unit = TimeUnit.SECONDS)
	public IdTokensVo findBySenderId(String senderId) {
		jpa.model.IdTokens idTokens = idTokensDao.getBySenderId(senderId);
		IdTokensVo idTokensVo = new IdTokensVo();
		try {
			BeanUtils.copyProperties(idTokensVo, idTokens);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		return idTokensVo;
	}

    @WebMethod
    @AccessTimeout(value = 10, unit = TimeUnit.SECONDS)
	public List<IdTokensVo> findAll() {
		List<jpa.model.IdTokens> list = idTokensDao.getAll();
		List<IdTokensVo> volist = new ArrayList<IdTokensVo>();
		for (jpa.model.IdTokens idTokens : list) {
			IdTokensVo idTokensVo = new IdTokensVo();
			try {
				BeanUtils.copyProperties(idTokensVo, idTokens);
				idTokensVo.setSenderId(idTokens.getSenderData().getSenderId());
				volist.add(idTokensVo);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
		}
		return volist;
	}
}
