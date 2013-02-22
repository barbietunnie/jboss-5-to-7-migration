package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.constant.EmailAddrType;

@Entity
@Table(name="message_address", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "addressType", "EmailAddrRowId"}))
public class MessageAddress extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 4120242394404262528L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=EmailAddr.class)
	@JoinColumn(name="EmailAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private EmailAddr addressValue;

	@Column(length=12, nullable=false)
	private String addressType = EmailAddrType.FROM_ADDR.getValue();

	public MessageAddress() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public EmailAddr getAddressValue() {
		return addressValue;
	}

	public void setAddressValue(EmailAddr addressValue) {
		this.addressValue = addressValue;
	}
}
