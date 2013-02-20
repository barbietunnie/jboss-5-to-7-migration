package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class MessageAddressPK implements Serializable {
	private static final long serialVersionUID = -3160308177705771531L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@Column(name="AddressSequence", nullable=false)
	private int addressSequence = -1;

	public MessageAddressPK() {}
	
	public MessageAddressPK(MessageInbox messageInbox, int addressSequence) {
		this.messageInbox = messageInbox;
		this.addressSequence = addressSequence;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getAddressSequence() {
		return addressSequence;
	}

	public void setAddressSequence(int addressSequence) {
		this.addressSequence = addressSequence;
	}

}