package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="message_header", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "headerSequence"}))
public class MessageHeader extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 3741976547999638916L;

	@Embedded
	private MessageHeaderPK messageHeaderPK;

	@Column(length=100, nullable=true)
	private String headerName = null;
	@Column(length=32700, nullable=true)
	private String headerValue = null;

	public MessageHeader() {}
}
