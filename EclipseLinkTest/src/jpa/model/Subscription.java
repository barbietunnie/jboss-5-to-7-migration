package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.constant.CodeType;
import jpa.util.StringUtil;

@Entity
@Table(name="subscription", uniqueConstraints=@UniqueConstraint(columnNames = {"EmailAddrRowId", "MailingListRowId"}))
public class Subscription extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 5306761711116978942L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="EmailAddrRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private EmailAddress emailAddr; // subscriber email address
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="MailingListRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MailingList mailingList; // mailing list isSubscribed to
	
	@Column(length=1,nullable=false,columnDefinition="boolean not null")
	private boolean isSubscribed = true;
	@Column(nullable=true,columnDefinition="Boolean")
	private Boolean isOptIn = null;
	@Column(nullable=false)
	private Timestamp CreateTime;
	@Column(nullable=false)
	private int sentCount = 0;
	@Column(nullable=true)
	private Timestamp lastSentTime = null;
	@Column(nullable=false)
	private int openCount = 0;
	@Column(nullable=true)
	private Timestamp lastOpenTime = null;
	@Column(nullable=false)
	private int clickCount = 0;
	@Column(nullable=true)
	private Timestamp lastClickTime = null;

	public Subscription() {
		// must have a no-argument constructor
	}

	/** define components for UI */
	public String getEmailAddrShort() {
		if (getEmailAddr()!=null) {
			return StringUtil.cutWithDots(getEmailAddr().getAddress(), 100);
		}
		else {
			throw new IllegalStateException("Subscription instance must be loaded with data!");
		}
	}
	
	public String getSubscribedDesc() {
		return CodeType.NO_CODE.getValue().equals(isSubscribed) ? CodeType.NO.getValue() : CodeType.YES.getValue();
	}
	
	public String getAcceptHtmlDesc() {
		boolean acceptHtml = getEmailAddr()==null?true:getEmailAddr().isAcceptHtml();
		return (acceptHtml==false ? CodeType.NO.getValue() : CodeType.YES.getValue());
	}
	
//	public String getSubscriberName() {
//		if (getEmailAddr()!=null && getEmailAddr().getSubscriberData()!=null) {
//			String firstName = getEmailAddr().getSubscriberData().getFirstName();
//			String lastName = getEmailAddr().getSubscriberData().getLastName();
//			return (firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName);
//		}
//		else {
//			throw new IllegalStateException("Subscription instance must be loaded with data!");
//		}
//	}
	/** end of UI */

	public EmailAddress getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(EmailAddress emailAddr) {
		this.emailAddr = emailAddr;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public Boolean getIsOptIn() {
		return isOptIn;
	}

	public void setIsOptIn(Boolean isOptIn) {
		this.isOptIn = isOptIn;
	}

	public Timestamp getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}

	public int getSentCount() {
		return sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}

	public Timestamp getLastSentTime() {
		return lastSentTime;
	}

	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public Timestamp getLastOpenTime() {
		return lastOpenTime;
	}

	public void setLastOpenTime(Timestamp lastOpenTime) {
		this.lastOpenTime = lastOpenTime;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public Timestamp getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
	}
}
