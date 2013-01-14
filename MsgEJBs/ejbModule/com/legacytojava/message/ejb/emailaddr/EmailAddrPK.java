package com.legacytojava.message.ejb.emailaddr;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmailAddrPK implements java.io.Serializable {
	private static final long serialVersionUID = 1906709070445493504L;

	public EmailAddrPK() {}
	
	@Column(name = "EmailAddr")
	private String emailAddr;
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	public EmailAddrPK(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
}
