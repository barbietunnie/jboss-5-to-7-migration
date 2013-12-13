package com.legacytojava.message.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.legacytojava.message.util.StringUtil;

public class CustomerVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -1028823392338670111L;
	
	private String custId = "";
	private String clientId = "";
	private String ssnNumber = null;
	private String taxId = null;
	private String profession = null;
	private String firstName = null;
	private String middleName = null;
	private String lastName = "";
	private String alias = null;
	private String streetAddress = null;
	private String streetAddress2 = null;
	private String cityName = null;
	private String stateCode = null;
	private String zipCode5 = null;
	private String zipCode4 = null;
	private String provinceName = null;
	private String postalCode = null;
	private String country = null;
	private String dayPhone = null;
	private String eveningPhone = null;
	private String mobilePhone = null;
	private Date birthDate = null;
	private Date startDate = new Date(new java.util.Date().getTime());
	private Date endDate = null;
	private String mobileCarrier = null;
	private String msgHeader = null;
	private String msgDetail = null;
	private String msgOptional = null;
	private String msgFooter = null;
	private String timeZoneCode = null;
	private String memoText = null;
	private String emailAddr = "";
	private long emailAddrId = -1;
	private String prevEmailAddr = null;
	private Timestamp passwordChangeTime = null;
	private String userPassword = null;
	private String securityQuestion = null;
	private String securityAnswer = null;
	
	private String origCustId = null;
	
	public CustomerVo() {
		super();
	}
	
	/** define components for UI */
	public String getEmailAddrShort() {
		return StringUtil.cutWithDots(emailAddr, 60);
	}
	/** end of UI */
	
	/*
	 * override method from superclass
	 * @see com.legacytojava.message.vo.BaseVo#isEditable()
	 */
	public boolean isEditable() {
		editable = !("S".equalsIgnoreCase(getStatusId()));
		return editable;
	}
	
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String address) {
		this.streetAddress = address;
	}
	public String getStreetAddress2() {
		return streetAddress2;
	}
	public void setStreetAddress2(String address2) {
		this.streetAddress2 = address2;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public String getProfession() {
		return profession;
	}
	public void setProfession(String profession) {
		this.profession = profession;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String city) {
		this.cityName = city;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public String getDayPhone() {
		return dayPhone;
	}
	public void setDayPhone(String dayPhone) {
		this.dayPhone = dayPhone;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getEveningPhone() {
		return eveningPhone;
	}
	public void setEveningPhone(String eveningPhone) {
		this.eveningPhone = eveningPhone;
	}
	public String getMobileCarrier() {
		return mobileCarrier;
	}
	public void setMobileCarrier(String mobileCarrier) {
		this.mobileCarrier = mobileCarrier;
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
	public String getMemoText() {
		return memoText;
	}
	public void setMemoText(String memo) {
		this.memoText = memo;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public String getMsgDetail() {
		return msgDetail;
	}
	public void setMsgDetail(String msgDetail) {
		this.msgDetail = msgDetail;
	}
	public String getMsgFooter() {
		return msgFooter;
	}
	public void setMsgFooter(String msgFooter) {
		this.msgFooter = msgFooter;
	}
	public String getMsgHeader() {
		return msgHeader;
	}
	public void setMsgHeader(String msgHeader) {
		this.msgHeader = msgHeader;
	}
	public String getMsgOptional() {
		return msgOptional;
	}
	public void setMsgOptional(String msgOptional) {
		this.msgOptional = msgOptional;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getSecurityAnswer() {
		return securityAnswer;
	}
	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}
	public String getSecurityQuestion() {
		return securityQuestion;
	}
	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}
	public String getSsnNumber() {
		return ssnNumber;
	}
	public void setSsnNumber(String ssn) {
		this.ssnNumber = ssn;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public String getTaxId() {
		return taxId;
	}
	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}
	public String getTimeZoneCode() {
		return timeZoneCode;
	}
	public void setTimeZoneCode(String timeZoneCode) {
		this.timeZoneCode = timeZoneCode;
	}
	public String getZipCode4() {
		return zipCode4;
	}
	public void setZipCode4(String zip4) {
		this.zipCode4 = zip4;
	}
	public String getZipCode5() {
		return zipCode5;
	}
	public void setZipCode5(String zip5) {
		this.zipCode5 = zip5;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	public long getEmailAddrId() {
		return emailAddrId;
	}
	public void setEmailAddrId(long emailAddrId) {
		this.emailAddrId = emailAddrId;
	}
	public String getPrevEmailAddr() {
		return prevEmailAddr;
	}
	public void setPrevEmailAddr(String prevEmailAddr) {
		this.prevEmailAddr = prevEmailAddr;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public Timestamp getPasswordChangeTime() {
		return passwordChangeTime;
	}
	public void setPasswordChangeTime(Timestamp passwordChangeTime) {
		this.passwordChangeTime = passwordChangeTime;
	}
	public String getOrigCustId() {
		return origCustId;
	}
	public void setOrigCustId(String origCustId) {
		this.origCustId = origCustId;
	}
}