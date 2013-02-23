package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="message_stream", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId"}))
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MessageStreamNative",
		entities={
		 @EntityResult(entityClass=MessageStream.class),
	  	}
	  	),
	})
public class MessageStream extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 6941824800672413874L;
	@Transient
	public static final String MAPPING_MESSAGE_STREAM = "MessageStreamNative";

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="FromAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr fromAddress;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="TpAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr toAddress;

	@Column(length=255, nullable=true)
	private String msgSubject = null;
	@Lob
	@Column(length=65530, nullable=true)
	private byte[] msgStream = null;
	
	public MessageStream() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public EmailAddr getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(EmailAddr fromAddress) {
		this.fromAddress = fromAddress;
	}

	public EmailAddr getToAddress() {
		return toAddress;
	}

	public void setToAddress(EmailAddr toAddress) {
		this.toAddress = toAddress;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public byte[] getMsgStream() {
		return msgStream;
	}

	public void setMsgStream(byte[] msgStream) {
		this.msgStream = msgStream;
	}

}
