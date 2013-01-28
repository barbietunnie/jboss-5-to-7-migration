package com.pra.rave.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class StudyPK implements java.io.Serializable {
	private static final long serialVersionUID = -1246061498854616740L;
	@Column(name="Study_OID", nullable=false, length=100)
	private String studyOID;

	public StudyPK() {}
	public StudyPK(String studyOid) {
		this.studyOID = studyOid;
	}

	public String getStudyOID() {
		return studyOID;
	}
	public void setStudyOID(String studyOID) {
		this.studyOID = studyOID;
	}
}
