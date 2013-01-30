package com.pra.rave.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="Rave_Ipadmin1")
public class RaveIpadmin1 extends BaseModel {
	private static final long serialVersionUID = -1169938721377215510L;

	@OneToOne(targetEntity=ItemGroup.class, optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="Item_Group_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private ItemGroup itemGroup;

	@Column(nullable=true)
	private java.sql.Date startdt;
	@Column(nullable=true)
	private java.sql.Time starttm;
	@Column(nullable=true)
	private java.sql.Time stoptm;
	@Column(nullable=true, length=100)
	private String dose_ip;
	
	public RaveIpadmin1() {}

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public void setItemGroup(ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

	public java.sql.Date getStartdt() {
		return startdt;
	}

	public void setStartdt(java.sql.Date startdt) {
		this.startdt = startdt;
	}

	public java.sql.Time getStarttm() {
		return starttm;
	}

	public void setStarttm(java.sql.Time starttm) {
		this.starttm = starttm;
	}

	public java.sql.Time getStoptm() {
		return stoptm;
	}

	public void setStoptm(java.sql.Time stoptm) {
		this.stoptm = stoptm;
	}

	public String getDose_ip() {
		return dose_ip;
	}

	public void setDose_ip(String dose_ip) {
		this.dose_ip = dose_ip;
	}

}
