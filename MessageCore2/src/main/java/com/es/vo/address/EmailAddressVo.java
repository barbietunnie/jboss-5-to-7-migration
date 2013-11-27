package com.es.vo.address;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.es.core.util.StringUtil;
import com.es.data.constant.CodeType;
import com.es.vo.comm.BaseVo;
import com.es.vo.comm.TimestampAdapter;

@XmlAccessorType (XmlAccessType.NONE)
@XmlRootElement(name = "emailAddrVo") 
public class EmailAddressVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 824782085344620557L;
	@XmlElement
	private long emailAddrId = -1;
	@XmlElement
	private String emailAddr = "";
	@XmlElement
	private String origEmailAddr;
	@XmlElement
    @XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp statusChangeTime = null;
    @XmlElement
	private String statusChangeUserId = null;
    @XmlElement
	private int bounceCount = 0;
    @XmlElement
    @XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastBounceTime = null;
    @XmlElement
    @XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastSentTime = null;
    @XmlElement
    @XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastRcptTime= null;
    @XmlElement
	private String acceptHtml = CodeType.YES_CODE.getValue();

    // used when join with MsgInbox table
	private String ruleName = null;
	// used when joining Subscriber table to get customer name
	private String subrId = null;
	private String firstName = null;
	private String lastName = null;
	private String middleName = null;
	// used when join with Subscription table to get open/click counts
	private Integer sentCount = null;
	private Integer openCount = null;
	private Integer clickCount = null;
	
	// As the table already has a column called OrigEmailAddr, use currEmailAddr to avoid confusion. 
	private String currEmailAddr = null;
	
	/** define components for UI */
	public String getEmailAddrShort() {
		return StringUtil.cutWithDots(emailAddr, 80);
	}
	
	public String getAcceptHtmlDesc() {
		return "N".equals(acceptHtml) ? CodeType.NO.getValue() : CodeType.YES.getValue();
	}
	
	public String getCustomerName() {
		return (firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName);
	}
	/** end of UI */
	
	public long getEmailAddrId() {
		return emailAddrId;
	}

	public void setEmailAddrId(long addrId) {
		this.emailAddrId = addrId;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String address) {
		this.emailAddr = address;
	}

	public String getOrigEmailAddr() {
		return origEmailAddr;
	}

	public void setOrigEmailAddr(String origEmailAddr) {
		this.origEmailAddr = origEmailAddr;
	}

	public int getBounceCount() {
		return bounceCount;
	}

	public void setBounceCount(int bounceCount) {
		this.bounceCount = bounceCount;
	}

	public Timestamp getLastBounceTime() {
		return lastBounceTime;
	}

	public void setLastBounceTime(Timestamp lastBounceTime) {
		this.lastBounceTime = lastBounceTime;
	}

	public Timestamp getLastRcptTime() {
		return lastRcptTime;
	}

	public void setLastRcptTime(Timestamp lastRcptTime) {
		this.lastRcptTime = lastRcptTime;
	}

	public Timestamp getLastSentTime() {
		return lastSentTime;
	}

	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}

	public Timestamp getStatusChangeTime() {
		return statusChangeTime;
	}

	public void setStatusChangeTime(Timestamp statusChangeTime) {
		this.statusChangeTime = statusChangeTime;
	}

	public String getStatusChangeUserId() {
		return statusChangeUserId;
	}

	public void setStatusChangeUserId(String statusChangeUserId) {
		this.statusChangeUserId = statusChangeUserId;
	}

	public String getAcceptHtml() {
		return acceptHtml;
	}

	public void setAcceptHtml(String acceptHtml) {
		this.acceptHtml = acceptHtml;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getSubrId() {
		return subrId;
	}

	public void setSubrId(String subrId) {
		this.subrId = subrId;
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
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getCurrEmailAddr() {
		return currEmailAddr;
	}

	public void setCurrEmailAddr(String currEmailAddr) {
		this.currEmailAddr = currEmailAddr;
	}

	public Integer getSentCount() {
		return sentCount;
	}

	public void setSentCount(Integer sentCount) {
		this.sentCount = sentCount;
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}

	public Integer getClickCount() {
		return clickCount;
	}

	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
	}
}