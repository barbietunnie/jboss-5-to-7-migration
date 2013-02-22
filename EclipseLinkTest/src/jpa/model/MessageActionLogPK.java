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

	@Column(name="LeadMsgId", nullable=false)
	private int leadMsgId = -1;

	public MessageActionLogPK() {}
	
	public MessageActionLogPK(MessageInbox messageInbox, int logSequence) {
		this.messageInbox = messageInbox;
		this.leadMsgId = logSequence;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getLeadMsgId() {
		return leadMsgId;
	}

	public void setLeadMsgId(int leadMsgId) {
		this.leadMsgId = leadMsgId;
	}

}