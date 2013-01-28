package com.pra.rave.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="ODM2_SUBJECT", uniqueConstraints=@UniqueConstraint(columnNames = {"Study_Id","Subject_Key"}))
public class Subject extends BaseModel {
	private static final long serialVersionUID = -5032994057868896936L;

	@Column(name="Subject_Key", nullable=false, length=100)
	private String subjectKey;
	@Column(name="Location_OID", nullable=false)
	private String locationOID;

	@ManyToOne(targetEntity=Study.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="Study_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private Study study;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="subject", fetch=FetchType.LAZY, orphanRemoval=true)
	private List<StudyEvent> studyEvents;
	
	public Subject() {}

	public String getSubjectKey() {
		return subjectKey;
	}

	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}

	public String getLocationOID() {
		return locationOID;
	}

	public void setLocationOID(String locationOID) {
		this.locationOID = locationOID;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public List<StudyEvent> getStudyEvents() {
		return studyEvents;
	}

	public void setStudyEvents(List<StudyEvent> studyEvents) {
		this.studyEvents = studyEvents;
	}
	
}
