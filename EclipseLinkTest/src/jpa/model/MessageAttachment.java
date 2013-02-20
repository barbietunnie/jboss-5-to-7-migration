package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="message_attachment", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "attachmentDepth", "attachmentSequence"}))
public class MessageAttachment extends BaseModel implements Serializable
{
	private static final long serialVersionUID = -2228140043531630257L;

	@Embedded
	private MessageAttachmentPK messageAttachmentPK;

	@Column(length=100, nullable=true)
	private String attachmentName = null;
	@Column(length=100, nullable=true)
	private String attachmentType = null;
	@Column(length=100, nullable=true)
	private String attachmentDisp = null;
	@Column(length=32700, nullable=true)
	private byte[] attachmentValue = null;

	public MessageAttachment() {}

	public MessageAttachmentPK getMessageAttachmentPK() {
		return messageAttachmentPK;
	}

	public void setMessageAttachmentPK(MessageAttachmentPK messageAttachmentPK) {
		this.messageAttachmentPK = messageAttachmentPK;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	public String getAttachmentDisp() {
		return attachmentDisp;
	}

	public void setAttachmentDisp(String attachmentDisp) {
		this.attachmentDisp = attachmentDisp;
	}

	public byte[] getAttachmentValue() {
		return attachmentValue;
	}

	public void setAttachmentValue(byte[] attachmentValue) {
		this.attachmentValue = attachmentValue;
	}
}
