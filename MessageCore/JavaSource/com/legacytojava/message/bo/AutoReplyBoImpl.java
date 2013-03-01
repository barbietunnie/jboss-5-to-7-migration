package com.legacytojava.message.bo;

import java.security.DigestException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;

import com.legacytojava.jbatch.common.KeyGenerator;
import com.legacytojava.message.bean.HtmlConverter;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.template.RenderUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.emailaddr.EmailTemplateDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.TemplateNotFoundException;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.TemplateRenderVo;

public class AutoReplyBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(AutoReplyBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private EmailAddrDao emailAddrDao;
	private EmailTemplateDao emailTemplateDao;
	private MailingListDao mailingListDao;
	/**
	 * construct the reply text from the TaskArguments, render the text and send
	 * the reply message to MailSender input queue.
	 * 
	 * @param messageBean -
	 *            the original email that is replying to.
	 * @return a Long value representing number of addresses the message is
	 *         replied to.
	 * @throws AddressException
	 * @throws JMSException
	 */
	public Long process(MessageBean messageBean) throws DataValidationException,
			AddressException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (StringUtil.isEmpty(taskArguments)) {
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
		setTargetToMailSender();
		for (int i = 0; i < from.length; i++) {
			Address _from = from[i];
			// check FROM address
			if (_from == null || StringUtil.isEmpty(_from.toString())) {
				continue;
			}
			// select the address from database (or insert if it does not exist)
			EmailAddrVo vo = emailAddrDao.findByAddress(_from.toString());
			Map<String, String> variables = null;
			/* 
			 * add product key to reply message for "Free Premium Upgrade" promotion. 
			 */
			if (Constants.FreePremiumUpgradeTemplateId.equals(taskArguments)) {
				variables = new HashMap<String, String>();
				try {
					variables.put("_ProductKey", KeyGenerator.generateKey());
				}
				catch (DigestException e) { // should never happen
					logger.error("DigestException caught", e);
				}
				String origBody = messageBean.getBody();
				Pattern p = Pattern.compile("^user_name=([\\w\\s]{1,50});");
				Matcher m = p.matcher(origBody);
				if (m.find() && m.groupCount() >= 1) {
					variables.put("_UserName", m.group(1));
				}
				else {
					variables.put("_UserName", vo.getEmailAddr());
				}
			}
			// end of promotion
			TemplateRenderVo renderVo = null;
			try {
				// Mailing List id may have been provided by upstream process (subscribe)
				renderVo = RenderUtil.renderEmailTemplate(vo.getEmailAddr(), variables, taskArguments,
						messageBean.getMailingListId());
			}
			catch (TemplateNotFoundException e) {
				throw new DataValidationException("Email Template not found by Id: "
						+ taskArguments);
			}
			replyBean.setSubject(renderVo.getSubject());
			String body = renderVo.getBody();
			if (renderVo.getEmailTemplateVo() != null && renderVo.getEmailTemplateVo().getIsHtml()) {
				if (Constants.YES_CODE.equals(vo.getAcceptHtml())) {
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
			replyBean.setClientId(messageBean.getClientId());
			if (!StringUtil.isEmpty(messageBean.getClientId())) {
				replyBean.setClientId(messageBean.getClientId());
			}
			else if (!StringUtil.isEmpty(renderVo.getClientId())) {
				replyBean.setClientId(renderVo.getClientId());
			}
			replyBean.setCustId(messageBean.getCustId());
			// set recipient address
			Address[] _to = {_from};
			replyBean.setTo(_to);
			// write to MailSender input queue
			String jmsMsgId = jmsProcessor.writeMsg(replyBean);
			msgsSent++;
			if (isDebugEnabled) {
				logger.debug("Jms Message Id returned: " + jmsMsgId);
				logger.debug("Reply message queued: " + LF + replyBean);
			}
		}
		return Long.valueOf(msgsSent);
	}
	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}
	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
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
}
