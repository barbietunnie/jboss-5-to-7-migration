package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=MailingList.class)
	@JoinColumn(name="MailingListRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private MailingList mailingList;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=EmailAddr.class)
	@JoinColumn(name="EmailAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private EmailAddr emailAddress;

	@Column(length=1000, nullable=false)
	private String comments = "";

	public MessageUnsubComment() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public EmailAddr getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddr emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
}
