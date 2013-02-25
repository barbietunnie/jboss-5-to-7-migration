package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="message_unsub_comment", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId"}))
public class MessageUnsubComment extends BaseModel implements Serializable
{
	private static final long serialVersionUID = -8816644697506979193L;

	@OneToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@Column(name="MailingListRowId", nullable=true)
	private int mailingListRowId;

	@Column(name="EmailAddrRowId", nullable=false)
	private int emailAddrRowId;

	@Column(length=1000, nullable=false)
	private String comments = "";

	public MessageUnsubComment() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getMailingListRowId() {
		return mailingListRowId;
	}

	public void setMailingListRowId(int mailingListRowId) {
		this.mailingListRowId = mailingListRowId;
	}

	public int getEmailAddrRowId() {
		return emailAddrRowId;
	}

	public void setEmailAddrRowId(int emailAddrRowId) {
		this.emailAddrRowId = emailAddrRowId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
}
