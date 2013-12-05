package com.es.bo.task;

import java.io.IOException;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.sender.EmailRenderBo;
import com.es.bo.sender.EmailRenderDo;
import com.es.bo.sender.MailSenderBo;
import com.es.bo.smtp.SmtpException;
import com.es.core.util.HtmlConverter;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.address.EmailTemplateDao;
import com.es.dao.address.MailingListDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.exception.DataValidationException;
import com.es.exception.TemplateException;
import com.es.exception.TemplateNotFoundException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;

@Component("autoReplyMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class AutoReplyMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 6364742594226515121L;
	static final Logger logger = Logger.getLogger(AutoReplyMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddrDao;
	@Autowired
	private EmailTemplateDao emailTemplateDao;
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private EmailRenderBo emailRenderBo;
	@Autowired
	private MailSenderBo mailSenderBo;
	
	/**
	 * Construct the reply text from the EmailTemplateId passed in the
	 * TaskArguments, render the text and send the reply message.
	 * 
	 * @param messageBean
	 *            - the original email that is replying to.
	 * @return a Integer value representing number of addresses the message is
	 *         replied to.
	 * @throws AddressException
	 * @throws TemplateException
	 * @throws IOException
	 */
	public Integer process(MessageContext ctx) throws DataValidationException,
			AddressException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (StringUtils.isBlank(ctx.getTaskArguments())) {
			throw new DataValidationException("Arguments(TemplateId) is not valued.");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		MessageBean messageBean = ctx.getMessageBean();
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
		//replyBean.setOriginalMail(messageBean);
		int msgsSent = 0;
		for (int i = 0; i < from.length; i++) {
			Address _from = from[i];
			// check FROM address
			if (_from == null || StringUtils.isBlank(_from.toString())) {
				continue;
			}
			// select the address from database (or insert if it does not exist)
			EmailAddressVo vo = emailAddrDao.findSertAddress(_from.toString());
			Map<String, String> variables = null;
			EmailRenderDo renderVo = null;
			try {
				// Mailing List id may have been provided by upstream process (subscribe)
				renderVo = emailRenderBo.renderEmailTemplate(vo.getEmailAddr(), variables, ctx.getTaskArguments(),
						messageBean.getMailingListId());
			}
			catch (TemplateNotFoundException e) {
				throw new DataValidationException("Email Template not found by Id: "
						+ ctx.getTaskArguments());
			}
			catch (TemplateException e) {
				throw new DataValidationException("TemplateException caught", e);
			}
			replyBean.setSubject(renderVo.getSubject());
			String body = renderVo.getBody();
			if (renderVo.getEmailTemplateVo() != null && renderVo.getEmailTemplateVo().getIsHtml()) {
				if (CodeType.YES_CODE.getValue().equals(vo.getAcceptHtml())) {
					replyBean.setContentType(Constants.TEXT_HTML);
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
			try {
				ctx.setMessageBean(replyBean);
				mailSenderBo.process(ctx);
				if (isDebugEnabled) {
					logger.debug("Message replied to: " + replyBean.getToAsString());
				}
			}
			catch (SmtpException e) {
				throw new IOException(e.getMessage(), e);
			}
			msgsSent++;
			if (isDebugEnabled) {
				logger.debug("Reply message processed: " + LF + replyBean);
			}
		}
		return Integer.valueOf(msgsSent);
	}
}
