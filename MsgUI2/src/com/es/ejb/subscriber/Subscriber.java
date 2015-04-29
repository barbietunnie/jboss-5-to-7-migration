package com.es.ejb.subscriber;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.persistence.NoResultException;
import javax.sql.DataSource;

import jpa.model.EmailAddress;
import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriberDataService;
import jpa.service.common.SubscriptionService;
import jpa.util.SpringUtil;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;

import com.es.tomee.util.TomeeCtxUtil;

/**
 * Session Bean implementation class Subscriber
 */
@Stateless (name="subscriber", mappedName = "ejb/Subscriber")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SubscriberRemote.class)
@Local(SubscriberLocal.class)
@LocalBean
public class Subscriber implements SubscriberRemote, SubscriberLocal {
	protected static final Logger logger = Logger.getLogger(Subscriber.class);
	@Resource
	SessionContext context;
	private SubscriberDataService subscriberDao;
	private SubscriptionService subscriptionBo;
	private EmailAddressService emailAddrDao;

	/**
     * Default constructor. 
     */
    public Subscriber() {
		subscriberDao = SpringUtil.getAppContext().getBean(SubscriberDataService.class);
		subscriptionBo = SpringUtil.getAppContext().getBean(SubscriptionService.class);
		emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressService.class);
    }

    public void getResources() {
		try {
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/msgdb_pool");
			logger.info("in EJB - msgdb_pool 1: " + StringUtil.prettyPrint(dataSource));
			TomeeCtxUtil.listContext(initialContext, "java:comp");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		
		if (context != null) {
			// EJBContext will prepend "java:comp/env/" to the lookup name.
			DataSource dataSource = (DataSource) context.lookup("msgdb_pool");
			logger.info("in EJB - msgdb_pool 2: " + StringUtil.prettyPrint(dataSource));
		}
    }
	
    
    public List<SubscriberData> getAllSubscribers() {
    	return subscriberDao.getAll();
    }
    
    public SubscriberData getSubscriberById(String subrId) {
    	try {
    		SubscriberData vo = subscriberDao.getBySubscriberId(subrId);
    		return vo;
    	}
    	catch (NoResultException e) {
			return null;
		}
	}

	public SubscriberData getSubscriberByEmailAddress(String emailAddr) {
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(emailAddr);
		try {
			SubscriberData sbsrVo = subscriberDao.getByEmailAddress(emailAddrVo.getAddress());
			return sbsrVo;
		}
		catch (NoResultException e) {
			return null;
		}
	}

	public Subscription subscribe(String emailAddr, String listId) {
		Subscription emailAdded = subscriptionBo.subscribe(emailAddr, listId);
		return emailAdded;
	}

	public Subscription unSubscriber(String emailAddr, String listId) {
		Subscription emailRemoved = subscriptionBo.unsubscribe(emailAddr, listId);
		return emailRemoved;
	}

	public void insertSubscriber(SubscriberData vo) {
		subscriberDao.insert(vo);
	}

	public void updateSubscriber(SubscriberData vo) {
		subscriberDao.update(vo);
	}

	public void deleteSubscriber(SubscriberData vo) {
		subscriberDao.delete(vo);
	}
	
	public Subscription optInRequest(String emailAddr, String listId) {
		Subscription emailOptIned = subscriptionBo.optInRequest(emailAddr, listId);
		return emailOptIned;
	}

	public Subscription optInConfirm(String emailAddr, String listId) {
		Subscription emailOptIned = subscriptionBo.optInConfirm(emailAddr, listId);
		return emailOptIned;
	}

	@Override
	public Subscription addToList(String sbsrEmailAddr, String listEmailAddr) {
		Subscription sub = subscriptionBo.addToList(sbsrEmailAddr, listEmailAddr);
		return sub;
	}

	@Override
	public Subscription removeFromList(String sbsrEmailAddr, String listEmailAddr) {
		Subscription sub = subscriptionBo.removeFromList(sbsrEmailAddr, listEmailAddr);
		return sub;
	}
}
