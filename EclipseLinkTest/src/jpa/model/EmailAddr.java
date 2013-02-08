package jpa.model;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="email_addr")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="EmailAddrEntiry",
		entities={
		 @EntityResult(entityClass=EmailAddr.class),
	  	}),
	  @SqlResultSetMapping(name="EmailAddrWithCounts",
		entities={
		 @EntityResult(entityClass=EmailAddr.class),
	  	},
	  	columns={
		 @ColumnResult(name="sentCount"),
		 @ColumnResult(name="openCount"),
		 @ColumnResult(name="clickCount"),
	  	}),
	})

public class EmailAddr extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -6508051650541209578L;

	@Transient
	public static final String MAPPING_EMAIL_ADDR_ENTITY = "EmailAddrEntiry";
	@Transient
	public static final String MAPPING_EMAIL_ADDR_WITH_COUNTS = "EmailAddrWithCounts";

	@Column(nullable=false, length=255, unique=true)
	private String address = "";
	@Column(nullable=true)
	private Timestamp statusChangeTime = null;
	@Column(nullable=true, length=20)
 	private String statusChangeUserId = null;
	@Column(nullable=false)
	private int bounceCount = 0;
	@Column(nullable=true)
	private Timestamp lastBounceTime = null;
	@Column(nullable=true)
	private Timestamp lastSentTime = null;
	@Column(nullable=true)
	private Timestamp lastRcptTime= null;
	@Column(nullable=false,length=1,columnDefinition="boolean not null")
	private boolean isAcceptHtml = true;
	@Column(nullable=false, length=255)
	private String origAddress = "";

	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch=FetchType.LAZY, optional=true, mappedBy="emailAddr")
	private CustomerData customerData;
	
	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch=FetchType.LAZY,optional=true, mappedBy="emailAddr")
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

	// As the table already has a column called OrigAddress, use currAddress to avoid confusion.
	@Transient
	private String currAddress = null;

	public EmailAddr() {
		// must have a no-argument constructor
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOrigAddress() {
		return origAddress;
	}

	public void setOrigAddress(String origAddress) {
		this.origAddress = origAddress;
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

	public boolean isAcceptHtml() {
		return isAcceptHtml;
	}

	public void setAcceptHtml(boolean isAcceptHtml) {
		this.isAcceptHtml = isAcceptHtml;
	}

	public CustomerData getCustomerData() {
		return customerData;
	}

	public void setCustomerData(CustomerData customerData) {
		this.customerData = customerData;
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

	public String getCurrAddress() {
		return currAddress;
	}

	public void setCurrAddress(String currAddress) {
		this.currAddress = currAddress;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
}
