package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="id_tokens")
public class IdTokens extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -632308305179136081L;

	@OneToOne(targetEntity=ClientData.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ClientDataRowId", referencedColumnName="Row_Id", columnDefinition="int", nullable=false)
	private ClientData clientData;

	@Column(nullable=true, length=100)
	private String description = null;
	@Column(nullable=false, length=16)
	private String bodyBeginToken = "";
	@Column(nullable=false, length=4)
	private String bodyEndToken = "";
	@Column(length=20)
	private String xheaderName = null;
	@Column(length=16)
	private String xhdrBeginToken = null;
	@Column(length=4)
	private String xhdrEndToken = null;
	@Column(nullable=false)
	private int maxLength = -1;

	public IdTokens() {
		// must have a no-argument constructor
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
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

	public String getXheaderName() {
		return xheaderName;
	}

	public void setXheaderName(String xheaderName) {
		this.xheaderName = xheaderName;
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
