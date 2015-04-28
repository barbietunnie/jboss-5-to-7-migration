package jpa.model.message;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class MessageDeliveryStatusPK implements Serializable {
	private static final long serialVersionUID = 8350118998915460679L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	@XmlTransient
	private MessageInbox messageInbox;

	@Column(name="FinalRcptAddrRowId", nullable=false)
	private int finalRcptAddrRowId;

	public MessageDeliveryStatusPK() {}
	
	public MessageDeliveryStatusPK(MessageInbox messageInbox, int finalRcptAddrRowId) {
		this.messageInbox = messageInbox;
		this.finalRcptAddrRowId = finalRcptAddrRowId;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getFinalRcptAddrRowId() {
		return finalRcptAddrRowId;
	}

	public void setFinalRcptAddrRowId(int finalRcptAddrRowId) {
		this.finalRcptAddrRowId = finalRcptAddrRowId;
	}

}