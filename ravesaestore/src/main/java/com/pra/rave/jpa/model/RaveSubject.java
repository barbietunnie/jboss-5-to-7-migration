package com.pra.rave.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name="Rave_Subject")
public class RaveSubject extends BaseModel {
	private static final long serialVersionUID = -8487717684896575179L;

	@OneToOne(targetEntity=ItemGroup.class, optional=false, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="Item_Group_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private ItemGroup itemGroup;

	@Index(name="subject_patient_id")
	@Column(nullable=true, length=20)
	private String pt_id;
	@Column(nullable=true, length=100)
	private String pt;
	@Column(nullable=true)
	private java.sql.Date base_birthdt;
	@Column(nullable=true, length=30)
	private String siteid;
	
	public RaveSubject() {}

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public void setItemGroup(ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

	public String getPt_id() {
		return pt_id;
	}

	public void setPt_id(String pt_id) {
		this.pt_id = pt_id;
	}

	public String getPt() {
		return pt;
	}

	public void setPt(String pt) {
		this.pt = pt;
	}

	public java.sql.Date getBase_birthdt() {
		return base_birthdt;
	}

	public void setBase_birthdt(java.sql.Date base_birthdt) {
		this.base_birthdt = base_birthdt;
	}

	public String getSiteid() {
		return siteid;
	}

	public void setSiteid(String siteid) {
		this.siteid = siteid;
	}

}
