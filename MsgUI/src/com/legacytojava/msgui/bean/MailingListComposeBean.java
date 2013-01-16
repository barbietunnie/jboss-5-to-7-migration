package com.legacytojava.msgui.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.BodypartBean;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.bo.mailsender.MessageBodyBuilder;
import com.legacytojava.message.bo.template.RenderUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailingListDeliveryOption;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.emailaddr.EmailTemplateDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.dao.user.SessionUploadDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.HtmlTags;
import com.legacytojava.message.vo.SessionUploadVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.message.vo.emailaddr.TemplateRenderVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class MailingListComposeBean {
	static final Logger logger = Logger.getLogger(MailingListComposeBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	private String listId = null;
	private String msgSubject = null;
	private String msgBody = null;
	private boolean isHtml = true;
	private String embedEmailId = " "; // use system default
	private String renderedBody = null;
	private String renderedSubj = null;
	private String templateId =null;
	
	private UIInput templateIdInput = null;

	private MailingListDao mailingListDao = null;
	private SessionUploadDao sessionUploadDao = null;
	private EmailTemplateDao emailTemplateDao = null;
	private String deliveryOption = null;
	private EmailAddrDao emailAddrDao = null;
	private List<SessionUploadVo> uploads = null;
	
	private String actionFailure = null;

	private static String TO_FAILED = "mailinglist.failed";
	private static String TO_CANCELED = "mailinglist.canceled";
	private static String TO_SENT = "mailinglist.sent";
	private static String TO_PREVIEW = "mailinglist.preview";
	
	public MailingListComposeBean() {
		//
	}
	
	public SessionUploadDao getSessionUploadDao() {
		if (sessionUploadDao == null) {
			sessionUploadDao = (SessionUploadDao) SpringUtil.getWebAppContext().getBean(
					"sessionUploadDao");
		}
		return sessionUploadDao;
	}
	
	public MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext().getBean(
					"mailingListDao");
		}
		return mailingListDao;
	}
	

	public EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateDao) SpringUtil.getWebAppContext().getBean(
					"emailTemplateDao");
		}
		return emailTemplateDao;
	}

	public EmailAddrDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getWebAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	private void reset() {
		actionFailure = null;
	}
	
	private void clearUploads() {
		String sessionId = FacesUtil.getSessionId();
		if (uploads != null) {
			uploads.clear();
		}
		int rowsDeleted = getSessionUploadDao().deleteBySessionId(sessionId);
		logger.info("clearUploads() - SessionId: " + sessionId + ", rows deleted: " + rowsDeleted);
		uploads = null;
	}
	
	public String attachFiles() {
		String pageUrl = "/upload/msgInboxAttachFiles.jsp?frompage=mailinglist";
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		try {
			ectx.redirect(ectx.encodeResourceURL(ectx.getRequestContextPath() + pageUrl));
		}
		catch (IOException e) {
			logger.error("attachFiles() - IOException caught", e);
			throw new FacesException("Cannot redirect to " + pageUrl + " due to IO exception.", e);
		}
		return null;
	}
	
	public List<SessionUploadVo> retrieveUploadFiles() {
		String sessionId = FacesUtil.getSessionId();
		boolean valid = FacesUtil.isSessionIdValid();
		logger.info("retrieveUploadFiles() - SessionId: " + sessionId + ", Valid? " + valid);
		uploads = getSessionUploadDao().getBySessionId4Web(sessionId);
		if (isDebugEnabled && uploads != null)
			logger.debug("retrieveUploadFiles() - files retrieved: " + uploads.size());
		return uploads;
	}
	
	public String removeUploadFile() {
		String seq = FacesUtil.getRequestParameter("seq");
		String name = FacesUtil.getRequestParameter("name");
		String id = FacesUtil.getSessionId();
		logger.info("removeUploadFile() - id/seq/name: " + id + "/" + seq + "/" + name);
		try {
			int sessionSeq = Integer.parseInt(seq);
			for (int i = 0; uploads != null && i < uploads.size(); i++) {
				SessionUploadVo vo = uploads.get(i);
				if (sessionSeq == vo.getSessionSeq()) {
					uploads.remove(i);
					break;
				}
			}
			int rowsDeleted = getSessionUploadDao().deleteByPrimaryKey(id, sessionSeq);
			logger.info("removeUploadFile() - rows deleted: " + rowsDeleted + ", file name: "
					+ name);
		}
		catch (RuntimeException e) {
			logger.error("RuntimeException caught", e);
		}
		return null;
	}
	
	public String copyFromTemplate() {
		String id = (String) templateIdInput.getSubmittedValue();
		EmailTemplateVo vo = getEmailTemplateDao().getByTemplateId(id);
		if (vo != null) {
			listId = vo.getListId();
			msgSubject = vo.getSubject();
			msgBody = vo.getBodyText();
			isHtml = vo.getIsHtml();
			isHtml = isHtml == false ? HtmlTags.isHTML(msgBody) : isHtml;
			embedEmailId = vo.getEmbedEmailId();
			deliveryOption = vo.getDeliveryOption();
		}
		else {
			logger.error("copyFromTemplate() - template not found by templateId: " + templateId);
		}
		return "mailinglist.copytemplate";
	}
	
	public String sendMessage() {
		logger.info("sendMessage() - Mailing List Selected: " + listId);
		reset();
		// validate variable loops
		try {
			RenderUtil.checkVariableLoop(msgBody);
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		try {
			RenderUtil.checkVariableLoop(msgSubject);
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		// make sure we have all the data to build a message bean
		try {
			MailingListVo listVo = getMailingListDao().getByListId(listId);
			if (listVo == null) {
				logger.error("sendMessage() - Unexpected Internal Error occurred...");
				throw new IllegalStateException("mailingList is null");
			}
			// retrieve new addresses
			Address[] from = InternetAddress.parse(listVo.getEmailAddr());
			Address[] to = InternetAddress.parse(listVo.getEmailAddr());
			// retrieve new message body
			msgBody = msgBody == null ? "" : msgBody; // just for safety
			// construct messageBean for new message
			MessageBean mBean = new MessageBean();
			mBean.setMailingListId(listId);
			mBean.setRuleName(RuleNameType.BROADCAST.toString());
			if (Constants.YES_CODE.equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(true));
			}
			else if (Constants.NO_CODE.equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(false));
			}
			if (MailingListDeliveryOption.CUSTOMERS_ONLY.equals(deliveryOption)) {
				mBean.setToCustomersOnly(true);
			}
			else if (MailingListDeliveryOption.PROSPECTS_ONLY.equals(deliveryOption)) {
				mBean.setToProspectsOnly(true);
			}
			String contentType = "text/plain";
			isHtml = isHtml == false ? HtmlTags.isHTML(msgBody) : isHtml;
			if (isHtml) {
				contentType = "text/html";
			}
			// retrieve upload files
			String sessionId = FacesUtil.getSessionId();
			List<SessionUploadVo> list = getSessionUploadDao().getBySessionId(sessionId);
			if (list != null && list.size() > 0) {
				// construct multipart
				mBean.setContentType("multipart/mixed");
				// message body part
				BodypartBean aNode = new BodypartBean();
				aNode.setContentType(contentType);
				aNode.setValue(msgBody);
				aNode.setSize(msgBody.length());
				mBean.put(aNode);
				// message attachments
				for (int i = 0; i < list.size(); i++) {
					SessionUploadVo vo = list.get(i);
					BodypartBean subNode = new BodypartBean();
					subNode.setContentType(vo.getContentType());
					subNode.setDisposition(Part.ATTACHMENT);
					subNode.setDescription(vo.getFileName());
					byte[] bytes = vo.getSessionValue();
					subNode.setValue(bytes);
					if (bytes != null) {
						subNode.setSize(bytes.length);
					}
					else {
						subNode.setSize(0);
					}
					mBean.put(subNode);
					mBean.updateAttachCount(1);
					mBean.getComponentsSize().add(Integer.valueOf(subNode.getSize()));
				}
				// remove uploaded files from session table
				clearUploads();
			}
			else {
				mBean.setContentType(contentType);
				mBean.setBody(msgBody);
			}
			// set addresses and subject
			mBean.setFrom(from);
			mBean.setTo(to);
			mBean.setSubject(msgSubject);
			// process the message
			TaskBaseBo taskBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(
					"assignRuleNameBo");
			taskBo.setTaskArguments(mBean.getRuleName());
			Object mailsSent = taskBo.process(mBean);
			if (mailsSent != null && mailsSent instanceof Long) {
				logger.info("sendMessage() - Broadcast Message queued: " + mailsSent);
				if (isDebugEnabled)
					logger.debug("sendMessage() - Broadcast message: " + LF + mBean);
			}
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
			return TO_FAILED;
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return TO_FAILED;
		}
		return TO_SENT;
	}
	
	public String cancelSend() {
		return TO_CANCELED;
	}
	
	public String previewMsgBody() {
		try {
			// build variable values using the first email address found in EmailAddr table.
			long previewAddrId = getEmailAddrDao().getEmailAddrIdForPreview();
			// include mailing list variables
			EmailAddrVo addrVo = getEmailAddrDao().getByAddrId(previewAddrId);
			String previewAddr = "1";
			if (addrVo != null) {
				previewAddr = addrVo.getEmailAddr();
			}
			TemplateRenderVo renderVo = RenderUtil.renderEmailText(previewAddr, null, msgSubject,
					msgBody, listId);
			renderedBody = getDisplayBody(renderVo.getBody());
			renderedSubj = renderVo.getSubject();
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		}
		return TO_PREVIEW;
	}
	
	private String getDisplayBody(String bodytext) {
		if (bodytext == null) return null;
		if (isHtml) {
			return MessageBodyBuilder.removeHtmlBodyTags(bodytext);
		}
		else {
			return EmailAddrUtil.getHtmlDisplayText(bodytext);
		}
	}

	/**
	 * Validate FROM email address
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateFromAddress(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateFromAddress() - From Address: " + value);
		String fromAddr = (String) value;
		if (!isValidEmailAddress(fromAddr)) {
			// invalid email address
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	private boolean isValidEmailAddress(String addrs) {
		List<String> list = getAddressList(addrs);
		for (int i = 0; i < list.size(); i++) {
			if (!EmailAddrUtil.isRemoteOrLocalEmailAddress(list.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	private List<String> getAddressList(String addrs) {
		List<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(addrs, ",");
		while (st.hasMoreTokens()) {
			String addr = st.nextToken();
			list.add(EmailAddrUtil.removeDisplayName(addr, true));
		}
		return list;
	}
	
	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public List<SessionUploadVo> getUploads() {
		//if (uploads == null)
			retrieveUploadFiles();
		return uploads;
	}

	public void setUploads(List<SessionUploadVo> uploads) {
		this.uploads = uploads;
	}
	
	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public String getEmbedEmailId() {
		return embedEmailId;
	}

	public void setEmbedEmailId(String embedEmailId) {
		this.embedEmailId = embedEmailId;
	}

	public String getRenderedBody() {
		return renderedBody;
	}

	public void setRenderedBody(String renderedBody) {
		this.renderedBody = renderedBody;
	}

	public String getRenderedSubj() {
		return renderedSubj;
	}

	public void setRenderedSubj(String renderedSubj) {
		this.renderedSubj = renderedSubj;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public UIInput getTemplateIdInput() {
		return templateIdInput;
	}

	public void setTemplateIdInput(UIInput templateIdInput) {
		this.templateIdInput = templateIdInput;
	}

	public String getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}
}