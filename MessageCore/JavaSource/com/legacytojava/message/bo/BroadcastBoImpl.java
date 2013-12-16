package com.legacytojava.message.bo;

import java.util.HashMap;
import java.util.List;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.HtmlConverter;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.template.RenderUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MobileCarrier;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.VariableName;
import com.legacytojava.message.dao.customer.CustomerDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.dao.emailaddr.SubscriptionDao;
import com.legacytojava.message.dao.inbox.MsgClickCountsDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.PhoneNumberUtil;
import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;
import com.legacytojava.message.vo.emailaddr.TemplateRenderVo;

@Component("broadcastBo")
@Scope(value="prototype")
@Lazy(true)
public class BroadcastBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(BroadcastBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private SubscriptionDao subscriptionDao;
	@Autowired
	private MsgClickCountsDao msgClickCountsDao;
	@Autowired
	private CustomerDao customerDao;

	/**
	 * Send the email to the addresses on the Mailing List.
	 * 
	 * @param msgBean -
	 *            message to broadcast
	 * @return a Long value representing number of addresses the message has
	 *         been sent to.
	 */
	public Long process(MessageBean msgBean) throws DataValidationException,
			AddressException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (msgBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (msgBean.getMsgId()==null) {
			throw new DataValidationException("MsgId is null");
		}
		if (!RuleNameType.BROADCAST.toString().equals(msgBean.getRuleName())) {
			throw new DataValidationException("Invalid Rule Name: " + msgBean.getRuleName());
		}
		if (taskArguments != null && taskArguments.trim().length() > 0) {
			// mailing list from MessageBean takes precedence
			if (msgBean.getMailingListId() == null) {
				msgBean.setMailingListId(taskArguments);
			}
		}
		if (msgBean.getMailingListId() == null) {
			throw new DataValidationException("Mailing List was not provided.");
		}
		
		long mailsSent = 0;
		Boolean saveEmbedEmailId = msgBean.getEmBedEmailId();
		String listId = msgBean.getMailingListId();
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		else if (!listVo.isActive()) {
			logger.warn("MailingList " + listId + " is not active.");
			return Long.valueOf(0);
		}
		String _from = listVo.getEmailAddr();
		String dispName = listVo.getDisplayName();
		if (dispName != null && dispName.trim().length() > 0) {
			_from = dispName + "<" + _from + ">";
		}
		logger.info("Broadcasting to Mailing List: " + listId + ", From: " + _from);
		Address[] from = InternetAddress.parse(_from);
		// set FROM to list address
		msgBean.setFrom(from);
		// get message body from body node
		String bodyText = null;
		if (msgBean.getBodyNode() != null) {
			bodyText = new String(msgBean.getBodyNode().getValue());
		}
		if (bodyText == null) {
			throw new DataValidationException("Message body is empty.");
		}
		msgClickCountsDao.updateStartTime(msgBean.getMsgId());
		// extract variables from message body
		List<String> varNames = RenderUtil.retrieveVariableNames(bodyText);
		if (isDebugEnabled)
			logger.debug("Body Variable names: " + varNames);
		// extract variables from message subject
		String subjText = msgBean.getSubject() == null ? "" : msgBean.getSubject();
		List<String> subjVarNames = RenderUtil.retrieveVariableNames(subjText);
		if (!subjVarNames.isEmpty()) {
			varNames.addAll(subjVarNames);
			if (isDebugEnabled)
				logger.debug("Subject Variable names: " + subjVarNames);
		}
		// get subscribers
		List<SubscriptionVo> subs = null;
		if (msgBean.getToCustomersOnly()) {
			subs = subscriptionDao.getSubscribersWithCustomerRecord(listId);
		}
		else if (msgBean.getToProspectsOnly()) {
			subs = subscriptionDao.getSubscribersWithoutCustomerRecord(listId);
		}
		else {
			subs = subscriptionDao.getSubscribers(listId);
		}
		// sending email to each subscriber
		setTargetToMailSender();
		for (SubscriptionVo sub : subs) {
			mailsSent += constructAndSendMessage(msgBean, sub, listVo, bodyText, subjVarNames, saveEmbedEmailId, false);
			if (Constants.YES_CODE.equalsIgnoreCase(listVo.getIsSendText())) {
				mailsSent += constructAndSendMessage(msgBean, sub, listVo, bodyText, subjVarNames, saveEmbedEmailId, true);
			}
		}
		if (mailsSent > 0 && msgBean.getMsgId() != null) {
			// update sent count to the Broadcasted message
			msgClickCountsDao.updateSentCount(msgBean.getMsgId(), (int) mailsSent);
		}
		return Long.valueOf(mailsSent);
	}
	
	private int constructAndSendMessage(MessageBean msgBean,
			SubscriptionVo sub, MailingListVo listVo, String bodyText,
			List<String> varNames, Boolean saveEmbedEmailId, boolean isText)
			throws JMSException, DataValidationException {
		String listId = msgBean.getMailingListId();
		String subjText = msgBean.getSubject() == null ? "" : msgBean.getSubject();
		Address[] to = null;
		String toAddress = null;
		try {
			if (isText) {
				CustomerVo custVo = customerDao.getByEmailAddrId(sub.getEmailAddrId());
				if (custVo != null
						&& StringUtils.isNotBlank(custVo.getMobilePhone())
						&& StringUtils.isNotBlank(custVo.getMobileCarrier())) {
					try {
						MobileCarrier mc = MobileCarrier.getByValue(custVo.getMobileCarrier());
						String phone = PhoneNumberUtil.convertTo10DigitNumber(custVo.getMobilePhone());
						if (StringUtils.isNotBlank(mc.getCountry())) {
							phone = mc.getCountry() + phone;
						}
						toAddress = phone+"@"+mc.getText();
						to = InternetAddress.parse(toAddress);
					}
					catch (IllegalArgumentException e) {
						logger.error("Mobile carrier (" + custVo.getMobileCarrier() + ") not found in enum MobileCarrier!");
						// TODO notify programming
					}
				}
				if (to == null) {
					return 0;
				}
			}
			else {
				toAddress = sub.getEmailAddr();
				to = InternetAddress.parse(toAddress);
			}
		}
		catch (AddressException e) {
			logger.error("Invalid TO address, ignored: " + toAddress, e);
			return 0;
		}
		/*
		String mailingAddr = StringUtil.removeDisplayName(listVo.getEmailAddr(), true);
		if (sub.getEmailAddr().toLowerCase().indexOf(mailingAddr) >= 0) {
			logger.warn("Loop occurred, ignore mailing list address: " + sub.getEmailAddr());
			continue;
		}
		*/
		HashMap<String, String> variables = new HashMap<String, String>();
		if (msgBean.getMsgId() != null) {
			String varName = VariableName.LIST_VARIABLE_NAME.BroadcastMsgId.toString();
			variables.put(varName, String.valueOf(msgBean.getMsgId()));
		}
		logger.info("Sending Broadcast Email to: " + toAddress);
		TemplateRenderVo renderVo = null;
		renderVo = RenderUtil.renderEmailText(toAddress, variables, subjText,
				bodyText, listId, varNames);
		// set TO to subscriber address
		msgBean.setTo(to);
		String body = renderVo.getBody();
		if ("text/html".equals(msgBean.getBodyContentType())
				&& Constants.NO_CODE.equals(sub.getAcceptHtml()) || isText) {
			// convert to plain text
			try {
				body = HtmlConverter.getInstance().convertToText(body);
				msgBean.getBodyNode().setContentType("text/plain");
			}
			catch (ParserException e) {
				logger.error("Failed to convert from html to plain text for: " + body);
				logger.error("ParserException caught", e);
			}
		}
		msgBean.getBodyNode().setValue(body);
		msgBean.setSubject(renderVo.getSubject());
		if (isText) { // do not embed email id in text message
			msgBean.setEmBedEmailId(Boolean.FALSE);
		}
		else {
			msgBean.setEmBedEmailId(saveEmbedEmailId);
			subscriptionDao.updateSentCount(sub.getEmailAddrId(), listId);
		}
		// write to mail sender queue
		String jmsMsgId = jmsProcessor.writeMsg(msgBean);
		if (isDebugEnabled)
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		int mailsSent = msgBean.getTo().length;
		return mailsSent;
	}

}
