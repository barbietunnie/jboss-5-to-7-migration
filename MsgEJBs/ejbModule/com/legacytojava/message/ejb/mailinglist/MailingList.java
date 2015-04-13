package com.legacytojava.message.ejb.mailinglist;

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

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bo.mailinglist.MailingListBo;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;

/**
 * Session Bean implementation class MailingList
 */
@Stateless(mappedName = "ejb/MailingList")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(MailingListRemote.class)
@Local(MailingListLocal.class)
public class MailingList implements MailingListRemote, MailingListLocal {
	protected static final Logger logger = Logger.getLogger(MailingList.class);
	@Resource
	SessionContext context;
	private MailingListBo mailingListBo;
    /**
     * Default constructor. 
     */
    public MailingList() {
    	mailingListBo = (MailingListBo) SpringUtil.getAppContext().getBean("mailingListBo");
    }

	public int sendMail(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException {
		int mailsSent = mailingListBo.send(toAddr, variables, templateId);
		return mailsSent;
	}

	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		int mailsSent = mailingListBo.broadcast(templateId);
		return mailsSent;
	}

	public int broadcast(String templateId, String listId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		int mailsSent = mailingListBo.broadcast(templateId, listId);
		return mailsSent;
	}

	public int subscribe(String emailAddr, String listId) throws DataValidationException {
		int emailsSignedUp = mailingListBo.subscribe(emailAddr, listId);
		return emailsSignedUp;
	}

	public int unSubscribe(String emailAddr, String listId) throws DataValidationException {
		int emailsRemoved = mailingListBo.unSubscribe(emailAddr, listId);
		return emailsRemoved;
	}

	public int optInRequest(String emailAddr, String listId) throws DataValidationException {
		int emailsOptIned = mailingListBo.optInRequest(emailAddr, listId);
		return emailsOptIned;
	}

	public int optInConfirm(String emailAddr, String listId) throws DataValidationException {
		int emailsOptIned = mailingListBo.optInConfirm(emailAddr, listId);
		return emailsOptIned;
	}

	public int updateOpenCount(long emailAddrId, String listId) throws DataValidationException {
		int recsUpdated = mailingListBo.updateOpenCount(emailAddrId, listId);
		return recsUpdated;
	}

	public int updateClickCount(long emailAddrId, String listId) throws DataValidationException {
		int recsUpdated = mailingListBo.updateClickCount(emailAddrId, listId);
		return recsUpdated;
	}

	public int updateMsgOpenCount(long msgId) {
		int recsUpdated = mailingListBo.updateOpenCount(msgId, 1);
		return recsUpdated;
	}

	public int updateMsgClickCount(long msgId) {
		int recsUpdated = mailingListBo.updateClickCount(msgId, 1);
		return recsUpdated;
	}
}
