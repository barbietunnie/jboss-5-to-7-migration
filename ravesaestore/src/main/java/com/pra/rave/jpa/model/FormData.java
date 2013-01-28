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
@Table(name="ODM4_FORM")
public class FormData extends BaseModel {
	private static final long serialVersionUID = 7614559432705867458L;

	@Column(name="Form_OID", nullable=false, length=100)
	private String formOID;
	@Column(name="Form_Repeat_Key", nullable=true, length=30)
	private String formRepeatKey;

	@ManyToOne(targetEntity=StudyEvent.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="Study_Event_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private StudyEvent studyEvent;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="formData", fetch=FetchType.LAZY, orphanRemoval=true)
	private List<ItemGroup> itemGroups;

	public FormData() {}

	public String getFormOID() {
		return formOID;
	}

	public void setFormOID(String formOID) {
		this.formOID = formOID;
	}

	public String getFormRepeatKey() {
		return formRepeatKey;
	}

	public void setFormRepeatKey(String formRepeatKey) {
		this.formRepeatKey = formRepeatKey;
	}

	public StudyEvent getStudyEvent() {
		return studyEvent;
	}

	public void setStudyEvent(StudyEvent studyEvent) {
		this.studyEvent = studyEvent;
	}

	public List<ItemGroup> getItemGroups() {
		return itemGroups;
	}

	public void setItemGroups(List<ItemGroup> itemGroups) {
		this.itemGroups = itemGroups;
	}

}
