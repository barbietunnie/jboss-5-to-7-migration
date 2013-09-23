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
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.model.BaseModel;
import jpa.model.SenderData;
import jpa.model.SubscriberData;

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
	@OrderBy
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

	@Column(name="SenderDataRowId", nullable=true, columnDefinition="Integer")
	private Integer senderDataRowId = null;
	@Transient
	private SenderData senderData;

	@Column(name="SubscriberDataRowId", nullable=true, columnDefinition="Integer")
	private Integer subscriberDataRowId = null;
	@Transient
	private SubscriberData subscriberData;

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

	public Integer getSenderDataRowId() {
		return senderDataRowId;
	}

	public void setSenderDataRowId(Integer senderDataRowId) {
		this.senderDataRowId = senderDataRowId;
	}

	public Integer getSubscriberDataRowId() {
		return subscriberDataRowId;
	}

	public void setSubscriberDataRowId(Integer subscriberDataRowId) {
		this.subscriberDataRowId = subscriberDataRowId;
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

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}

	public SubscriberData getSubscriberData() {
		return subscriberData;
	}

	public void setSubscriberData(SubscriberData subscriberData) {
		this.subscriberData = subscriberData;
	}

}