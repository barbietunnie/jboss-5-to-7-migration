package com.es.data.preload;

import com.es.data.constant.EmailVariableType;

/*
 * define sample email variables
 */
public enum EmailVariableEnum implements EnumInterface {

	SubscriberName(EmailVariableType.Custom,"Subscriber","FirstName,LastName",false,"Valued Subscriber",
			"SELECT CONCAT(c.FirstName, ' ', c.LastName) as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberFirstName(EmailVariableType.Custom,"Subscriber","FirstName",false,"Valued Subscriber",
			"SELECT c.FirstName as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberLastName(EmailVariableType.Custom,"Subscriber","LastName",false,"Valued Subscriber",
			"SELECT c.LastName as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberAddress(EmailVariableType.Custom,"Subscriber","StreetAddress",false,"",
			"SELECT CONCAT_WS(',',c.StreetAddress2,c.StreetAddress) as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberCityName(EmailVariableType.Custom,"Subscriber","CityName",false,"",
			"SELECT c.CityName as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberStateCode(EmailVariableType.Custom,"Subscriber","StateCode",false,"",
			"SELECT CONCAT_WS(',',c.StateCode,c.ProvinceName) as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberZipCode(EmailVariableType.Custom,"Subscriber","ZipCode",false,"",
			"SELECT CONCAT_WS('-',c.ZipCode5,ZipCode4) as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),
	SubscriberCountry(EmailVariableType.Custom,"Subscriber","Country",false,"",
			"SELECT c.Country as ResultStr FROM subscriber c, email_address e where e.EmailAddrId=c.EmailAddrId and e.EmailAddrId=?1","com.es.bo.external.SubscriberNameResolver"),

	EmailOpenCountImgTag(EmailVariableType.System,"","",true,
			"<img src='${WebSiteUrl}/msgopen.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>",null,null),
	EmailClickCountImgTag(EmailVariableType.System,"","",true,
			"<img src='${WebSiteUrl}/msgclick.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>",null,null),
	EmailUnsubscribeImgTag(EmailVariableType.System,"","",true,
			"<img src=='${WebSiteUrl}/msgunsub.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>",null,null),
	EmailTrackingTokens(EmailVariableType.System,"","",true,
			"msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}",null,null),
	FooterWithUnsubLink(EmailVariableType.System,"","",true,
			"<p>To unsubscribe from this mailing list, " + LF +
			"<a target='_blank' href='${WebSiteUrl}/MsgUnsubPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}'>click here</a>.</p>", null,null),
	FooterWithUnsubAddr(EmailVariableType.System,"","",true,
			"To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}" + LF +
			"with \"unsubscribe\" (no quotation marks) in the subject.",null,null),
	SubscribeURL(EmailVariableType.System,"","",true,
			"${WebSiteUrl}/subscribe.jsp?sbsrid=${SubscriberAddressId}",null,null),
	ConfirmationURL(EmailVariableType.System,"","",true,
			"${WebSiteUrl}/confirmsub.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}",null,null),
	UnsubscribeURL(EmailVariableType.System,"","",true,
			"${WebSiteUrl}/unsubscribe.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}",null,null),
	UserProfileURL(EmailVariableType.System,"","",true,
			"${WebSiteUrl}/userprofile.jsp?sbsrid=${SubscriberAddressId}",null,null),
	TellAFriendURL(EmailVariableType.System,"","",true,
			"${WebSiteUrl}/referral.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}",null,null),
	SiteLogoURL(EmailVariableType.System,"","",true,
			"${WebSiteUrl}/images/logo.gif",null,null);

	private EmailVariableType variableType;
	private String tableName;
	private String columnName;
	private boolean isBuiltin;
	private String defaultValue;
	private String variableQuery;
	private String variableProcName;

	private EmailVariableEnum(EmailVariableType variableType, String tableName,
			String columnName, boolean isBuiltin, String defaultValue,
			String variableQuery, String variableProcName) {
		this.variableType = variableType;
		this.tableName = tableName;
		this.columnName = columnName;
		this.isBuiltin = isBuiltin;
		this.defaultValue = defaultValue;
		this.variableQuery = variableQuery;
		this.variableProcName = variableProcName;
	}

	public EmailVariableType getVariableType() {
		return variableType;
	}

	public void setVariableType(EmailVariableType variableType) {
		this.variableType = variableType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isBuiltin() {
		return isBuiltin;
	}

	public void setBuiltin(boolean isBuiltin) {
		this.isBuiltin = isBuiltin;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getVariableQuery() {
		return variableQuery;
	}

	public void setVariableQuery(String variableQuery) {
		this.variableQuery = variableQuery;
	}

	public String getVariableProcName() {
		return variableProcName;
	}

	public void setVariableProcName(String variableProcName) {
		this.variableProcName = variableProcName;
	}
	
} 
