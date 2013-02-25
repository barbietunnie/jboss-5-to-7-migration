package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
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

	@Column(name="LeadMessageRowId", nullable=false)
	private int leadMessageRowId;

	public MessageActionLogPK() {}
	
	public MessageActionLogPK(MessageInbox messageInbox, int leadMessageId) {
		this.messageInbox = messageInbox;
		this.leadMessageRowId = leadMessageId;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getLeadMessageRowId() {
		return leadMessageRowId;
	}

	public void setLeadMessageRowId(int leadMessageRowId) {
		this.leadMessageRowId = leadMessageRowId;
	}

}