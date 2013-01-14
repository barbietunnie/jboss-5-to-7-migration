package com.legacytojava.message.ejb.sendmail;

import java.io.IOException;
import java.text.ParseException;

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
import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.jboss.logging.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.inbox.MsgInboxBo;
import com.legacytojava.message.bo.outbox.MsgOutboxBo;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.ClientVo;

/**
 * Session Bean implementation class SendMail
 */
@Stateless(mappedName = "ejb/SendMail")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SendMailRemote.class)
@Local(SendMailLocal.class)
public class SendMail implements SendMailRemote, SendMailLocal {
	protected static final Logger logger = Logger.getLogger(SendMail.class);
	@Resource
	SessionContext context;
	private MsgOutboxBo msgOutboxBo;
	private MsgInboxBo msgInboxBo;
	private TaskBaseBo sendMailBo;
    /**
     * Default constructor. 
     */
    public SendMail() {
		msgOutboxBo = (MsgOutboxBo)SpringUtil.getAppContext().getBean("msgOutboxBo");
		msgInboxBo = (MsgInboxBo)SpringUtil.getAppContext().getBean("msgInboxBo");
		sendMailBo = (TaskBaseBo)SpringUtil.getAppContext().getBean("sendMailBo");
    }

	public long saveRenderData(RenderResponse rsp) throws DataValidationException {
		long msgId = msgOutboxBo.saveRenderData(rsp);
		return msgId;
	}

	public long saveMessage(MessageBean messageBean) throws DataValidationException {
		long msgId = msgInboxBo.saveMessage(messageBean);
		return msgId;
	}

	public MessageBean getMessageByPK(long renderId) throws AddressException,
			DataValidationException, ParseException {
		MessageBean messageBean = msgOutboxBo.getMessageByPK(renderId);
		return messageBean;
	}

	public long sendMail(MessageBean messageBean) throws DataValidationException,
			MessagingException, JMSException, IOException {
		Long emailsSent = (Long) sendMailBo.process(messageBean);
		return emailsSent;
	}

	public long sendMailFromSite(String siteId, String toAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException {
		ClientVo clientVo = ClientUtil.getClientVo(siteId);
		String fromAddr = clientVo.getReturnPathLeft() + "@" + clientVo.getDomainName();
		return sendMail(fromAddr, toAddr, subject, body);
	}

	public long sendMailToSite(String siteId, String fromAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException {
		ClientVo clientVo = ClientUtil.getClientVo(siteId);
		String toAddr = clientVo.getReturnPathLeft() + "@" + clientVo.getDomainName();
		return sendMail(fromAddr, toAddr, subject, body);
	}

	public long sendMail(String fromAddr, String toAddr, String subject, String body)
			throws DataValidationException, MessagingException, JMSException, IOException {
		MessageBean msgBean = new MessageBean();
		msgBean.setTo(InternetAddress.parse(toAddr));
		msgBean.setSubject(subject);
		msgBean.setBody(body);
		msgBean.setClientId(Constants.DEFAULT_CLIENTID);
		msgBean.setFrom(InternetAddress.parse(fromAddr));
		return sendMail(msgBean);
	}
}
