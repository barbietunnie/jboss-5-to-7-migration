package com.pra.rave.jpa.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="ODM1_STUDY", uniqueConstraints=@UniqueConstraint(columnNames = {"Study_OID"}))
public class Study extends BaseModel {
	private static final long serialVersionUID = -2325099904457822803L;
	@Embedded
	private StudyPK studyPK;
	@Column(name="Study_Name", nullable=true)
	private String studyName;
	@Column(name="Protocol_Name", nullable=true)
	private String protocolName;
	@Column(name="MetaData_Version_OID", nullable=true, length=30)
	private String metaDataVersionOID;

	@Column(name="Adverse1_Load_Dt", nullable=true)
	private Timestamp adverse1LoadDt;
	@Column(name="Aescr1_Load_Dt", nullable=true)
	private Timestamp aescr1LoadDt;
	@Column(name="Srf1_Load_Dt", nullable=true)
	private Timestamp srf1LoadDt;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="study", fetch=FetchType.LAZY, orphanRemoval=true)
	private List<Subject> subjects;
	
	public Study() {}
	
	public StudyPK getStudyPK() {
		return studyPK;
	}
	public void setStudyPK(StudyPK studyPK) {
		this.studyPK = studyPK;
	}
	public String getStudyName() {
		return studyName;
	}
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	public String getProtocolName() {
		return protocolName;
	}
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
	public String getMetaDataVersionOID() {
		return metaDataVersionOID;
	}
	public void setMetaDataVersionOID(String metaDataVersionOID) {
		this.metaDataVersionOID = metaDataVersionOID;
	}
	public Timestamp getAdverse1LoadDt() {
		return adverse1LoadDt;
	}

	public void setAdverse1LoadDt(Timestamp adverse1LoadDt) {
		this.adverse1LoadDt = adverse1LoadDt;
	}

	public Timestamp getAescr1LoadDt() {
		return aescr1LoadDt;
	}

	public void setAescr1LoadDt(Timestamp aescr1LoadDt) {
		this.aescr1LoadDt = aescr1LoadDt;
	}

	public Timestamp getSrf1LoadDt() {
		return srf1LoadDt;
	}

	public void setSrf1LoadDt(Timestamp srf1LoadDt) {
		this.srf1LoadDt = srf1LoadDt;
	}

	public List<Subject> getSubjects() {
		return subjects;
	}
	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}
}
