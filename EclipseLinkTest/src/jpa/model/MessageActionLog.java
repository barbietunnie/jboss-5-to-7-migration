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
@Table(name="message_action_log", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId"}))
public class MessageActionLog extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 60873582256305774L;

	@OneToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private MessageInbox messageInbox;

	@Column(length=50, nullable=false)
	private String actionService = "";
	@Column(length=255, nullable=true)
	private String parameters = null;

	public MessageActionLog() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public String getActionService() {
		return actionService;
	}

	public void setActionService(String actionService) {
		this.actionService = actionService;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
}
