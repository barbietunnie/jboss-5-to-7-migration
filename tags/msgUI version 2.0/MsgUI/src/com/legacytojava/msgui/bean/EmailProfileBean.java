package com.legacytojava.msgui.bean;

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

import org.apache.log4j.Logger;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.client.ClientDao;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class EmailProfileBean {
	static final Logger logger = Logger.getLogger(EmailProfileBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private ClientDao clientDao = null;
	private DataModel siteProfiles = null;
	private ClientVo client = null;
	private boolean editMode = true;
	
	private UIInput clientIdInput = null;
	private UIInput returnPathLeftInput = null;
	private UIInput verpEnabledInput = null;
	private UIInput useTestAddrInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private final ClientVo siteMeta = new ClientVo();
	
	public EmailProfileBean() {
		getData();
	}
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (siteProfiles == null) {
			List<ClientVo> clientList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				clientList = getClientDao().getAllForTrial();
			}
			else {
				clientList = getClientDao().getAll();
			}
			List<ClientVo> systemAtTop = new ArrayList<ClientVo>();
			for (int i = 0; i < clientList.size(); i++) {
				ClientVo vo = clientList.get(i);
				if (Constants.DEFAULT_CLIENTID.equals(vo.getClientId())) {
					systemAtTop.add(vo);
					clientList.remove(i);
				}
			}
			systemAtTop.addAll(clientList);
			siteProfiles = new ListDataModel(systemAtTop);
		}
		return siteProfiles;
	}
	
	public ClientVo getData() {
		client = getClientDao().getByClientId(Constants.DEFAULT_CLIENTID);
		reset();
		return client;
	}

	public String refresh() {
		siteProfiles = null;
		return "";
	}
	
	public String refreshClient() {
		getData();
		FacesUtil.refreshCurrentJSFPage();
		return null;
	}
	
	public ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao) SpringUtil.getWebAppContext().getBean("clientDao");
		}
		return clientDao;
	}

	public void setClientDao(ClientDao clientDao) {
		this.clientDao = clientDao;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		clientIdInput = null;
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
		this.client = (ClientVo) siteProfiles.getRowData();
		logger.info("viewSiteProfile() - Site to be edited: " + client.getClientId());
		client.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - ClientVo to be passed to jsp: " + client);
		
		return "siteprofile.edit";
	}
	
	public String saveClient() {
		if (isDebugEnabled)
			logger.debug("saveClient() - Entering...");
		if (client == null) {
			logger.warn("saveClient() - ClientVo is null.");
			return "siteprofile.failed";
		}
		reset();
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			client.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getClientDao().update(client);
			logger.info("saveClient() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getClientDao().insert(client);
			if (rowsInserted > 0)
				addToList(client);
			logger.info("saveClient() - Rows Inserted: " + rowsInserted);
		}
		return "siteprofile.saved";
	}

	@SuppressWarnings("unchecked")
	private void addToList(ClientVo vo) {
		List<ClientVo> list = (List<ClientVo>) siteProfiles.getWrappedData();
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
		List<ClientVo> list = getSiteProfilesList();
		for (int i=0; i<list.size(); i++) {
			ClientVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getClientDao().delete(vo.getClientId());
				if (rowsDeleted > 0) {
					logger.info("deleteSiteProfiles() - Client deleted: " + vo.getClientId());
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
			logger.warn("copySiteProfile() - Client List is null.");
			return "siteprofile.failed";
		}
		reset();
		List<ClientVo> mailList = getSiteProfilesList();
		for (int i=0; i<mailList.size(); i++) {
			ClientVo vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.client = (ClientVo) vo.getClone();
					client.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.client = new ClientVo();
				}
				client.setClientId(null);
				client.setMarkedForEdition(true);
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
		this.client = new ClientVo();
		client.setMarkedForEdition(true);
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
			logger.warn("getAnySitesMarkedForDeletion() - Client List is null.");
			return false;
		}
		List<ClientVo> mailList = getSiteProfilesList();
		for (Iterator<ClientVo> it=mailList.iterator(); it.hasNext();) {
			ClientVo vo = it.next();
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
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
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
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailLocalPart", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String clientId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - clientId: " + clientId);
		ClientVo vo = getClientDao().getByClientId(clientId);
		if (editMode == true && vo != null && client != null && vo.getRowId() != client.getRowId()) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
	        		"com.legacytojava.msgui.messages", "siteProfileAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// mailingList already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "siteProfileAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public boolean getIsVerpEnabledInput() {
		if (verpEnabledInput != null) {
			if (verpEnabledInput.getLocalValue() != null) {
				return Constants.YES.equals(verpEnabledInput.getLocalValue());
			}
			else if (verpEnabledInput.getValue() != null) {
				return Constants.YES.equals(verpEnabledInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseTestAddrInput() {
		if (useTestAddrInput != null) {
			if (useTestAddrInput.getLocalValue() != null) {
				return Constants.YES.equals(useTestAddrInput.getLocalValue());
			}
			else if (useTestAddrInput.getValue() != null) {
				return Constants.YES.equals(useTestAddrInput.getValue());
			}
		}
		return true; // for safety
	}
	
	@SuppressWarnings("unchecked")
	private List<ClientVo> getSiteProfilesList() {
		if (siteProfiles == null) {
			return new ArrayList<ClientVo>();
		}
		else {
			return (List<ClientVo>)siteProfiles.getWrappedData();
		}
	}
	
	public ClientVo getClient() {
		return client;
	}

	public void setClient(ClientVo client) {
		this.client = client;
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

	public DataModel getSiteProfiles() {
		return siteProfiles;
	}

	public void setSiteProfiles(DataModel siteProfiles) {
		this.siteProfiles = siteProfiles;
	}

	public UIInput getClientIdInput() {
		return clientIdInput;
	}

	public void setClientIdInput(UIInput clientIdInput) {
		this.clientIdInput = clientIdInput;
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

	public ClientVo getSiteMeta() {
		return siteMeta;
	}
}
