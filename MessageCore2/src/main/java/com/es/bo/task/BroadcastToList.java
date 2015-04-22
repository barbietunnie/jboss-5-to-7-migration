package com.es.bo.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.render.RenderUtil;
import com.es.bo.sender.EmailRenderBo;
import com.es.bo.sender.EmailRenderDo;
import com.es.bo.sender.MailSenderBo;
import com.es.bo.smtp.SmtpException;
import com.es.core.util.EmailSender;
import com.es.core.util.HtmlConverter;
import com.es.core.util.PhoneNumberUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.address.MailingListDao;
import com.es.dao.address.MobileCarrierDao;
import com.es.dao.address.SubscriptionDao;
import com.es.dao.inbox.MsgClickCountDao;
import com.es.dao.subscriber.SubscriberDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.MobileCarrierEnum;
import com.es.data.constant.VariableName;
import com.es.data.preload.RuleNameEnum;
import com.es.exception.DataValidationException;
import com.es.exception.TemplateException;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.address.MailingListVo;
import com.es.vo.address.MobileCarrierVo;
import com.es.vo.address.SubscriptionVo;
import com.es.vo.comm.SubscriberVo;

@Component("broadcastToList")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastToList extends TaskBaseAdaptor {
	private static final long serialVersionUID = -7248771079550892321L;
	static final Logger logger = Logger.getLogger(BroadcastToList.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private SubscriptionDao subscriptionDao;
	@Autowired
	private MsgClickCountDao msgClickCountsDao;
	@Autowired
	private SubscriberDao subscriberDao;
	@Autowired
	private EmailRenderBo emailTemplateBo;
	@Autowired
	private MailSenderBo mailSenderBo;
	@Autowired
	private EmailAddressDao emailAddrDao;
	@Autowired
	private MobileCarrierDao mCarrierDao;

	/**
	 * Send the email to the addresses on the Mailing List.
	 * 
	 * @param msgBean -
	 *            message to broadcast
	 * @return an Long value representing number of addresses the message has
	 *         been sent to.
	 * @throws TemplateException 
	 * @throws IOException 
	 */
	public Long process(MessageContext ctx) throws DataValidationException,
			AddressException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (!RuleNameEnum.BROADCAST.getValue().equals(messageBean.getRuleName())) {
			throw new DataValidationException("Invalid Rule Name: " + messageBean.getRuleName());
		}
		if (StringUtils.isNotBlank(ctx.getTaskArguments())) {
			// mailing list from MessageBean takes precedence
			if (StringUtils.isBlank(messageBean.getMailingListId())) {
				messageBean.setMailingListId(ctx.getTaskArguments());
			}
		}
		if (StringUtils.isBlank(messageBean.getMailingListId())) {
			throw new DataValidationException("Mailing List was not provided.");
		}
		
		messageBean.setIsReceived(false);
		int mailsSent = 0;
		Boolean saveEmbedEmailId = messageBean.getEmBedEmailId();
		String listId = messageBean.getMailingListId();
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		if (!listVo.isActive()) {
			logger.warn("MailingList " + listId + " is not active.");
			return Long.valueOf(0);
		}
		String _from = listVo.getEmailAddr();
		String dispName = listVo.getDisplayName();
		if (StringUtils.isNotBlank(dispName)) {
			_from = dispName + "<" + _from + ">";
		}
		logger.info("Broadcasting to Mailing List: " + listId + ", From: " + _from);
		Address[] from = InternetAddress.parse(_from);
		// set FROM to list address
		messageBean.setFrom(from);
		// get message body from body node
		String bodyText = null;
		if (messageBean.getBodyNode() != null) {
			bodyText = new String(messageBean.getBodyNode().getValue());
		}
		if (bodyText == null) {
			throw new DataValidationException("Message body is empty.");
		}
		// extract variables from message body
		List<String> varNames = RenderUtil.retrieveVariableNames(bodyText);
		if (isDebugEnabled) {
			logger.debug("Body Variable names: " + varNames);
		}
		// extract variables from message subject
		String subjText = messageBean.getSubject() == null ? "" : messageBean.getSubject();
		List<String> subjVarNames = RenderUtil.retrieveVariableNames(subjText);
		if (!subjVarNames.isEmpty()) {
			varNames.addAll(subjVarNames);
			if (isDebugEnabled) {
				logger.debug("Subject Variable names: " + subjVarNames);
			}
		}
		// get subscribers
		List<SubscriptionVo> subrs = null;
		if (messageBean.getToSubscribersOnly()) {
			subrs = subscriptionDao.getSubscribersWithRecord(listId);
		}
		else if (messageBean.getToProspectsOnly()) {
			subrs = subscriptionDao.getSubscribersWithoutRecord(listId);
		}
		else {
			subrs = subscriptionDao.getByListId(listId);
		}
		// sending email to each subscriber
		for (SubscriptionVo subr : subrs) {
			//messageBean.setSubject(subjText);
			//messageBean.getBodyNode().setValue(bodyText);
			try {
				mailsSent += constructAndSendMessage(ctx, subr, listVo, subjText, bodyText, varNames, saveEmbedEmailId, false);
				if (CodeType.YES_CODE.getValue().equals(listVo.getIsSendText())) {
					mailsSent += constructAndSendMessage(ctx, subr, listVo, subjText, bodyText, varNames, saveEmbedEmailId, true);
				}
				ctx.getEmailAddrIdList().add(subr.getEmailAddrId());
			}
			catch (TemplateException e) {
				logger.error("TemplateException caught", e);
				throw new DataValidationException("TemplateException caught", e);
			}
		}
		if (messageBean.getMsgId() != null) {
			msgClickCountsDao.updateStartTime(messageBean.getMsgId());
			if (mailsSent > 0) {
				// update sent count to the Broadcasted message
				msgClickCountsDao.updateSentCount(messageBean.getMsgId(), (int) mailsSent);
			}
		}
		return Long.valueOf(mailsSent);
	}
	
	private int constructAndSendMessage(MessageContext ctx, SubscriptionVo subscription,
			MailingListVo listVo, String subjText, String bodyText, List<String> varNames,
			Boolean saveEmbedEmailId, boolean isText)
			throws DataValidationException, IOException, TemplateException {
		MessageBean msgBean = ctx.getMessageBean();
		String listId = msgBean.getMailingListId();
		Address[] to = null;
		String toAddress = null;
		try {
			if (isText) { // find mobile phone email address
				toAddress = getTextingToAddress(subscription);
				if (StringUtils.isBlank(toAddress)) {
					return 0;
				}
				else {
					to = InternetAddress.parse(toAddress);
				}
			}
			else { // regular email address
				toAddress = subscription.getEmailAddr();
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
		Map<String, String> variables = new HashMap<String, String>();
		if (msgBean.getMsgId() != null) {
			String varName = VariableName.LIST_VARIABLE_NAME.BroadcastMsgId.name();
			variables.put(varName, String.valueOf(msgBean.getMsgId()));
		}
		logger.info("Sending Broadcast Email to: " + toAddress);
		EmailRenderDo renderVo = null;
		renderVo = emailTemplateBo.renderEmailText(toAddress, variables, subjText,
				bodyText, listId, varNames);
		// set TO to subscriber address
		msgBean.setTo(to);
		String body = renderVo.getBody();
		EmailAddressVo subrEmail = emailAddrDao.findSertAddress(subscription.getEmailAddr());
		if (Constants.TEXT_HTML.equals(msgBean.getBodyContentType())
				&& (isText || CodeType.NO_CODE.getValue().equals(subrEmail.getAcceptHtml()))) {
			// convert to plain text
			try {
				body = HtmlConverter.getInstance().convertToText(body);
				msgBean.getBodyNode().setContentType(Constants.TEXT_PLAIN);
			}
			catch (ParserException e) {
				logger.error("Failed to convert from html to plain text for: " + body);
				logger.error("ParserException caught", e);
			}
		}
		msgBean.getBodyNode().setValue(body);
		msgBean.setSubject(renderVo.getSubject());
		/*
		 * Remove existing Email_Id from header to ensure that a fresh Email_Id
		 * is always embedded in the header.
		 */
		msgBean.removeHeader(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
		if (isText) { // do not embed email id in text message
			msgBean.setEmBedEmailId(Boolean.FALSE);
		}
		else {
			msgBean.setEmBedEmailId(saveEmbedEmailId);
			subscriptionDao.updateSentCount(subscription.getEmailAddrId(), subscription.getListId());
		}
		// invoke mail sender to send the mail off
		try {
			mailSenderBo.process(ctx);
			if (isDebugEnabled) {
				logger.debug("Message sent to: " + msgBean.getToAsString());
			}
		}
		catch (SmtpException e) {
			throw new IOException(e.getMessage(), e);
		}
		int mailsSent = msgBean.getTo().length;
		return mailsSent;
	}

	private String getTextingToAddress(SubscriptionVo subscription) throws AddressException {
		SubscriberVo subrVo = subscriberDao.getByEmailAddress(subscription.getEmailAddr());
		String toAddress = null;
		if (subrVo != null) {
			if (StringUtils.isNotBlank(subrVo.getMobilePhone())
					&& StringUtils.isNotBlank(subrVo.getMobileCarrier())) {
				try {
					String phoneNumber = PhoneNumberUtil.convertTo10DigitNumber(subrVo.getMobilePhone());
					toAddress = getMobilePhoneEmailAddress(subrVo.getMobileCarrier(), phoneNumber);
				}
				catch (NumberFormatException e) {
					logger.error("Invalid mobile phone number (" + subrVo.getMobilePhone() + ") found in Subscriber!");
				}
			}
		}
		return toAddress;
	}
	
	private String getMobilePhoneEmailAddress(String carrierName, String phoneNumber) {
		String countryCode = "";
		String domainName = null;
		MobileCarrierVo mCarrierVo = mCarrierDao.getByCarrierName(carrierName);
		if (mCarrierVo != null) {
			countryCode = mCarrierVo.getCountryCode();
			domainName = mCarrierVo.getTextAddress();
		}
		else {
			try {
				MobileCarrierEnum mc = MobileCarrierEnum.getByValue(carrierName);
				countryCode = mc.getCountry();
				domainName = mc.getText();
			}
			catch (IllegalArgumentException e) {
				String msg = "Mobile carrier (" + carrierName + ") not found in enum MobileCarrierEnum!";
				logger.error(msg);
				// notify programming
				String subj = "(" + carrierName + ") need to be added to the system - {0}";
				EmailSender.sendEmail(subj, msg, null, EmailSender.EmailList.ToDevelopers);
				return null;
			}
		}
		if (StringUtils.isNotBlank(countryCode)) {
			phoneNumber = countryCode + phoneNumber;
		}
		return (phoneNumber + "@" + domainName);
	}
}
