package jpa.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="customer_data")
public class CustomerData extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -2242214285799087578L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ClientRowId", referencedColumnName="Row_Id", columnDefinition="int")
	private ClientData clientData;

	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch=FetchType.LAZY, mappedBy="customerData")
	private List<EmailAddr> emailAddrs;

	@Column(nullable=false, length=20)
	private String customerId = "";
	@Column(length=11)
	private String ssnNumber = null;
	@Column(length=10)
	private String taxId = null;
	@Column(length=40)
	private String profession = null;
	@Column(length=32)
	private String firstName = null;
	@Column(length=32)
	private String middleName = null;
	@Column(length=32, nullable=false)
	private String lastName = "";
	@Column(length=50)
	private String alias = null;
	@Column(length=60)
	private String streetAddress = null;
	@Column(length=40)
	private String streetAddress2 = null;
	@Column(length=32)
	private String cityName = null;
	@Column(length=2,columnDefinition="char(2)")
	private String stateCode = null;
	@Column(length=5, columnDefinition="char(5)")
	private String zipCode5 = null;
	@Column(length=4)
	private String zipCode4 = null;
	@Column(length=30)
	private String provinceName = null;
	@Column(length=11)
	private String postalCode = null;
	@Column(length=30)
	private String country = null;
	@Column(length=18)
	private String dayPhone = null;
	@Column(length=18)
	private String eveningPhone = null;
	@Column(length=18)
	private String mobilePhone = null;
	@Column(nullable=true)
	@Temporal(TemporalType.DATE)
	private Date birthDate = null;
	@Column(nullable=false)
	@Temporal(TemporalType.DATE)
	private Date startDate = new Date(System.currentTimeMillis());
	@Column(nullable=true)
	@Temporal(TemporalType.DATE)
	private Date endDate = null;
	@Column(length=18)
	private String faxNumber = null;
	@Column(length=100)
	private String msgHeader = null;
	@Column(length=255)
	private String msgDetail = null;
	@Column(length=100)
	private String msgOptional = null;
	@Column(length=100)
	private String msgFooter = null;
	@Column(length=50)
	private String timeZone = null;
	@Column(length=255)
	private String memoText = null;
	@Column(nullable=true)
	private Timestamp passwordChangeTime = null;
	@Column(length=32)
	private String userPassword = null;
	@Column(length=100)
	private String securityQuestion = null;
	@Column(length=26)
	private String securityAnswer = null;

	@Transient
	private String origCustId = null;

	public CustomerData() {
		// must have a no-argument constructor
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}

	public List<EmailAddr> getEmailAddrs() {
		return emailAddrs;
	}

	public void setEmailAddrs(List<EmailAddr> emailAddrs) {
		this.emailAddrs = emailAddrs;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getSsnNumber() {
		return ssnNumber;
	}

	public void setSsnNumber(String ssnNumber) {
		this.ssnNumber = ssnNumber;
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getStreetAddress2() {
		return streetAddress2;
	}

	public void setStreetAddress2(String streetAddress2) {
		this.streetAddress2 = streetAddress2;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getZipCode5() {
		return zipCode5;
	}

	public void setZipCode5(String zipCode5) {
		this.zipCode5 = zipCode5;
	}

	public String getZipCode4() {
		return zipCode4;
	}

	public void setZipCode4(String zipCode4) {
		this.zipCode4 = zipCode4;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDayPhone() {
		return dayPhone;
	}

	public void setDayPhone(String dayPhone) {
		this.dayPhone = dayPhone;
	}

	public String getEveningPhone() {
		return eveningPhone;
	}

	public void setEveningPhone(String eveningPhone) {
		this.eveningPhone = eveningPhone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getMsgHeader() {
		return msgHeader;
	}

	public void setMsgHeader(String msgHeader) {
		this.msgHeader = msgHeader;
	}

	public String getMsgDetail() {
		return msgDetail;
	}

	public void setMsgDetail(String msgDetail) {
		this.msgDetail = msgDetail;
	}

	public String getMsgOptional() {
		return msgOptional;
	}

	public void setMsgOptional(String msgOptional) {
		this.msgOptional = msgOptional;
	}

	public String getMsgFooter() {
		return msgFooter;
	}

	public void setMsgFooter(String msgFooter) {
		this.msgFooter = msgFooter;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getMemoText() {
		return memoText;
	}

	public void setMemoText(String memoText) {
		this.memoText = memoText;
	}

	public Timestamp getPasswordChangeTime() {
		return passwordChangeTime;
	}

	public void setPasswordChangeTime(Timestamp passwordChangeTime) {
		this.passwordChangeTime = passwordChangeTime;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	public String getOrigCustId() {
		return origCustId;
	}

	public void setOrigCustId(String origCustId) {
		this.origCustId = origCustId;
	}

}
