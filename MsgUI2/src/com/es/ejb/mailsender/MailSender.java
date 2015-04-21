package com.es.ejb.mailsender;

import java.io.IOException;

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

import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class MailSender
 */
@Stateless(name="MailSender")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Local(MailSenderLocal.class)
@Remote(MailSenderRemote.class)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
public class MailSender implements MailSenderRemote, MailSenderLocal {
	protected final Logger logger = Logger.getLogger(MailSender.class);
	@Resource
	SessionContext ctx;
	private MailSenderBo mailSenderBo;
	private EmailAddressService emailAddrDao;
    /**
     * Default constructor. 
     */
    public MailSender() {
    	mailSenderBo = SpringUtil.getAppContext().getBean(MailSenderBo.class);
    	emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressService.class);
    }

    @Override
	public void send(MessageBean msgBean) throws IOException, SmtpException {
		mailSenderBo.process(new MessageContext(msgBean));
	}

    @Override
	public void send(byte[] msgStream) throws IOException, SmtpException {
		mailSenderBo.process(new MessageContext(msgStream));
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRES_NEW)
	@Override
	public EmailAddress findByAddress(String address) {
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(address);
		return emailAddrVo;
	}
}
