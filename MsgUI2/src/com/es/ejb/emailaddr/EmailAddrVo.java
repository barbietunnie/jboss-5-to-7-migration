package com.es.ejb.emailaddr;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.es.ejb.vo.BaseWsVo;
import com.es.tomee.util.TimestampAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "EmailAddrVo")
public class EmailAddrVo extends BaseWsVo {
	private static final long serialVersionUID = -918554579365101630L;

	@XmlElement(required=true)
	private String address;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp statusChangeTime;
 	private String statusChangeUserId;
	@XmlElement(required=true)
	private int bounceCount = 0;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastBounceTime;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastSentTime;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastRcptTime;
	@XmlElement(required=true)
	private boolean isAcceptHtml;
	private String origAddress;

	public EmailAddrVo() {
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

}
