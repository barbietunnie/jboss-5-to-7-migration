package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.MailingListDeliveryOption;
import jpa.constant.MailingListType;

@Entity
@Table(name="email_template")
public class EmailTemplate extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -4595181759983336810L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ClientDataRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private ClientData clientData;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="MailingListRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private MailingList mailingList;
	
	@Column(nullable=false, length=26, unique=true)
	private String templateId = "";
	@Column(nullable=true, length=255)
	private String subject = null;
	@Lob
	@Column(nullable=true, length=65534)
	private String bodyText = null;
	@Column(nullable=false, length=1, columnDefinition="boolean not null")
	private boolean isHtml = true;
	@Column(nullable=false, length=12)
	private String listType = MailingListType.TRADITIONAL.getValue();
	@Column(nullable=false, length=10)
	private String deliveryOption = MailingListDeliveryOption.ALL_ON_LIST.getValue();
	@Column(nullable=true, length=100)
	private String selectCriteria = null;
	@Column(nullable=true, length=1, columnDefinition="Boolean")
	private Boolean isEmbedEmailId = null; // use system default
	@Column(nullable=false, length=1, columnDefinition="boolean not null")
	private boolean isBuiltin = false;
	@Lob
	@Column(nullable=true, columnDefinition="BLOB")
	private SchedulesBlob schedulesBlob = null;
	
	@Transient
	private String origTemplateId = null;

	public EmailTemplate() {
		// must have a no-argument constructor
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public String getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public String getSelectCriteria() {
		return selectCriteria;
	}

	public void setSelectCriteria(String selectCriteria) {
		this.selectCriteria = selectCriteria;
	}

	public Boolean getIsEmbedEmailId() {
		return isEmbedEmailId;
	}

	public void setIsEmbedEmailId(Boolean isEmbedEmailId) {
		this.isEmbedEmailId = isEmbedEmailId;
	}

	public boolean isBuiltin() {
		return isBuiltin;
	}

	public void setBuiltin(boolean isBuiltin) {
		this.isBuiltin = isBuiltin;
	}

	public SchedulesBlob getSchedulesBlob() {
		return schedulesBlob;
	}

	public void setSchedulesBlob(SchedulesBlob schedulesBlob) {
		this.schedulesBlob = schedulesBlob;
	}

	public String getOrigTemplateId() {
		return origTemplateId;
	}

	public void setOrigTemplateId(String origTemplateId) {
		this.origTemplateId = origTemplateId;
	}

}
