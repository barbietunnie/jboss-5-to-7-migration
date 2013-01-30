package com.pra.rave.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ODM5_ITEM_GROUP")
public class ItemGroup extends BaseModel {
	private static final long serialVersionUID = 3886138182695172772L;

	@Column(name="Item_Group_OID", nullable=false, length=100)
	private String itemGroupOID;
	@Column(name="Item_Group_Repeat_Key", nullable=true, length=30)
	private String itemGroupRepeatKey;

	@ManyToOne(targetEntity=FormData.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="Form_Data_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private FormData formData;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="itemGroup", fetch=FetchType.LAZY, orphanRemoval=true)
	private List<ItemData> itemDataList;

	@OneToOne(cascade=CascadeType.ALL, mappedBy="itemGroup", fetch=FetchType.LAZY, orphanRemoval=true)
	private RaveAdverse1 raveAdverse1;

	@OneToOne(cascade=CascadeType.ALL, mappedBy="itemGroup", fetch=FetchType.LAZY, orphanRemoval=true)
	private RaveAescr1 raveAescr1;

	@OneToOne(cascade=CascadeType.ALL, mappedBy="itemGroup", fetch=FetchType.LAZY, orphanRemoval=true)
	private RaveSrf1 raveSrf1;

	@OneToOne(cascade=CascadeType.ALL, mappedBy="itemGroup", fetch=FetchType.LAZY, orphanRemoval=true)
	private RaveSubject raveSubject;

	@OneToOne(cascade=CascadeType.ALL, mappedBy="itemGroup", fetch=FetchType.LAZY, orphanRemoval=true)
	private RaveIpadmin1 raveIpadmin1;

	public ItemGroup() {}

	public String getItemGroupOID() {
		return itemGroupOID;
	}

	public void setItemGroupOID(String itemGroupOID) {
		this.itemGroupOID = itemGroupOID;
	}

	public String getItemGroupRepeatKey() {
		return itemGroupRepeatKey;
	}

	public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
		this.itemGroupRepeatKey = itemGroupRepeatKey;
	}

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}

	public List<ItemData> getItemDataList() {
		return itemDataList;
	}

	public void setItemDataList(List<ItemData> itemDataList) {
		this.itemDataList = itemDataList;
	}

	public RaveAdverse1 getRaveAdverse1() {
		return raveAdverse1;
	}

	public void setRaveAdverse1(RaveAdverse1 raveAdverse1) {
		this.raveAdverse1 = raveAdverse1;
	}

	public RaveAescr1 getRaveAescr1() {
		return raveAescr1;
	}

	public void setRaveAescr1(RaveAescr1 raveAescr1) {
		this.raveAescr1 = raveAescr1;
	}

	public RaveSrf1 getRaveSrf1() {
		return raveSrf1;
	}

	public void setRaveSrf1(RaveSrf1 raveSrf1) {
		this.raveSrf1 = raveSrf1;
	}

	public RaveSubject getRaveSubject() {
		return raveSubject;
	}

	public void setRaveSubject(RaveSubject raveSubject) {
		this.raveSubject = raveSubject;
	}

	public RaveIpadmin1 getRaveIpadmin1() {
		return raveIpadmin1;
	}

	public void setRaveIpadmin1(RaveIpadmin1 raveIpadmin1) {
		this.raveIpadmin1 = raveIpadmin1;
	}

}
