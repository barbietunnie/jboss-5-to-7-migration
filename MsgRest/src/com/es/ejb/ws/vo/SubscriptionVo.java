package com.es.ejb.ws.vo;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.msgui.vo.TimestampAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SubscriptionVo")
public class SubscriptionVo extends BaseWsVo {
	private static final long serialVersionUID = 5112977722659777018L;
	
	@XmlElement(required=true)
	private String listId;
	private String description;

	@XmlElement(required=true)
	private boolean isSubscribed = true;

	@XmlElement(required=true)
	private String address;
	private Boolean isOptIn = null;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp CreateTime;

	public SubscriptionVo() {
		// must have a no-argument constructor
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Boolean getIsOptIn() {
		return isOptIn;
	}

	public void setIsOptIn(Boolean isOptIn) {
		this.isOptIn = isOptIn;
	}

	public Timestamp getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}

}
