package com.es.ejb.mailsender;

import java.io.IOException;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.EJBException;
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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;
import jpa.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.ejb.senderdata.SenderDataLocal;

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
@WebService (portName = "MailSender", serviceName = "MailSenderService", targetNamespace = "http://com.es.ws.mailsender/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class MailSender implements MailSenderRemote, MailSenderLocal, MailSenderWs {
	protected final Logger logger = Logger.getLogger(MailSender.class);
	@Resource
	SessionContext ctx;
	private MailSenderBo mailSenderBo;
	private EmailAddressService emailAddrDao;
	
	@javax.ejb.EJB
	private SenderDataLocal sender;
	
    /**
     * Default constructor. 
     */
    public MailSender() {
    	mailSenderBo = SpringUtil.getAppContext().getBean(MailSenderBo.class);
    	emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressService.class);
    }

    @Override
	public void send(MessageBean msgBean) {
		try {
			mailSenderBo.process(new MessageContext(msgBean));
		}
		catch (SmtpException | IOException e) {
			throw new EJBException("Exception caught", e);
		}
	}

    @Override
	public void send(byte[] msgStream) {
		try {
			mailSenderBo.process(new MessageContext(msgStream));
		}
		catch (SmtpException | IOException e) {
			throw new EJBException("Exception caught", e);
		}
	}

    @Override
	public void send(String fromAddr, String toAddr, String subject, String body) {
		MessageBean msgBean = new MessageBean();
		try {
			msgBean.setFrom(InternetAddress.parse(fromAddr));
		}
		catch (AddressException e) {
			throw new IllegalArgumentException("Invalid email FROM address: " + fromAddr);
		}
		try {
			msgBean.setTo(InternetAddress.parse(toAddr));
		} catch (AddressException e) {
			throw new IllegalArgumentException("Invalid email TO address: " + toAddr);
		}
		msgBean.setSubject(subject);
		msgBean.setBody(body);
		msgBean.setSenderId(Constants.DEFAULT_SENDER_ID);
		send(msgBean);
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRES_NEW)
	@Override
	public EmailAddress findByAddress(String address) {
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(address);
		return emailAddrVo;
	}

	@WebMethod
	@Override
	public void sendMail(String fromAddr, String toAddr, String subject, String body) {
    	logger.info("in sendMail() - from/to: " + fromAddr + "/" + toAddr); 
		send(fromAddr, toAddr, subject, body);
	}
	
	@WebMethod
	@Override
	public void sendMailToSite(String siteId, String fromAddr, String subject, String body) {
    	logger.info("in sendMailToSite() - siteId/from: " + siteId + "/" + fromAddr);
    	if (StringUtils.isBlank(siteId)) {
    		siteId = Constants.DEFAULT_SENDER_ID;
    	}
		jpa.model.SenderData sd = sender.findBySenderId(siteId);
		if (sd == null) {
			logger.info("Failed to find Sender by SenderId (" + siteId + "), exit.");
			return;
		}
		String to = sd.getReturnPathLeft() + "@" + sd.getDomainName() + "," + sd.getSubrCareEmail();
		logger.info("Email address from senderId: " + to);
		send(fromAddr, to, subject, body);
	}

}
