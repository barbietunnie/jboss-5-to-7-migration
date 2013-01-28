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

@Entity
@Table(name="ODM3_STUDY_EVENT")
public class StudyEvent extends BaseModel {
	private static final long serialVersionUID = 137072261293490122L;

	@Column(name="Study_Event_OID", nullable=false, length=100)
	private String studyEventOID;
	@Column(name="Study_Event_Repeat_Key", nullable=true, length=30)
	private String studyEventRepeatKey;

	@ManyToOne(targetEntity=Subject.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="Subject_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private Subject subject;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="studyEvent", fetch=FetchType.LAZY, orphanRemoval=true)
	private List<FormData> formDataList;

	public StudyEvent() {}

	public String getStudyEventOID() {
		return studyEventOID;
	}

	public void setStudyEventOID(String studyEventOID) {
		this.studyEventOID = studyEventOID;
	}

	public String getStudyEventRepeatKey() {
		return studyEventRepeatKey;
	}

	public void setStudyEventRepeatKey(String studyEventRepeatKey) {
		this.studyEventRepeatKey = studyEventRepeatKey;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public List<FormData> getFormDataList() {
		return formDataList;
	}

	public void setFormDataList(List<FormData> formDataList) {
		this.formDataList = formDataList;
	}
	
}
