package jpa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.constant.CodeType;

@Entity
@Table(name="message_source")
public class MessageSource extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 1038996333144767265L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=EmailAddr.class)
	@JoinColumn(name="FromAddressRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private EmailAddr fromAddress;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="ReplyToAddressRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr replyToAddress;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=TemplateData.class)
	@JoinColumn(name="TemplateDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private TemplateData templateData;

	@JoinTable(name="source_to_variable",
			joinColumns = {@JoinColumn(name="MessageSoureRowId",referencedColumnName="Row_Id", columnDefinition="int")},
			inverseJoinColumns = {@JoinColumn(name = "TemplateVariableRowId", referencedColumnName = "Row_Id", columnDefinition="int")},
			uniqueConstraints={@UniqueConstraint(columnNames={"MessageSoureRowId", "TemplateVariableRowId"})}
			)
	@ManyToMany(fetch=FetchType.LAZY,cascade=CascadeType.ALL)
	private List<TemplateVariable> templateVariableList;
	
	@Column(nullable=false, length=26, unique=true)
	private String msgSourceId = "";
	@Column(nullable=true, length=100)
	private String description = null;
	
	@Column(nullable=false, length=1, columnDefinition="boolean")
	private boolean isExcludingIdToken = false;
	// Y - No email id will be embedded into message
	@Column(nullable=false, length=1, columnDefinition="char")
	private String carrierCode = jpa.constant.CarrierCode.SMTPMAIL.getValue();
	// Internet, WebMail, Internal Routing, ...
	@Column(nullable=false, length=1, columnDefinition="char")
	private String allowOverride = CodeType.YES_CODE.getValue();
	// allow override templates, addresses to be supplied at runtime
	@Column(nullable=false, length=1, columnDefinition="boolean")
	private boolean isSaveMsgStream = true;
	// Y - save rendered SMTP message stream to MSGOBSTREAM
	@Column(nullable=false, length=1, columnDefinition="boolean")
	private boolean isArchiveMsg = false;
	// Y - archive the rendered messages
	@Column(nullable=true)
	private Integer purgeAfter = null; // in month

	public MessageSource() {}

	public EmailAddr getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(EmailAddr fromAddress) {
		this.fromAddress = fromAddress;
	}

	public EmailAddr getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(EmailAddr replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	public TemplateData getTemplateData() {
		return templateData;
	}

	public void setTemplateData(TemplateData templateData) {
		this.templateData = templateData;
	}

	public List<TemplateVariable> getTemplateVariableList() {
		if (templateVariableList==null) {
			templateVariableList = new ArrayList<TemplateVariable>();
		}
		return templateVariableList;
	}

	public void setTemplateVariableList(List<TemplateVariable> templateVariableList) {
		this.templateVariableList = templateVariableList;
	}

	public String getMsgSourceId() {
		return msgSourceId;
	}

	public void setMsgSourceId(String msgSourceId) {
		this.msgSourceId = msgSourceId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isExcludingIdToken() {
		return isExcludingIdToken;
	}

	public void setExcludingIdToken(boolean isExcludingIdToken) {
		this.isExcludingIdToken = isExcludingIdToken;
	}

	public String getCarrierCode() {
		return carrierCode;
	}

	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}

	public String getAllowOverride() {
		return allowOverride;
	}

	public void setAllowOverride(String allowOverride) {
		this.allowOverride = allowOverride;
	}

	public boolean isSaveMsgStream() {
		return isSaveMsgStream;
	}

	public void setSaveMsgStream(boolean isSaveMsgStream) {
		this.isSaveMsgStream = isSaveMsgStream;
	}

	public boolean isArchiveMsg() {
		return isArchiveMsg;
	}

	public void setArchiveMsg(boolean isArchiveMsg) {
		this.isArchiveMsg = isArchiveMsg;
	}

	public Integer getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}

}
