package com.es.vo.comm;

import java.io.Serializable;

import com.es.data.constant.CarrierCode;
import com.es.data.constant.MailServerType;

public class MailBoxVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = 826439429623556631L;
	private String userId = ""; 
	private String userPswd = "";
	private String hostName = "";
	private int portNumber = -1;
	private String protocol = "";
	private String serverType = MailServerType.SMTP.getValue();
	private String folderName = "INBOX";
	private String mailBoxDesc = null;
	private String carrierCode = CarrierCode.SMTPMAIL.getValue();
	private String internalOnly = null;
	private int readPerPass = -1;
	private String useSsl = "";
	private Integer retryMax = null;
	private Integer minimumWait = null;
	private String toPlainText = null;
	private String toAddrDomain = null;
	private String checkDuplicate = null;
	private String alertDuplicate = null;
	private String logDuplicate = null;
	private Integer purgeDupsAfter = null;
	
	// used to tell if it's from batch or EJB timer
	private boolean fromTimer = false; // default to batch
	
	/* This field is no longer used by MailReader. */
	public String getProcessorName() {
		// returns a dummy value.
		return "mailProcessor";
	}
	
	public String getAlertDuplicate() {
		return alertDuplicate;
	}
	public void setAlertDuplicate(String alertDuplicate) {
		this.alertDuplicate = alertDuplicate;
	}
	public String getCarrierCode() {
		return carrierCode;
	}
	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}
	public String getCheckDuplicate() {
		return checkDuplicate;
	}
	public void setCheckDuplicate(String checkDuplicate) {
		this.checkDuplicate = checkDuplicate;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getLogDuplicate() {
		return logDuplicate;
	}
	public void setLogDuplicate(String logDuplicate) {
		this.logDuplicate = logDuplicate;
	}
	public String getMailBoxDesc() {
		return mailBoxDesc;
	}
	public void setMailBoxDesc(String mailBoxDesc) {
		this.mailBoxDesc = mailBoxDesc;
	}
	public Integer getMinimumWait() {
		return minimumWait;
	}
	public void setMinimumWait(Integer minimumWait) {
		this.minimumWait = minimumWait;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public Integer getPurgeDupsAfter() {
		return purgeDupsAfter;
	}
	public void setPurgeDupsAfter(Integer purgeDupsAfter) {
		this.purgeDupsAfter = purgeDupsAfter;
	}
	public int getReadPerPass() {
		return readPerPass;
	}
	public void setReadPerPass(int readPerPass) {
		this.readPerPass = readPerPass;
	}
	public Integer getRetryMax() {
		return retryMax;
	}
	public void setRetryMax(Integer retryMax) {
		this.retryMax = retryMax;
	}
	public String getToAddrDomain() {
		return toAddrDomain;
	}
	public void setToAddrDomain(String toAddrDomain) {
		this.toAddrDomain = toAddrDomain;
	}
	public String getToPlainText() {
		return toPlainText;
	}
	public void setToPlainText(String toPlainText) {
		this.toPlainText = toPlainText;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserPswd() {
		return userPswd;
	}
	public void setUserPswd(String userPswd) {
		this.userPswd = userPswd;
	}
	public String getUseSsl() {
		return useSsl;
	}
	public void setUseSsl(String useSsl) {
		this.useSsl = useSsl;
	}

	public String getInternalOnly() {
		return internalOnly;
	}

	public void setInternalOnly(String internalOnly) {
		this.internalOnly = internalOnly;
	}

	public boolean isFromTimer() {
		return fromTimer;
	}

	public void setFromTimer(boolean fromTimer) {
		this.fromTimer = fromTimer;
	}
}