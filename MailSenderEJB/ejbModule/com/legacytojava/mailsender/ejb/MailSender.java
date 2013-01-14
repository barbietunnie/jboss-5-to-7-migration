package com.legacytojava.mailsender.ejb;

import java.io.IOException;
import java.util.HashMap;
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
import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import org.jboss.logging.Logger;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.mailsender.MailSenderBoImpl;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

/**
 * Session Bean implementation class MailSender
 */
@Stateless(name="MailSender")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Local(MailSenderLocal.class)
@Remote(MailSenderRemote.class)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
public class MailSender implements MailSenderRemote, MailSenderLocal {
	protected final Logger logger = Logger.getLogger(MailSender.class);
	@Resource
	SessionContext ctx;
	private MailSenderBoImpl mailSenderBoImpl;
	private EmailAddrDao emailAddrDao;
    /**
     * Default constructor. 
     */
    public MailSender() {
    	mailSenderBoImpl = new MailSenderBoImpl();
    	emailAddrDao = (EmailAddrDao)SpringUtil.getAppContext().getBean("emailAddrDao");
    }

	public void send(MessageBean msgBean) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException {
		try {
			mailSenderBoImpl.process(msgBean);
		}
		catch (SendFailedException sfex) {
			// failed to send the message to certain recipients
			Map<String, ?> errors = new HashMap<String, Object>();
			logger.error("SendFailedException caught", sfex);
			mailSenderBoImpl.updtDlvrStatAndLoopback(msgBean, sfex, errors);
			if (errors.containsKey("validSent")) {
				mailSenderBoImpl.sendDeliveryReport(msgBean);
			}
		}
	}

	public void send(byte[] msgStream) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException {
		mailSenderBoImpl.process(msgStream);
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRES_NEW)
	public EmailAddrVo findByAddress(String address) {
		EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(address);
		return emailAddrVo;
	}
}
