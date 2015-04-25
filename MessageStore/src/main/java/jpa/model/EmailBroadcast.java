package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.Lob;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.MailingListDeliveryType;

@Entity
@Table(name="email_broadcast")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="EmailBroadcastEntiry",
		entities={
		 @EntityResult(entityClass=EmailBroadcast.class),
	  	}),
	})
public class EmailBroadcast extends BaseModel implements Serializable {
	private static final long serialVersionUID = 8041670636070073207L;

	@Transient
	public static final String MAPPING_EMAIL_BROADCAST_ENTITY = "EmailBroadcastEntiry";

	@Column(name="MailingListRowId", nullable=false)
	private int mailingListRowId;

	@Column(name="EmailTemplateRowId", nullable=true)
	private Integer emailTemplateRowId;
	
	@Column(nullable=true, length=255)
	private String msgSubject = null;
	@Lob
	@Column(nullable=true,length=65530)
	private String msgBody = null;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer renderId = null; // TODO
	@Column(length=20, nullable=false)
	private String deliveryType = MailingListDeliveryType.ALL_ON_LIST.getValue();
	@Column(nullable=false, columnDefinition="int")
	private int sentCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int openCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int clickCount = 0;
	@Column(nullable=true)
	private Timestamp lastOpenTime = null;
	@Column(nullable=true)
	private Timestamp lastClickTime = null;
	@Column(nullable=true)
	private Timestamp startTime = null;
	@Column(nullable=true)
	private Timestamp endTime = null;
	@Column(nullable=false, columnDefinition="int")
	private int unsubscribeCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int complaintCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int referralCount = 0;

	public EmailBroadcast() {}

	public int getMailingListRowId() {
		return mailingListRowId;
	}

	public void setMailingListRowId(int mailingListRowId) {
		this.mailingListRowId = mailingListRowId;
	}

	public Integer getEmailTemplateRowId() {
		return emailTemplateRowId;
	}

	public void setEmailTemplateRowId(Integer emailTemplateRowId) {
		this.emailTemplateRowId = emailTemplateRowId;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public Integer getRenderId() {
		return renderId;
	}

	public void setRenderId(Integer renderId) {
		this.renderId = renderId;
	}

	public String getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	public int getSentCount() {
		return sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public Timestamp getLastOpenTime() {
		return lastOpenTime;
	}

	public void setLastOpenTime(Timestamp lastOpenTime) {
		this.lastOpenTime = lastOpenTime;
	}

	public Timestamp getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public int getUnsubscribeCount() {
		return unsubscribeCount;
	}

	public void setUnsubscribeCount(int unsubscribeCount) {
		this.unsubscribeCount = unsubscribeCount;
	}

	public int getComplaintCount() {
		return complaintCount;
	}

	public void setComplaintCount(int complaintCount) {
		this.complaintCount = complaintCount;
	}

	public int getReferralCount() {
		return referralCount;
	}

	public void setReferralCount(int referralCount) {
		this.referralCount = referralCount;
	}

}