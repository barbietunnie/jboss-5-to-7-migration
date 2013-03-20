package jpa.service.task;

import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.AddressException;

import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.exception.TemplateNotFoundException;
import jpa.message.HtmlConverter;
import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.EmailAddressService;
import jpa.service.EmailTemplateService;
import jpa.service.MailingListService;
import jpa.service.msgin.EmailTemplateBo;
import jpa.service.msgin.TemplateRenderVo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("autoReplyMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class AutoReplyMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(AutoReplyMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private EmailTemplateService emailTemplateDao;
	@Autowired
	private MailingListService mailingListDao;
	@Autowired
	private EmailTemplateBo tmpltBo;
	
	/**
	 * construct the reply text from the TaskArguments, render the text and send
	 * the reply message to MailSender input queue.
	 * 
	 * @param messageBean -
	 *            the original email that is replying to.
	 * @return a Integer value representing number of addresses the message is
	 *         replied to.
	 * @throws AddressException
	 * @throws TemplateException 
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException,
			AddressException, TemplateException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (StringUtils.isBlank(taskArguments)) {
			throw new DataValidationException("Arguments(TemplateId) is not valued.");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		// check FROM address
		Address[] from = messageBean.getFrom();
		if (from == null || from.length == 0) {
			throw new DataValidationException("FROM is not valued, no one to reply to.");
		}
		// create a reply message bean
		MessageBean replyBean = new MessageBean();
		replyBean.setFrom(messageBean.getTo());
		if (messageBean.getMsgId() != null) {
			replyBean.setMsgRefId(messageBean.getMsgId());
		}
		else if (messageBean.getMsgRefId() != null) {
			replyBean.setMsgRefId(messageBean.getMsgRefId());
		}
		replyBean.setMailboxUser(messageBean.getMailboxUser());
		int msgsSent = 0;
		for (int i = 0; i < from.length; i++) {
			Address _from = from[i];
			// check FROM address
			if (_from == null || StringUtils.isBlank(_from.toString())) {
				continue;
			}
			// select the address from database (or insert if it does not exist)
			EmailAddress vo = emailAddrDao.findSertAddress(_from.toString());
			Map<String, String> variables = null;
			TemplateRenderVo renderVo = null;
			try {
				// Mailing List id may have been provided by upstream process (subscribe)
				renderVo = tmpltBo.renderEmailTemplate(vo.getAddress(), variables, taskArguments,
						messageBean.getMailingListId());
			}
			catch (TemplateNotFoundException e) {
				throw new DataValidationException("Email Template not found by Id: "
						+ taskArguments);
			}
			replyBean.setSubject(renderVo.getSubject());
			String body = renderVo.getBody();
			if (renderVo.getEmailTemplate() != null && renderVo.getEmailTemplate().isHtml()) {
				if (vo.isAcceptHtml()) {
					replyBean.setContentType("text/html");
				}
				else {
					try {
						body = HtmlConverter.getInstance().convertToText(body);
					}
					catch (ParserException e) {
						logger.error("Failed to convert from html to plain text for: " + body);
						logger.error("ParserException caught", e);
					}
				}
			}
			replyBean.setBody(body);
			replyBean.setSenderId(messageBean.getSenderId());
			if (StringUtils.isNotBlank(messageBean.getSenderId())) {
				replyBean.setSenderId(messageBean.getSenderId());
			}
			else if (StringUtils.isNotBlank(renderVo.getSenderId())) {
				replyBean.setSenderId(renderVo.getSenderId());
			}
			replyBean.setSubrId(messageBean.getSubrId());
			// set recipient address
			Address[] _to = {_from};
			replyBean.setTo(_to);
			// TODO send the reply off
			msgsSent++;
			if (isDebugEnabled) {
				logger.debug("Reply message processed: " + LF + replyBean);
			}
		}
		return Integer.valueOf(msgsSent);
	}
}