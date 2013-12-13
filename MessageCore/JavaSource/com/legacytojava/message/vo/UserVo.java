package com.legacytojava.message.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.legacytojava.message.constant.Constants;

public class UserVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = 5066188503284566018L;
	private String userId = "";
	private String password = "";
	private String sessionId = null;
	private String firstName = null;
	private String lastName = null;
	private String middleInit = null;
	private Timestamp createTime;
	private Timestamp lastVisitTime;
	private int hits = 0;
	private String role = "";
	private String emailAddr = null;
	
	private String defaultFolder = null;
	private String defaultRuleName = null;
	private String defaultToAddr = null;
	private String clientId = "";
	
	/** define UI components */
	public void addHit() {
		hits++;
		lastVisitTime = new Timestamp(new Date().getTime());
	}
	
	public String getEmailAddrShort() {
		return StringUtils.left(emailAddr, 30);
	}
	
	public boolean getIsAdmin() {
		return Constants.ADMIN_ROLE.equals(role);
	}
	/** end of UI components */
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMiddleInit() {
		return middleInit;
	}
	public void setMiddleInit(String middleInit) {
		this.middleInit = middleInit;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getLastVisitTime() {
		return lastVisitTime;
	}
	public void setLastVisitTime(Timestamp lastVisitTime) {
		this.lastVisitTime = lastVisitTime;
	}
	public int getHits() {
		return hits;
	}
	public void setHits(int hits) {
		this.hits = hits;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getDefaultFolder() {
		return defaultFolder;
	}

	public void setDefaultFolder(String defaultFolder) {
		this.defaultFolder = defaultFolder;
	}

	public String getDefaultRuleName() {
		return defaultRuleName;
	}

	public void setDefaultRuleName(String defaultRuleName) {
		this.defaultRuleName = defaultRuleName;
	}

	public String getDefaultToAddr() {
		return defaultToAddr;
	}

	public void setDefaultToAddr(String defaultToAddr) {
		this.defaultToAddr = defaultToAddr;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}