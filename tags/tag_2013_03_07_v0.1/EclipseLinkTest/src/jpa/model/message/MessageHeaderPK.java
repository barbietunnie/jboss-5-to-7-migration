package jpa.model.message;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class MessageHeaderPK implements Serializable {
	private static final long serialVersionUID = -8974001276891830442L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@Column(name="HeaderSequence", nullable=false)
	private int headerSequence = -1;

	public MessageHeaderPK() {}
	
	public MessageHeaderPK(MessageInbox messageInbox, int headerSequence) {
		this.messageInbox = messageInbox;
		this.headerSequence = headerSequence;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getHeaderSequence() {
		return headerSequence;
	}

	public void setHeaderSequence(int headerSequence) {
		this.headerSequence = headerSequence;
	}

}