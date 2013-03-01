package com.legacytojava.message.bo.mailinglist;

import java.io.IOException;
import java.util.Map;

import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.template.RenderUtil;
import com.legacytojava.message.constant.CarrierCode;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailingListDeliveryOption;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.emailaddr.EmailTemplateDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.dao.emailaddr.SubscriptionDao;
import com.legacytojava.message.dao.inbox.MsgClickCountsDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.message.vo.emailaddr.TemplateRenderVo;

public class MailingListBoImpl implements MailingListBo {
	static final Logger logger = Logger.getLogger(MailingListBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private EmailTemplateDao emailTemplateDao;
	private MailingListDao mailingListDao;
	private EmailAddrDao emailAddrDao;
	private SubscriptionDao subscriptionDao;
	private MsgClickCountsDao msgClickCountsDao;
	private TaskBaseBo assignRuleNameBo;
	private TaskBaseBo sendMailBo;
	
	/**
	 * broadcast to the mailing list retrieved from the template by the provided
	 * template id. The default list id from the template is used to obtain the
	 * TO addresses of the subscribers. The message subject and message body
	 * from the template are used to construct a MessageBean. The messageBean is
	 * then passed to BroadcaseBo for processing.
	 * 
	 * @param templateId -
	 *            unique id of a template where the message information come
	 *            from
	 * @return number of mails sent
	 * @throws OutOfServiceException
	 * @throws TemplateNotFoundException
	 * @throws DataValidationException
	 */
	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		EmailTemplateVo vo = emailTemplateDao.getByTemplateId(templateId);
		if (vo == null) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		return broadcast(vo);
	}
	
	/**
	 * broadcast to the mailing list retrieved from a template, use the provided
	 * list id.
	 * 
	 * @param templateId-
	 *            unique id of a template where the message information come
	 *            from
	 * @param listId -
	 *            mailing list id to be used by the template
	 * @return number of mails sent
	 * @throws OutOfServiceException
	 * @throws TemplateNotFoundException
	 * @throws DataValidationException
	 */
	public int broadcast(String templateId, String listId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		EmailTemplateVo vo = emailTemplateDao.getByTemplateId(templateId);
		if (vo == null) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		vo.setListId(listId);
		return broadcast(vo);
	}

