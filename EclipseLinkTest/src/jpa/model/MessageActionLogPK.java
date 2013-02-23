package jpa.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class MessageActionLogPK implements Serializable {
	private static final long serialVersionUID = 2827238892173739680L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="LeadMessageRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox leadMessage;

	public MessageActionLogPK() {}
	
	public MessageActionLogPK(MessageInbox messageInbox, MessageInbox leadMessage) {
		this.messageInbox = messageInbox;
		this.leadMessage = leadMessage;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public MessageInbox getLeadMessage() {
		return leadMessage;
	}

	public void setLeadMessage(MessageInbox leadMessage) {
		this.leadMessage = leadMessage;
	}

}