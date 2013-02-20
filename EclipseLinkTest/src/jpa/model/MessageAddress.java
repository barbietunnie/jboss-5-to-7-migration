package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.constant.EmailAddrType;

@Entity
@Table(name="message_address", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "addressSequence"}))
public class MessageAddress extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 4120242394404262528L;

	@Embedded
	private MessageAddressPK messageAddressPK;

	@Column(length=12, nullable=false)
	private String addressType = EmailAddrType.FROM_ADDR.getValue();
	@Column(length=255, nullable=true)
	private String addressValue = null;

	public MessageAddress() {}

	public MessageAddressPK getMessageAddressPK() {
		return messageAddressPK;
	}

	public void setMessageAddressPK(MessageAddressPK messageAddressPK) {
		this.messageAddressPK = messageAddressPK;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public String getAddressValue() {
		return addressValue;
	}

	public void setAddressValue(String addressValue) {
		this.addressValue = addressValue;
	}
}