	/**
	 * Send the email off using provided information.
	 * 
	 * @param toAddr -
	 *            the target email address
	 * @param variables -
	 *            name/value pair of variables used to render the template
	 * @param templateId -
	 *            template id used to retrieve the message template
	 * @return number of mails sent
	 * @throws OutOfServiceException
	 * @throws TemplateNotFoundException
	 * @throws DataValidationException
	 */
	public int send(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException {
		// render email template
		TemplateRenderVo renderVo = RenderUtil.renderEmailTemplate(toAddr, variables, templateId);
		// create MessageBean
		MessageBean msgBean = createMessageBean(renderVo.getEmailTemplateVo());
		//msgBean.getBodyNode().setValue(renderVo.getBody());
		msgBean.setBody(renderVo.getBody());
		msgBean.setSubject(renderVo.getSubject());
		msgBean.setRuleName(RuleNameType.SEND_MAIL.toString());
		try {
			msgBean.setTo(InternetAddress.parse(toAddr));
		}
		catch (AddressException e) {
			throw new DataValidationException("Input toAddr is invalid: " + toAddr, e);
		}
		try {
			msgBean.setFrom(InternetAddress.parse(renderVo.getFromAddr()));
		}
		catch (AddressException e) {
			throw new DataValidationException("Invalid FROM address found from list: "
					+ renderVo.getFromAddr());
		}
		if (!StringUtil.isEmpty(renderVo.getCcAddr())) {
			try {
				msgBean.setCc(InternetAddress.parse(renderVo.getCcAddr()));
			}
			catch (AddressException e) {
				logger.error("send() - ccAddr is invalid: " + renderVo.getCcAddr());
			}
		}
		if (!StringUtil.isEmpty(renderVo.getBccAddr())) {
			try {
				msgBean.setBcc(InternetAddress.parse(renderVo.getBccAddr()));
			}
			catch (AddressException e) {
				logger.error("send() -bccAddr is invalid: " + renderVo.getBccAddr());
			}
		}
		if (isDebugEnabled) {
			logger.debug("send() - MessageBean created:" + LF + msgBean);
		}
		
		Long mailsSent = 0L;
		try {
			mailsSent = (Long) sendMailBo.process(msgBean);
		}
		catch (MessagingException e) {
			throw new OutOfServiceException("MessagingException caught",e);
		}
		catch (JMSException e) {
			throw new OutOfServiceException("JMSException caught",e);
		}
		catch (IOException e) {
			throw new OutOfServiceException("IOException caught",e);
		}
		return mailsSent.intValue();
	}
	
	private int broadcast(EmailTemplateVo vo) throws DataValidationException, OutOfServiceException {
		MessageBean msgBean = createMessageBean(vo);
		if (isDebugEnabled) {
			logger.debug("broadcast() - MessageBean created:" + LF + msgBean);
		}
		String jmsMsgId = null;
		try {
			assignRuleNameBo.setTaskArguments(msgBean.getRuleName());
			jmsMsgId = (String) assignRuleNameBo.process(msgBean);
		}
		catch (MessagingException e) {
			throw new OutOfServiceException("MessagingException caught",e);
		}
		catch (JMSException e) {
			throw new OutOfServiceException("JMSException caught",e);
		}
		catch (IOException e) {
			throw new OutOfServiceException("IOException caught",e);
		}
		return (jmsMsgId == null ? 0 : 1);
	}

	final static String LF = System.getProperty("line.separator", "\n");
	
	private MessageBean createMessageBean(EmailTemplateVo tmpltVo)
			throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering createMessageBean() method...");

		MessageBean msgBean = new MessageBean();
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setClientId(tmpltVo.getClientId());
		msgBean.setSubject(tmpltVo.getSubject());
		msgBean.setSendDate(new java.util.Date());
		
		msgBean.setIsReceived(false);
		msgBean.setOverrideTestAddr(false);
		msgBean.setRuleName(RuleNameType.BROADCAST.toString());
		if (Constants.YES_CODE.equalsIgnoreCase(tmpltVo.getEmbedEmailId())) {
			msgBean.setEmBedEmailId(Boolean.valueOf(true));
		}
		else if (Constants.NO_CODE.equalsIgnoreCase(tmpltVo.getEmbedEmailId())) {
			msgBean.setEmBedEmailId(Boolean.valueOf(false));
		}
		msgBean.setToCustomersOnly(MailingListDeliveryOption.CUSTOMERS_ONLY.equals(tmpltVo.getDeliveryOption()));
		msgBean.setToProspectsOnly(MailingListDeliveryOption.PROSPECTS_ONLY.equals(tmpltVo.getDeliveryOption()));
		
		// set message body and attachments
		String msgBody = tmpltVo.getBodyText();
		if (tmpltVo.getIsHtml()) {
			msgBean.setContentType("text/html");
		}
		else {
			msgBean.setContentType("text/plain");
		}
		msgBean.setBody(msgBody);
		
		// check mailing list id
		String listId = tmpltVo.getListId();
		msgBean.setMailingListId(listId);
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Could not find Mailing List by Id: " + listId);
		}
		return msgBean;
	}

	public int subscribe(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("subscribe() -  emailAddr: " + emailAddr + ", listAddr: " + listId);
		int emailsAdded = addOrRemove(emailAddr, listId, true);
		return emailsAdded;
	}

	public int unSubscribe(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("unSubscribe() - emailAddr: " + emailAddr + ", listAddr: " + listId);
		int emailsRemoved = addOrRemove(emailAddr, listId, false);
		return emailsRemoved;
	}

	public int optInRequest(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("optInRequest() -  emailAddr: " + emailAddr + ", listAddr: " + listId);
		int emailsAdded = optInOrConfirm(emailAddr, listId, false);
		return emailsAdded;
	}

	public int optInConfirm(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("optInConfirm() -  emailAddr: " + emailAddr + ", listAddr: " + listId);
		int emailsAdded = optInOrConfirm(emailAddr, listId, true);
		return emailsAdded;
	}

	public int optInConfirm(long emailAddrId, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("optInConfirm() -  emailAddrId: " + emailAddrId + ", listAddr: " + listId);
		EmailAddrVo addrVo = getEmailAddrDao().getByAddrId(emailAddrId);
		int emailsAdded = 0;
		if (addrVo != null) {
			emailsAdded = optInOrConfirm(addrVo.getEmailAddr(), listId, true);
		}
		return emailsAdded;
	}

	public int updateSentCount(long emailAddrId, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("updateSentCount() - emailAddrId: " + emailAddrId + ", listId: " + listId);
		if (StringUtil.isEmpty(listId)) {
			throw new DataValidationException("Mailing List Id is not valued.");
		}
		int recsUpdated = getSubscriptionDao().updateSentCount(emailAddrId, listId);
		return recsUpdated;
	}

	public int updateOpenCount(long emailAddrId, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("updateOpenCount() - emailAddrId: " + emailAddrId + ", listId: " + listId);
		if (StringUtil.isEmpty(listId)) {
			throw new DataValidationException("Mailing List Id is not valued.");
		}
		int recsUpdated = getSubscriptionDao().updateOpenCount(emailAddrId, listId);
		return recsUpdated;
	}

	public int updateClickCount(long emailAddrId, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("updateClickCount() - emailAddrId: " + emailAddrId + ", listId: " + listId);
		if (StringUtil.isEmpty(listId)) {
			throw new DataValidationException("Mailing List Id is not valued.");
		}
		int recsUpdated = getSubscriptionDao().updateClickCount(emailAddrId, listId);
		return recsUpdated;
	}

	public int updateSentCount(long msgId, int count) {
		if (isDebugEnabled)
			logger.debug("updateSentCount() - MsgId: " + msgId);
		int recsUpdated = getMsgClickCountsDao().updateSentCount(msgId, count);
		return recsUpdated;
	}

	public int updateOpenCount(long msgId, int count) {
		if (isDebugEnabled)
			logger.debug("updateOpenCount() - MsgId: " + msgId);
		int recsUpdated = getMsgClickCountsDao().updateOpenCount(msgId, count);
		return recsUpdated;
	}

	public int updateClickCount(long msgId, int count) {
		if (isDebugEnabled)
			logger.debug("updateClickCount() - MsgId: " + msgId);
		int recsUpdated = getMsgClickCountsDao().updateClickCount(msgId, count);
		return recsUpdated;
	}

	private int addOrRemove(String emailAddr, String listId, boolean addToList)
			throws DataValidationException {
		// validate email address and list id
		validateInput(emailAddr, listId);
		// retrieve/insert email address from/into EmailAddr table
		int rowsAffected = 0;
		if (addToList) {
			rowsAffected = subscriptionDao.subscribe(emailAddr, listId);
			logger.info(emailAddr + " added to list: " + listId);
		}
		else {
			rowsAffected = subscriptionDao.unsubscribe(emailAddr, listId);
			logger.info(emailAddr + " removed from list: " + listId);
		}
		return rowsAffected;
	}

	private int optInOrConfirm(String emailAddr, String listId, boolean confirm)
			throws DataValidationException {
		// validate email address and list id
		validateInput(emailAddr, listId);
		// opt-in or confirm subscription
		int rowsAffected = 0;
		if (confirm) {
			rowsAffected = subscriptionDao.optInConfirm(emailAddr, listId);
			logger.info(emailAddr + " confirmed to list: " + listId);
		}
		else {
			rowsAffected = subscriptionDao.optInRequest(emailAddr, listId);
			logger.info(emailAddr + " opt-in'ed to list: " + listId);
		}
		return rowsAffected;
	}

	private void validateInput(String emailAddr, String listId)
			throws DataValidationException {
		// validate email address
		if (StringUtil.isEmpty(emailAddr)) {
			throw new DataValidationException("Email Address is not valued.");
		}
		if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			throw new DataValidationException("Email Address is invalid: " + emailAddr);
		}
		// validate mailing list id
		if (StringUtil.isEmpty(listId)) {
			throw new DataValidationException("Mailing List Id is not valued.");
		}
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List does not exist: " + listId);
		}
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}

	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

	public MailingListDao getMailingListDao() {
		return mailingListDao;
	}

	public void setMailingListDao(MailingListDao mailingListDao) {
		this.mailingListDao = mailingListDao;
	}

	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}

	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

	public MsgClickCountsDao getMsgClickCountsDao() {
		return msgClickCountsDao;
	}

	public void setMsgClickCountsDao(MsgClickCountsDao msgClickCountsDao) {
		this.msgClickCountsDao = msgClickCountsDao;
	}

	public TaskBaseBo getSendMailBo() {
		return sendMailBo;
	}

	public void setSendMailBo(TaskBaseBo sendMailBo) {
		this.sendMailBo = sendMailBo;
	}

	public TaskBaseBo getAssignRuleNameBo() {
		return assignRuleNameBo;
	}

	public void setAssignRuleNameBo(TaskBaseBo assignRuleNameBo) {
		this.assignRuleNameBo = assignRuleNameBo;
	}
}
