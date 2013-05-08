package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.SenderDataService;
import jpa.util.EmailAddrUtil;
import jpa.util.SenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class EmailProfileBean {
	static final Logger logger = Logger.getLogger(EmailProfileBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private SenderDataService senderDao = null;
	private DataModel<SenderData> siteProfiles = null;
	private SenderData sender = null;
	private boolean editMode = true;
	
	private UIInput senderIdInput = null;
	private UIInput returnPathLeftInput = null;
	private UIInput verpEnabledInput = null;
	private UIInput useTestAddrInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private final SenderData siteMeta = new SenderData();
	
	public EmailProfileBean() {
		getData();
	}
	
	public DataModel<SenderData> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (siteProfiles == null) {
			List<SenderData> senderList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				senderList = getSenderDataService().getAll();
			}
			else {
				senderList = getSenderDataService().getAll();
			}
			List<SenderData> systemAtTop = new ArrayList<SenderData>();
			for (int i = 0; i < senderList.size(); i++) {
				SenderData vo = senderList.get(i);
				if (Constants.DEFAULT_SENDER_ID.equals(vo.getSenderId())) {
					systemAtTop.add(vo);
					senderList.remove(i);
				}
			}
			systemAtTop.addAll(senderList);
			siteProfiles = new ListDataModel<SenderData>(systemAtTop);
		}
		return siteProfiles;
	}
	
	public SenderData getData() {
		sender = getSenderDataService().getBySenderId(Constants.DEFAULT_SENDER_ID);
		reset();
		return sender;
	}

	public String refresh() {
		siteProfiles = null;
		return "";
	}
	
	public String refreshSender() {
		getData();
		FacesUtil.refreshCurrentJSFPage();
		return null;
	}
	
	public SenderDataService getSenderDataService() {
		if (senderDao == null) {
			senderDao = (SenderDataService) SpringUtil.getWebAppContext().getBean("senderDataService");
		}
		return senderDao;
	}

	public void setSenderDataService(SenderDataService senderDao) {
		this.senderDao = senderDao;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		senderIdInput = null;
		returnPathLeftInput = null;
		verpEnabledInput = null;
		useTestAddrInput = null;
	}
	
	public String viewSiteProfile() {
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - Entering...");
		if (siteProfiles == null) {
			logger.warn("viewSiteProfile() - SiteProfile List is null.");
			return "siteprofile.failed";
		}
		if (!siteProfiles.isRowAvailable()) {
			logger.warn("viewMailingList() - SiteProfile Row not available.");
			return "siteprofile.failed";
		}
		reset();
		this.sender = (SenderData) siteProfiles.getRowData();
		logger.info("viewSiteProfile() - Site to be edited: " + sender.getSenderId());
		sender.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - SenderData to be passed to jsp: " + sender);
		
		return "siteprofile.edit";
	}
	
	public String saveSender() {
		if (isDebugEnabled)
			logger.debug("saveSender() - Entering...");
		if (sender == null) {
			logger.warn("saveSender() - SenderData is null.");
			return "siteprofile.failed";
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			sender.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getSenderDataService().update(sender);
			logger.info("saveSender() - Rows Updated: " + 1);
		}
		else {
			getSenderDataService().insert(sender);
			addToList(sender);
			logger.info("saveSender() - Rows Inserted: " + 1);
		}
		return "siteprofile.saved";
	}

	@SuppressWarnings("unchecked")
	private void addToList(SenderData vo) {
		List<SenderData> list = (List<SenderData>) siteProfiles.getWrappedData();
		list.add(vo);
	}
	
	public String deleteSiteProfiles() {
		if (isDebugEnabled)
			logger.debug("deleteSiteProfiles() - Entering...");
		if (siteProfiles == null) {
			logger.warn("deleteSiteProfiles - SiteProfile List is null.");
			return "siteprofile.failed";
		}
		reset();
		List<SenderData> list = getSiteProfilesList();
		for (int i=0; i<list.size(); i++) {
			SenderData vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSenderDataService().deleteBySenderId(vo.getSenderId());
				if (rowsDeleted > 0) {
					logger.info("deleteSiteProfiles() - Sender deleted: " + vo.getSenderId());
				}
				list.remove(vo);
			}
		}
		return "siteprofile.deleted";
	}
	
	public String copySiteProfile() {
		if (isDebugEnabled)
			logger.debug("copySiteProfile() - Entering...");
		if (siteProfiles == null) {
			logger.warn("copySiteProfile() - Sender List is null.");
			return "siteprofile.failed";
		}
		reset();
		List<SenderData> mailList = getSiteProfilesList();
		for (int i=0; i<mailList.size(); i++) {
			SenderData vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.sender = new SenderData();
				try {
					vo.copyPropertiesTo(this.sender);
					sender.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				sender.setSenderId(null);
				sender.setMarkedForEdition(true);
				editMode = false;
				return "siteprofile.edit";
			}
		}
		return null;
	}
	
	public String addSiteProfile() {
		if (isDebugEnabled)
			logger.debug("addSiteProfile() - Entering...");
		reset();
		this.sender = new SenderData();
		sender.setMarkedForEdition(true);
		editMode = false;
		return "siteprofile.edit";
	}
	
	public String cancelEdit() {
		refresh();
		return "siteprofile.canceled";
	}
	
	public boolean getAnySitesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySitesMarkedForDeletion() - Entering...");
		if (siteProfiles == null) {
			logger.warn("getAnySitesMarkedForDeletion() - Sender List is null.");
			return false;
		}
		List<SenderData> mailList = getSiteProfilesList();
		for (Iterator<SenderData> it=mailList.iterator(); it.hasNext();) {
			SenderData vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			// invalid email address
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validateEmailLocalPart(FacesContext context, UIComponent component, Object value) {
		String localPart = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailLocalPart() - local part: " + localPart);
		if (!EmailAddrUtil.isValidEmailLocalPart(localPart)) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailLocalPart", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String senderId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - senderId: " + senderId);
		SenderData vo = getSenderDataService().getBySenderId(senderId);
		if (editMode == true && vo != null && sender != null && vo.getRowId() != sender.getRowId()) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
	        		"com.legacytojava.msgui.messages", "siteProfileAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// mailingList already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "siteProfileAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public boolean getIsVerpEnabledInput() {
		if (verpEnabledInput != null) {
			if (verpEnabledInput.getLocalValue() != null) {
				return CodeType.YES.getValue().equals(verpEnabledInput.getLocalValue());
			}
			else if (verpEnabledInput.getValue() != null) {
				return CodeType.YES.getValue().equals(verpEnabledInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseTestAddrInput() {
		if (useTestAddrInput != null) {
			if (useTestAddrInput.getLocalValue() != null) {
				return CodeType.YES.getValue().equals(useTestAddrInput.getLocalValue());
			}
			else if (useTestAddrInput.getValue() != null) {
				return CodeType.YES.getValue().equals(useTestAddrInput.getValue());
			}
		}
		return true; // for safety
	}
	
	@SuppressWarnings("unchecked")
	private List<SenderData> getSiteProfilesList() {
		if (siteProfiles == null) {
			return new ArrayList<SenderData>();
		}
		else {
			return (List<SenderData>)siteProfiles.getWrappedData();
		}
	}
	
	public SenderData getSender() {
		return sender;
	}

	public void setSender(SenderData client) {
		this.sender = client;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}
	
	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public DataModel<SenderData> getSiteProfiles() {
		return siteProfiles;
	}

	public void setSiteProfiles(DataModel<SenderData> siteProfiles) {
		this.siteProfiles = siteProfiles;
	}

	public UIInput getSenderIdInput() {
		return senderIdInput;
	}

	public void setSenderIdInput(UIInput clientIdInput) {
		this.senderIdInput = clientIdInput;
	}

	public UIInput getVerpEnabledInput() {
		return verpEnabledInput;
	}

	public void setVerpEnabledInput(UIInput verpEnabledInput) {
		this.verpEnabledInput = verpEnabledInput;
	}

	public UIInput getUseTestAddrInput() {
		return useTestAddrInput;
	}

	public void setUseTestAddrInput(UIInput useTestAddrInput) {
		this.useTestAddrInput = useTestAddrInput;
	}

	public UIInput getReturnPathLeftInput() {
		return returnPathLeftInput;
	}

	public void setReturnPathLeftInput(UIInput returnPathLeftInput) {
		this.returnPathLeftInput = returnPathLeftInput;
	}

	public SenderData getSiteMeta() {
		return siteMeta;
	}
}
