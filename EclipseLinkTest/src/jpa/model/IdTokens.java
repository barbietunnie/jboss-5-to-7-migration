package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="IdTokens", uniqueConstraints=@UniqueConstraint(columnNames = {"clientId"}))
public class IdTokens extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -632308305179136081L;

	@Column(name="ClientId", unique=true, nullable=false, length=16)
	//@OneToOne(targetEntity=Clients.class, fetch=FetchType.LAZY)
	//@JoinColumn(name="ClientId", columnDefinition="clientId")
	private String clientId = "";
	@Column(nullable=true, length=100)
	private String description = null;
	@Column(nullable=false, length=16)
	private String bodyBeginToken = "";
	@Column(nullable=false, length=4)
	private String bodyEndToken = "";
	@Column(length=20)
	private String xHeaderName = null;
	@Column(length=16)
	private String xhdrBeginToken = null;
	@Column(length=4)
	private String xhdrEndToken = null;
	@Column(length=11)
	private int maxLength = -1;

	public IdTokens() {
		// must have a no-argument constructor
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBodyBeginToken() {
		return bodyBeginToken;
	}

	public void setBodyBeginToken(String bodyBeginToken) {
		this.bodyBeginToken = bodyBeginToken;
	}

	public String getBodyEndToken() {
		return bodyEndToken;
	}

	public void setBodyEndToken(String bodyEndToken) {
		this.bodyEndToken = bodyEndToken;
	}

	public String getxHeaderName() {
		return xHeaderName;
	}

	public void setxHeaderName(String xHeaderName) {
		this.xHeaderName = xHeaderName;
	}

	public String getXhdrBeginToken() {
		return xhdrBeginToken;
	}

	public void setXhdrBeginToken(String xhdrBeginToken) {
		this.xhdrBeginToken = xhdrBeginToken;
	}

	public String getXhdrEndToken() {
		return xhdrEndToken;
	}

	public void setXhdrEndToken(String xhdrEndToken) {
		this.xhdrEndToken = xhdrEndToken;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
}
