package jpa.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.CascadeOnDelete;

import jpa.constant.StatusId;

@Entity
@Table(name="mailing_list")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MailingListWithCounts",
		entities={
		 @EntityResult(entityClass=MailingList.class),
	  	},
	  	columns={
		 @ColumnResult(name="sentCount"),
		 @ColumnResult(name="openCount"),
		 @ColumnResult(name="clickCount"),
	  	}),
	})
public class MailingList extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -1314842144892847007L;

	@Transient
	public static final String MAPPING_MAILING_LIST_WITH_COUNTS = "MailingListWithCounts";

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true, mappedBy="mailingList")
	@CascadeOnDelete
	private List<Subscription> subscriptions; // subscribers of this list
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false, targetEntity=SenderData.class)
	@JoinColumn(name="SenderDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private SenderData senderData; // sender the list associated to
	
	@Column(nullable=false, length=20, unique=true)
	private String listId = "";
	@Column(nullable=true, length=50)
	private String displayName = null;
	@Column(nullable=false, length=100)
	private String acctUserName = ""; // list email address left part
	@Column(nullable=true, length=500)
	private String description = null;
	@Column(nullable=false, length=1, columnDefinition="boolean not null")
	private boolean isBuiltin = false;
	@Column(nullable=false)
	private Timestamp createTime;
	@Column(nullable=false, length=255)
	private String listMasterEmailAddr = "";

	@Transient
	private String origListId = null;

	public MailingList() {
		// must have a no-argument constructor
	}

	/** define components for UI */
	public boolean isActive() {
		return StatusId.ACTIVE.getValue().equalsIgnoreCase(getStatusId());
	}
	
	public String getListEmailAddr() {
		if (getSenderData()!=null) {
			return acctUserName + "@" + getSenderData().getDomainName();
		}
		else {
			return acctUserName + "@" + "localhost";
		}
	}
	/** end of UI */

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public String getListMasterEmailAddr() {
		return listMasterEmailAddr;
	}

	public void setListMasterEmailAddr(String listMasterEmailAddr) {
		this.listMasterEmailAddr = listMasterEmailAddr;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAcctUserName() {
		return acctUserName;
	}

	public void setAcctUserName(String acctUserName) {
		this.acctUserName = acctUserName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isBuiltin() {
		return isBuiltin;
	}

	public void setBuiltin(boolean isBuiltin) {
		this.isBuiltin = isBuiltin;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getOrigListId() {
		return origListId;
	}

	public void setOrigListId(String origListId) {
		this.origListId = origListId;
	}
}
