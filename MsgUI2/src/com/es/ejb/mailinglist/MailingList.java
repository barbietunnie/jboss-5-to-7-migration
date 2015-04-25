package com.es.ejb.mailinglist;

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

import jpa.exception.DataValidationException;
import jpa.exception.OutOfServiceException;
import jpa.exception.TemplateNotFoundException;
import jpa.service.maillist.MailingListBo;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

/**
 * Session Bean implementation class MailingList
 */
@Stateless(mappedName = "ejb/MailingList")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
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
    	mailingListBo = SpringUtil.getAppContext().getBean(MailingListBo.class);
    }

    @Override
	public int sendMail(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException {
		int mailsSent = mailingListBo.send(toAddr, variables, templateId);
		return mailsSent;
	}

    @Override
	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		int mailsSent = mailingListBo.broadcast(templateId);
		return mailsSent;
	}

    @Override
	public int broadcast(String templateId, String listId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		int mailsSent = mailingListBo.broadcast(templateId, listId);
		return mailsSent;
	}

}
