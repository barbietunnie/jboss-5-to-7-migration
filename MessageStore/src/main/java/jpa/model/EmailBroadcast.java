package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="email_broadcast")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="EmailBroadcastEntiry",
		entities={
		 @EntityResult(entityClass=EmailBroadcast.class),
	  	}),
	})
public class EmailBroadcast extends BaseModel implements Serializable {
	private static final long serialVersionUID = 8041670636070073207L;

	@Transient
	public static final String MAPPING_EMAIL_BROADCAST_ENTITY = "EmailBroadcastEntiry";

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="EmailAddressRowId",insertable=true,referencedColumnName="Row_Id",nullable=false,
			foreignKey=@ForeignKey(name="FK_email_broadcast_EmailAddressRowId"))
	private EmailAddress emailAddress;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="BroadcastDataRowId",insertable=true,referencedColumnName="Row_Id",nullable=false,
			foreignKey=@ForeignKey(name="FK_email_broadcast_BroadcastDataRowId"))
	private BroadcastData broadcastData;

	@Column(nullable=false, columnDefinition="int")
	private int sentCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int openCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int clickCount = 0;
	@Column(nullable=true)
	private Timestamp lastOpenTime = null;
	@Column(nullable=true)
	private Timestamp lastClickTime = null;

	public EmailBroadcast() {}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	public BroadcastData getBroadcastData() {
		return broadcastData;
	}

	public void setBroadcastData(BroadcastData broadcastData) {
		this.broadcastData = broadcastData;
	}

	public int getSentCount() {
		return sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public Timestamp getLastOpenTime() {
		return lastOpenTime;
	}

	public void setLastOpenTime(Timestamp lastOpenTime) {
		this.lastOpenTime = lastOpenTime;
	}

	public Timestamp getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
	}

}
