package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.Constants;

@Entity
@Table(name="email_addr")
public class EmailAddr extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -6508051650541209578L;

	@Column(nullable=false, length=255)
	private String emailAddr = "";
	@Column(nullable=true)
	private Timestamp statusChangeTime = null;
	@Column(nullable=true, length=20) // TODO
 	private String statusChangeUserId = null;
	@Column(nullable=false)
	private int bounceCount = 0;
	@Column(nullable=true)
	private Timestamp lastBounceTime = null;
	@Column(nullable=true)
	private Timestamp lastSentTime = null;
	@Column(nullable=true)
	private Timestamp lastRcptTime= null;
	@Column(nullable=false,length=1,columnDefinition="char")
	private String acceptHtml = Constants.Code.YES_CODE.getValue();

	@OneToOne(fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="CustomerDataRowId", referencedColumnName="Row_Id")
	private CustomerData costomerData;
	
	@OneToOne(fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="userDataRowId", referencedColumnName="Row_Id")
	private UserData userData;

	//TODO
	// used when join with MsgInbox table
	@Transient
	private String ruleName = null;
	@Transient
	private String firstName = null;
	@Transient
	private String lastName = null;
	@Transient
	private String middleName = null;

	//TODO
	// used when join with Subscription table to get open/click counts
	@Transient
	private Integer sentCount = null;
	@Transient
	private Integer openCount = null;
	@Transient
	private Integer clickCount = null;
	
	// As the table already has a column called OrigEmailAddr, use currEmailAddr to avoid confusion.
	@Transient
	private String currEmailAddr = null;

	public EmailAddr() {
		// must have a no-argument constructor
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
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

	public Timestamp getLastSentTime() {
		return lastSentTime;
	}

	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}

	public Timestamp getLastRcptTime() {
		return lastRcptTime;
	}

	public void setLastRcptTime(Timestamp lastRcptTime) {
		this.lastRcptTime = lastRcptTime;
	}

	public String getAcceptHtml() {
		return acceptHtml;
	}

	public void setAcceptHtml(String acceptHtml) {
		this.acceptHtml = acceptHtml;
	}

	public CustomerData getCostomerData() {
		return costomerData;
	}

	public void setCostomerData(CustomerData costomerData) {
		this.costomerData = costomerData;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
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

	public String getCurrEmailAddr() {
		return currEmailAddr;
	}

	public void setCurrEmailAddr(String currEmailAddr) {
		this.currEmailAddr = currEmailAddr;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
}
