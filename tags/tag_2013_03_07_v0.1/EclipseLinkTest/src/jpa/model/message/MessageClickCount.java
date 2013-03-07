package jpa.model.message;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.constant.MailingListDeliveryType;
import jpa.model.BaseModel;

@Entity
@Table(name="message_click_count", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId"}))
public class MessageClickCount extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 6478186312095503162L;

	@OneToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@Column(name="MailingListRowId", nullable=false)
	private int mailingListRowId;

	@Column(length=10, nullable=false)
	private String deliveryType = MailingListDeliveryType.ALL_ON_LIST.getValue();
	@Column(nullable=false, columnDefinition="smallint")
	private int sentCount = 0;
	@Column(nullable=false, columnDefinition="smallint")
	private int openCount = 0;
	@Column(nullable=false, columnDefinition="smallint")
	private int clickCount = 0;
	@Column(nullable=true)
	private Timestamp lastOpenTime = null;
	@Column(nullable=true)
	private Timestamp lastClickTime = null;
	@Column(nullable=true)
	private Timestamp startTime = null;
	@Column(nullable=true)
	private Timestamp endTime = null;
	@Column(nullable=false, columnDefinition="smallint")
	private int unsubscribeCount = 0;
	@Column(nullable=false, columnDefinition="smallint")
	private int complaintCount = 0;
	@Column(nullable=false, columnDefinition="smallint")
	private int referralCount = 0;

	public MessageClickCount() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getMailingListRowId() {
		return mailingListRowId;
	}

	public void setMailingListRowId(int mailingListRowId) {
		this.mailingListRowId = mailingListRowId;
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
