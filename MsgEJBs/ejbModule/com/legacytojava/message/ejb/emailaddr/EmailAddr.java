package com.legacytojava.message.ejb.emailaddr;

import java.sql.Timestamp;
import java.util.List;

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
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.logging.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

/**
 * Session Bean implementation class EmailAddr
 */
@Stateless(mappedName = "ejb/EmailAddr")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(EmailAddrRemote.class)
@Local(EmailAddrLocal.class)

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class EmailAddr implements EmailAddrRemote, EmailAddrLocal {
	protected static final Logger logger = Logger.getLogger(EmailAddr.class);
	@PersistenceContext(unitName = "EntityPersistence")
	private EntityManager emgr;

	@Resource
	SessionContext context;

	private EmailAddrDao emailAddrDao;
    /**
     * Default constructor. 
     */
    public EmailAddr() {
    	/*
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("spring-server2-config.xml");
		if (isInfoEnabled)
			logger.info("Spring config URL: "+url);
		XmlBeanFactory factory = new XmlBeanFactory(new UrlResource(url));
		*/
		emailAddrDao = (EmailAddrDao)SpringUtil.getAppContext().getBean("emailAddrDao");
    }

    @WebMethod
	public EmailAddrVo findByAddrId(int addrId) {
		EmailAddrVo emailAddrVo = emailAddrDao.getByAddrId(addrId);
		return emailAddrVo;
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ)
	public EmailAddrVo findByAddress(String address) {
		String emailAddress = EmailAddrUtil.removeDisplayName(address);
		//return emailAddrDao.findByAddress(address);
		String queryString = 
				"select ea.EmailAddrId," +
						"ea.EmailAddr," +
						"ea.OrigEmailAddr," +
						"ea.StatusId," +
						"ea.StatusChangeTime," +
						"ea.StatusChangeUserId," +
						"ea.BounceCount," +
						"ea.LastBounceTime," +
						"ea.LastSentTime," +
						"ea.LastRcptTime," +
						"ea.AcceptHtml," +
						"ea.UpdtTime," +
						"ea.UpdtUserId " +
				" from EmailAddr ea where ea.emailAddr = :emailAddr ";
		Query query = emgr.createNativeQuery(queryString, EmailAddrEntity.class);
		//query = emgr.createNamedQuery("getByAddress");
		query.setParameter("emailAddr", emailAddress);
		List<?> list = (List<?>) query.getResultList();
		EmailAddrEntity entity = null;
		if (list == null || list.isEmpty()) {
			entity = new EmailAddrEntity();
			Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
			EmailAddrPK pk = new EmailAddrPK(emailAddress);
			entity.setEmailAddrPK(pk);
			entity.setEmailAddr(pk.getEmailAddr());
			entity.setOrigEmailAddr(address);
			entity.setBounceCount(0);
			entity.setStatusId(StatusIdCode.ACTIVE);
			entity.setStatusChangeTime(updtTime);
			entity.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
			entity.setAcceptHtml(Constants.YES_CODE);
			entity.setUpdtTime(updtTime);
			entity.setUpdtUserId(Constants.DEFAULT_USER_ID);
			emgr.persist(entity);
		}
		else {
			entity = (EmailAddrEntity)list.get(0);
		}
		EmailAddrVo emailAddrVo = new EmailAddrVo();
		BeanUtils.copyProperties(entity, emailAddrVo);
		return emailAddrVo;
	}

	public int insert(EmailAddrVo emailAddrVo) {
		int rowsInserted = emailAddrDao.insert(emailAddrVo);
		return rowsInserted;
	}

	public int update(EmailAddrVo emailAddrVo) {
		int rowsUpdated = emailAddrDao.update(emailAddrVo);
		return rowsUpdated;
	}

	public int deleteByAddrId(int addrId) {
		int rowsDeleted = emailAddrDao.deleteByAddrId(addrId);
		return rowsDeleted;
	}

	public int deleteByAddress(String address) {
		int rowsDeleted = emailAddrDao.deleteByAddress(address);
		return rowsDeleted;
	}
}
