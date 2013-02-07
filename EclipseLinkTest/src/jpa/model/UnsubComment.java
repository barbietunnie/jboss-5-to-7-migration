package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="unsub_comment")
public class UnsubComment extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 6944693180570837420L;

	@ManyToOne(targetEntity=EmailAddr.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="EmailAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private EmailAddr emailAddr;

	@ManyToOne(targetEntity=MailingList.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="MailingListRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private MailingList mailingList;

	@Column(nullable=false, length=2046)
	private String comments = "";

	public UnsubComment() {
		// must have a no-argument constructor
	}

	public EmailAddr getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(EmailAddr emailAddr) {
		this.emailAddr = emailAddr;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
