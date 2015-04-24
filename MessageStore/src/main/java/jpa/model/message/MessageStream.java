package jpa.model.message;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

import org.eclipse.persistence.annotations.Index;

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

	@OneToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false,
			table="message_stream", foreignKey=@ForeignKey(name="FK_message_stream_MessageInboxRowId"))
	private MessageInbox messageInbox;

	@Index
	@Column(name="FromAddrRowId", nullable=true)
	private int fromAddrRowId;

	@Column(name="ToAddrRowId", nullable=true)
	private int toAddrRowId;

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

	public int getFromAddrRowId() {
		return fromAddrRowId;
	}

	public void setFromAddrRowId(int fromAddrRowId) {
		this.fromAddrRowId = fromAddrRowId;
	}

	public int getToAddrRowId() {
		return toAddrRowId;
	}

	public void setToAddrRowId(int toAddrRowId) {
		this.toAddrRowId = toAddrRowId;
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
