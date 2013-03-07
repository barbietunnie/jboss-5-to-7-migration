package jpa.model.message;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.model.BaseModel;
import jpa.model.ClientData;
import jpa.model.CustomerData;

import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(name="message_rendered")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MessageRenderedNative",
		entities={
		 @EntityResult(entityClass=MessageRendered.class),
	  	}
	  	),
	})
public class MessageRendered extends BaseModel implements Serializable {
	private static final long serialVersionUID = -522522467087500772L;

	@Transient
	public static final String MAPPING_MESSAGE_RENDERED = "MessageRenderedNative";

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="renderVariablePK.messageRendered", orphanRemoval=true)
	@CascadeOnDelete
	private List<RenderVariable> renderVariableList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="renderAttachmentPK.messageRendered", orphanRemoval=true)
	@CascadeOnDelete
	private List<RenderAttachment> renderAttachmentList;

	@Column(name="MessageSourceRowId", nullable=false)
	private int messageSourceRowId;
	@Transient
	private MessageSource messageSource;

	@Column(name="MessageTemplateRowId", nullable=false)
	private int messageTemplateRowId;
	@Transient
	private TemplateData messageTemplate;

	@Column(name="ClientDataRowId", nullable=true, columnDefinition="Integer")
	private Integer clientDataRowId = null;
	@Transient
	private ClientData clientData;

	@Column(name="CustomerDataRowId", nullable=true, columnDefinition="Integer")
	private Integer customerDataRowId = null;
	@Transient
	private CustomerData customerData;

	@Column(nullable=false)
	private Timestamp startTime;

	@Column(nullable=true)
	private Integer purgeAfter = null;

	public MessageRendered() {
		// must have a no-argument constructor
		startTime = new Timestamp(System.currentTimeMillis());
	}

	public List<RenderVariable> getRenderVariableList() {
		if (renderVariableList == null) {
			renderVariableList = new ArrayList<RenderVariable>();
		}
		return renderVariableList;
	}

	public void setRenderVariableList(List<RenderVariable> renderVariableList) {
		this.renderVariableList = renderVariableList;
	}

	public List<RenderAttachment> getRenderAttachmentList() {
		if (renderAttachmentList==null) {
			renderAttachmentList = new ArrayList<RenderAttachment>();
		}
		return renderAttachmentList;
	}

	public void setRenderAttachmentList(List<RenderAttachment> renderAttachmentList) {
		this.renderAttachmentList = renderAttachmentList;
	}

	public int getMessageSourceRowId() {
		return messageSourceRowId;
	}

	public void setMessageSourceRowId(int messageSourceRowId) {
		this.messageSourceRowId = messageSourceRowId;
	}

	public int getMessageTemplateRowId() {
		return messageTemplateRowId;
	}

	public void setMessageTemplateRowId(int messageTemplateRowId) {
		this.messageTemplateRowId = messageTemplateRowId;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Integer getClientDataRowId() {
		return clientDataRowId;
	}

	public void setClientDataRowId(Integer clientDataRowId) {
		this.clientDataRowId = clientDataRowId;
	}

	public Integer getCustomerDataRowId() {
		return customerDataRowId;
	}

	public void setCustomerDataRowId(Integer customerDataRowId) {
		this.customerDataRowId = customerDataRowId;
	}

	public Integer getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public TemplateData getMessageTemplate() {
		return messageTemplate;
	}

	public void setMessageTemplate(TemplateData messageTemplate) {
		this.messageTemplate = messageTemplate;
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}

	public CustomerData getCustomerData() {
		return customerData;
	}

	public void setCustomerData(CustomerData customerData) {
		this.customerData = customerData;
	}

}