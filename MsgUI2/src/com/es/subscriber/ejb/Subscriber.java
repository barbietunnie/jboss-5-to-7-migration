package com.es.subscriber.ejb;

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
import javax.sql.DataSource;

import jpa.exception.DataValidationException;
import jpa.model.EmailAddress;
import jpa.model.SubscriberData;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriberDataService;
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
	private EmailAddressService emailAddrDao;

	/**
     * Default constructor. 
     */
    public Subscriber() {
		subscriberDao = SpringUtil.getAppContext().getBean(SubscriberDataService.class);
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
    
    public SubscriberData getSubscriberById(String subrId) throws DataValidationException {
		SubscriberData vo = subscriberDao.getBySubscriberId(subrId);
		return vo;
	}

	public SubscriberData getSubscriberByEmailAddress(String emailAddr) {
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(emailAddr);
		SubscriberData customerVo = subscriberDao.getByEmailAddress(emailAddrVo.getAddress());
		return customerVo;
	}

	public void insertSubscriber(SubscriberData vo) throws DataValidationException {
		subscriberDao.insert(vo);
	}

	public void updateSubscriber(SubscriberData vo) throws DataValidationException {
		subscriberDao.update(vo);
	}

	public void deleteSubscriber(SubscriberData vo) throws DataValidationException {
		subscriberDao.delete(vo);
	}
}
