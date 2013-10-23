package jpa.msgui.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.CodeType;
import jpa.constant.MailingListDeliveryType;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.BodypartBean;
import jpa.message.MessageBean;
import jpa.message.MessageBodyBuilder;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.EmailTemplate;
import jpa.model.EmailVariable;
import jpa.model.MailingList;
import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.EmailAddressService;
import jpa.service.EmailTemplateService;
import jpa.service.EmailVariableService;
import jpa.service.MailingListService;
import jpa.service.SessionUploadService;
import jpa.service.msgin.EmailTemplateBo;
import jpa.service.msgin.TemplateRenderVo;
import jpa.service.task.TaskBaseBo;
import jpa.util.EmailAddrUtil;
import jpa.util.HtmlUtil;
import jpa.util.StringUtil;
import jpa.variable.RenderUtil;

import org.apache.log4j.Logger;

@ManagedBean(name="mailingListCompose")
@SessionScoped
public class MailingListComposeBean implements java.io.Serializable {
	private static final long serialVersionUID = -2015576038292544848L;
	static final Logger logger = Logger.getLogger(MailingListComposeBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	private String listId = null;
	private String msgSubject = null;
	private String msgBody = null;
	private boolean isHtml = true;
	private Boolean embedEmailId = null; // use system default
	private String renderedBody = null;
	private String renderedSubj = null;
	private String templateId =null;
	
	private UIInput templateIdInput = null;

	private MailingListService mailingListDao = null;
	private SessionUploadService sessionUploadDao = null;
	private EmailTemplateService emailTemplateDao = null;
	private EmailVariableService emailVariableDao = null;
	private EmailTemplateBo emailTemplateBo = null;
	private String deliveryOption = null;
	private EmailAddressService emailAddrDao = null;
	private List<SessionUpload> uploads = null;
	
	private String actionFailure = null;

	private static String TO_FAILED = "mailinglist.failed";
	private static String TO_CANCELED = "mailinglist.canceled";
	private static String TO_SENT = "mailinglist.sent";
	private static String TO_PREVIEW = "mailinglist.preview";
	
	public MailingListComposeBean() {
		//
	}
	
	public SessionUploadService getSessionUploadService() {
		if (sessionUploadDao == null) {
			sessionUploadDao = (SessionUploadService) SpringUtil.getWebAppContext().getBean(
					"sessionUploadService");
		}
		return sessionUploadDao;
	}
	
	public MailingListService getMailingListService() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListService) SpringUtil.getWebAppContext().getBean(
					"mailingListService");
		}
		return mailingListDao;
	}
	

	public EmailTemplateService getEmailTemplateService() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateService) SpringUtil.getWebAppContext().getBean(
					"emailTemplateService");
		}
		return emailTemplateDao;
	}

	public EmailVariableService getEmailVariableService() {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableService) SpringUtil.getWebAppContext().getBean(
					"emailVariableService");
		}
		return emailVariableDao;
	}

	public EmailTemplateBo getEmailTemplateBo() {
		if (emailTemplateBo == null) {
			emailTemplateBo = (EmailTemplateBo) SpringUtil.getWebAppContext().getBean(
					"emailTemplateBo");
		}
		return emailTemplateBo;
	}

	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddressService) SpringUtil.getWebAppContext().getBean(
					"emailAddressService");
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
		int rowsDeleted = getSessionUploadService().deleteBySessionId(sessionId);
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
	
	public List<SessionUpload> retrieveUploadFiles() {
		String sessionId = FacesUtil.getSessionId();
		boolean valid = FacesUtil.isSessionIdValid();
		logger.info("retrieveUploadFiles() - SessionId: " + sessionId + ", Valid? " + valid);
		uploads = getSessionUploadService().getBySessionId(sessionId);
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
				SessionUpload vo = uploads.get(i);
				if (sessionSeq == vo.getSessionUploadPK().getSessionSequence()) {
					uploads.remove(i);
					break;
				}
			}
			SessionUploadPK pk = new SessionUploadPK(id, sessionSeq);
			int rowsDeleted = getSessionUploadService().deleteByPrimaryKey(pk);
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
		EmailTemplate vo = getEmailTemplateService().getByTemplateId(id);
		if (vo != null) {
			listId = vo.getMailingList().getListId();
			msgSubject = vo.getSubject();
			msgBody = vo.getBodyText();
			isHtml = vo.isHtml();
			isHtml = isHtml == false ? HtmlUtil.isHTML(msgBody) : isHtml;
			embedEmailId = vo.getIsEmbedEmailId();
			deliveryOption = vo.getDeliveryOption();
		}
		else {
			logger.error("copyFromTemplate() - template not found by templateId: " + templateId);
		}
		return "mailinglist.copytemplate";
	}
	
	private void checkVariableLoop(String text) throws DataValidationException {
		List<String> varNames = RenderUtil.retrieveVariableNames(text);
		for (String loopName : varNames) {
			EmailVariable vo = getEmailVariableService().getByVariableName(loopName);
			if (vo != null) {
				RenderUtil.checkVariableLoop(vo.getDefaultValue(), loopName);
			}
		}
	}

	public String sendMessage() {
		logger.info("sendMessage() - Mailing List Selected: " + listId);
		reset();
		// validate variable loops
		try {
			checkVariableLoop(msgBody);
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		try {
			checkVariableLoop(msgSubject);
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		// make sure we have all the data to build a message bean
		try {
			MailingList listVo = getMailingListService().getByListId(listId);
			if (listVo == null) {
				logger.error("sendMessage() - Unexpected Internal Error occurred...");
				throw new IllegalStateException("mailingList is null");
			}
			// retrieve new addresses
			Address[] from = InternetAddress.parse(listVo.getListEmailAddr());
			Address[] to = InternetAddress.parse(listVo.getListEmailAddr());
			// retrieve new message body
			msgBody = msgBody == null ? "" : msgBody; // just for safety
			// construct messageBean for new message
			MessageBean mBean = new MessageBean();
			mBean.setMailingListId(listId);
			mBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
			if (CodeType.YES_CODE.getValue().equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(true));
			}
			else if (CodeType.NO_CODE.getValue().equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(false));
			}
			if (MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue().equals(deliveryOption)) {
				mBean.setToSubscribersOnly(true);
			}
			else if (MailingListDeliveryType.PROSPECTS_ONLY.getValue().equals(deliveryOption)) {
				mBean.setToProspectsOnly(true);
			}
			String contentType = "text/plain";
			isHtml = isHtml == false ? HtmlUtil.isHTML(msgBody) : isHtml;
			if (isHtml) {
				contentType = "text/html";
			}
			// retrieve upload files
			String sessionId = FacesUtil.getSessionId();
			List<SessionUpload> list = getSessionUploadService().getBySessionId(sessionId);
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
					SessionUpload vo = list.get(i);
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
					"assignRuleName");
			MessageContext ctx = new MessageContext(mBean);
			ctx.setTaskArguments(mBean.getRuleName());
			taskBo.process(ctx);
			List<Integer> mailsSent = ctx.getRowIds();
			if (mailsSent != null && !mailsSent.isEmpty()) {
				logger.info("sendMessage() - Broadcast Message queued: " + mailsSent.size());
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
			int previewAddrId = getEmailAddressService().getRowIdForPreview();
			// include mailing list variables
			EmailAddress addrVo = getEmailAddressService().getByRowId(previewAddrId);
			String previewAddr = "1";
			if (addrVo != null) {
				previewAddr = addrVo.getAddress();
			}
			TemplateRenderVo renderVo = getEmailTemplateBo().renderEmailText(previewAddr, null, msgSubject,
					msgBody, listId);
			renderedBody = getDisplayBody(renderVo.getBody());
			renderedSubj = renderVo.getSubject();
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		} catch (TemplateException e) {
			logger.error("TemplateException caught", e);
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
			return StringUtil.getHtmlDisplayText(bodytext);
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
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidEmailAddress", null);
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

	public List<SessionUpload> getUploads() {
		//if (uploads == null)
			retrieveUploadFiles();
		return uploads;
	}

	public void setUploads(List<SessionUpload> uploads) {
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

	public Boolean getEmbedEmailId() {
		return embedEmailId;
	}

	public void setEmbedEmailId(Boolean embedEmailId) {
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