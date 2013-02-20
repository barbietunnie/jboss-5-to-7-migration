package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class MessageAttachmentPK implements Serializable {
	private static final long serialVersionUID = 1355582145272160790L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@Column(nullable=false, columnDefinition="decimal(2,0)")
	private int attachmentDepth = -1;
	@Column(nullable=false, columnDefinition="decimal(3,0)")
	private int attachmentSequence = -1;

	public MessageAttachmentPK() {}
	
	public MessageAttachmentPK(MessageInbox messageInbox, int attachmentDepth, int attachmentSequence) {
		this.messageInbox = messageInbox;
		this.attachmentDepth = attachmentDepth;
		this.attachmentSequence = attachmentSequence;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getAttachmentDepth() {
		return attachmentDepth;
	}

	public void setAttachmentDepth(int attachmentDepth) {
		this.attachmentDepth = attachmentDepth;
	}

	public int getAttachmentSequence() {
		return attachmentSequence;
	}

	public void setAttachmentSequence(int attachmentSequence) {
		this.attachmentSequence = attachmentSequence;
	}

}