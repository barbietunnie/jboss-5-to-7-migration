package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="message_rfcfield", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "rfcType"}))
public class MessageRfcField extends BaseModel implements Serializable
{
	private static final long serialVersionUID = -4861561853095214088L;

	@Embedded
	private MessageRfcFieldPK messageRfcFieldPK;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="FinalRcptAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr finalRecipient;

	@Column(length=30, nullable=true)
	private String rfcStatus = null;
	@Column(length=30, nullable=true)
	private String rfcAction = null;
	@Column(length=255, nullable=true)
	private String originalRecipient = null;
	@Column(length=255, nullable=true)
	private String originalMsgSubject = null;
	@Column(length=255, nullable=true)
	private String messageId = null;
	@Lob
	@Column(length=32700, nullable=true)
	private String dsnText = null;
	@Lob
	@Column(length=32700, nullable=true)
	private String dsnRfc822 = null;
	@Lob
	@Column(length=32700, nullable=true)
	private String deliveryStatus = null;

	public MessageRfcField() {}

	public MessageRfcFieldPK getMessageRfcFieldPK() {
		return messageRfcFieldPK;
	}

	public void setMessageRfcFieldPK(MessageRfcFieldPK messageRfcFieldPK) {
		this.messageRfcFieldPK = messageRfcFieldPK;
	}

	public EmailAddr getFinalRecipient() {
		return finalRecipient;
	}

	public void setFinalRecipient(EmailAddr finalRecipient) {
		this.finalRecipient = finalRecipient;
	}

	public String getRfcStatus() {
		return rfcStatus;
	}

	public void setRfcStatus(String rfcStatus) {
		this.rfcStatus = rfcStatus;
	}

	public String getRfcAction() {
		return rfcAction;
	}

	public void setRfcAction(String rfcAction) {
		this.rfcAction = rfcAction;
	}

	public String getOriginalRecipient() {
		return originalRecipient;
	}

	public void setOriginalRecipient(String originalRecipient) {
		this.originalRecipient = originalRecipient;
	}

	public String getOriginalMsgSubject() {
		return originalMsgSubject;
	}

	public void setOriginalMsgSubject(String originalMsgSubject) {
		this.originalMsgSubject = originalMsgSubject;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getDsnText() {
		return dsnText;
	}

	public void setDsnText(String dsnText) {
		this.dsnText = dsnText;
	}

	public String getDsnRfc822() {
		return dsnRfc822;
	}

	public void setDsnRfc822(String dsnRfc822) {
		this.dsnRfc822 = dsnRfc822;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
}
