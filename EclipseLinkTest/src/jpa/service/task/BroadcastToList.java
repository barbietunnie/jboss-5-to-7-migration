package jpa.service.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.MobileCarrierEnum;
import jpa.constant.VariableName;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.HtmlConverter;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.MailingList;
import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.service.MailingListService;
import jpa.service.SubscriberDataService;
import jpa.service.SubscriptionService;
import jpa.service.message.MessageClickCountService;
import jpa.service.msgin.EmailTemplateBo;
import jpa.service.msgin.TemplateRenderVo;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;
import jpa.util.PhoneNumberUtil;
import jpa.variable.RenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("broadcastToList")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastToList extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(BroadcastToList.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListService mailingListDao;
	@Autowired
	private SubscriptionService subscriptionDao;
	@Autowired
	private MessageClickCountService msgClickCountsDao;
	@Autowired
	private SubscriberDataService subscriberDao;
	@Autowired
	private EmailTemplateBo emailTemplateBo;
	@Autowired
	private MailSenderBo mailSenderBo;

	/**
	 * Send the email to the addresses on the Mailing List.
	 * 
	 * @param msgBean -
	 *            message to broadcast
	 * @return an Integer value representing number of addresses the message has
	 *         been sent to.
	 * @throws TemplateException 
	 * @throws IOException 
	 */
	public Integer process(MessageBean msgBean) throws DataValidationException,
			AddressException, TemplateException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (msgBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (msgBean.getMsgId()==null) {
			throw new DataValidationException("MsgId is null");
		}
		if (!RuleNameEnum.BROADCAST.getValue().equals(msgBean.getRuleName())) {
			throw new DataValidationException("Invalid Rule Name: " + msgBean.getRuleName());
		}
		if (StringUtils.isNotBlank(taskArguments)) {
			// mailing list from MessageBean takes precedence
			if (StringUtils.isBlank(msgBean.getMailingListId())) {
				msgBean.setMailingListId(taskArguments);
			}
		}
		if (StringUtils.isBlank(msgBean.getMailingListId())) {
			throw new DataValidationException("Mailing List was not provided.");
		}
		
		int mailsSent = 0;
		Boolean saveEmbedEmailId = msgBean.getEmBedEmailId();
		String listId = msgBean.getMailingListId();
		MailingList listVo = null;
		try {
			listVo = mailingListDao.getByListId(listId);
		}
		catch (NoResultException e) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		if (!listVo.isActive()) {
			logger.warn("MailingList " + listId + " is not active.");
			return Integer.valueOf(0);
		}
		String _from = listVo.getListEmailAddr();
		String dispName = listVo.getDisplayName();
		if (StringUtils.isNotBlank(dispName)) {
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
		List<Subscription> subs = null;
		if (msgBean.getToSubscribersOnly()) {
			subs = subscriptionDao.getByListId(listId);
		}
		else if (msgBean.getToProspectsOnly()) {
			subs = subscriptionDao.getByListId(listId);
		}
		else {
			subs = subscriptionDao.getByListId(listId);
		}
		// sending email to each subscriber
		for (Subscription sub : subs) {
			mailsSent += constructAndSendMessage(msgBean, sub, listVo, bodyText, subjVarNames, saveEmbedEmailId, false);
			if (listVo.isSendText()) {
				mailsSent += constructAndSendMessage(msgBean, sub, listVo, bodyText, subjVarNames, saveEmbedEmailId, true);
			}
		}
		if (mailsSent > 0 && msgBean.getMsgId() != null) {
			// update sent count to the Broadcasted message
			msgClickCountsDao.updateSentCount(msgBean.getMsgId(), (int) mailsSent);
		}
		return Integer.valueOf(mailsSent);
	}
	
	private int constructAndSendMessage(MessageBean msgBean, Subscription sub,
			MailingList listVo, String bodyText, List<String> varNames,
			Boolean saveEmbedEmailId, boolean isText)
			throws DataValidationException, TemplateException, IOException {
		String listId = msgBean.getMailingListId();
		String subjText = msgBean.getSubject() == null ? "" : msgBean.getSubject();
		Address[] to = null;
		String toAddress = null;
		try {
			if (isText) {
				try {
					SubscriberData subrVo = subscriberDao.getByEmailAddress(sub.getEmailAddr().getAddress());
					if (StringUtils.isNotBlank(subrVo.getMobilePhone())
							&& StringUtils.isNotBlank(subrVo.getMobileCarrier())) {
						try {
							MobileCarrierEnum mc = MobileCarrierEnum.getByValue(subrVo.getMobileCarrier());
							String phone = PhoneNumberUtil.convertTo10DigitNumber(subrVo.getMobilePhone());
							if (StringUtils.isNotBlank(mc.getCountry())) {
								phone = mc.getCountry() + phone;
							}
							toAddress = phone+"@"+mc.getText();
							to = InternetAddress.parse(toAddress);
						}
						catch (IllegalArgumentException e) {
							logger.error("Mobile carrier (" + subrVo.getMobileCarrier() + ") not found in enum MobileCarrier!");
							// TODO notify programming
						}
					}
				}
				catch (NoResultException e) {}
				if (to == null) {
					return 0;
				}
			}
			else {
				toAddress = sub.getEmailAddr().getAddress();
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
		TemplateRenderVo renderVo = null;
		renderVo = emailTemplateBo.renderEmailText(toAddress, variables, subjText,
				bodyText, listId, varNames);
		// set TO to subscriber address
		msgBean.setTo(to);
		String body = renderVo.getBody();
		if ("text/html".equals(msgBean.getBodyContentType())
				&& !sub.getEmailAddr().isAcceptHtml() || isText) {
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
			subscriptionDao.updateSentCount(sub.getRowId());
		}
		// invoke mail sender to send the mail off
		try {
			mailSenderBo.process(new MessageContext(msgBean));
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

}
