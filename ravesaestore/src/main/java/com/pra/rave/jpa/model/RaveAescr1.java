package com.pra.rave.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name="Rave_Aescr_1")
public class RaveAescr1 extends BaseModel {
	private static final long serialVersionUID = -2404036144496253117L;

	@OneToOne(targetEntity=ItemGroup.class, optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="Item_Group_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private ItemGroup itemGroup;

	@Index(name="aescr_1_case_number")
	@Column(nullable=true, length=100)
	private String aecasnum;
	@Column(nullable=true, length=255)
	private String aevt;
	@Column(nullable=true)
	private java.sql.Date aestdt;
	@Column(nullable=true)
	private java.sql.Date aeendt;
	@Column(nullable=true, length=10)
	private String relprny;
	@Column(nullable=true, length=10)
	private String relchemo;
	@Column(nullable=true, length=100)
	private String aeacnct1;
	@Column(nullable=true, length=100)
	private String aeacnct2;
	@Column(nullable=true, length=255)
	private String aesout;
	@Column(nullable=true, length=10)
	private String serny;
	@Column(nullable=true, length=10)
	private String aesdth;
	@Column(nullable=true, length=10)
	private String aeslife;
	@Column(nullable=true, length=10)
	private String aeshosp;
	@Column(nullable=true, length=100)
	private String aesaddtc;
	@Column(nullable=true, length=20)
	private String aesdsdtc;
	@Column(nullable=true, length=20)
	private String aesdisab;
	@Column(nullable=true, length=10)
	private String aescong;
	@Column(nullable=true, length=255)
	private String aesmie;
	
	public RaveAescr1() {}

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public void setItemGroup(ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

	public String getAecasnum() {
		return aecasnum;
	}

	public void setAecasnum(String aecasnum) {
		this.aecasnum = aecasnum;
	}

	public String getAevt() {
		return aevt;
	}

	public void setAevt(String aevt) {
		this.aevt = aevt;
	}

	public java.sql.Date getAestdt() {
		return aestdt;
	}

	public void setAestdt(java.sql.Date aestdt) {
		this.aestdt = aestdt;
	}

	public java.sql.Date getAeendt() {
		return aeendt;
	}

	public void setAeendt(java.sql.Date aeendt) {
		this.aeendt = aeendt;
	}

	public String getRelprny() {
		return relprny;
	}

	public void setRelprny(String relprny) {
		this.relprny = relprny;
	}

	public String getRelchemo() {
		return relchemo;
	}

	public void setRelchemo(String relchemo) {
		this.relchemo = relchemo;
	}

	public String getAeacnct1() {
		return aeacnct1;
	}

	public void setAeacnct1(String aeacnct1) {
		this.aeacnct1 = aeacnct1;
	}

	public String getAeacnct2() {
		return aeacnct2;
	}

	public void setAeacnct2(String aeacnct2) {
		this.aeacnct2 = aeacnct2;
	}

	public String getAesout() {
		return aesout;
	}

	public void setAesout(String aesout) {
		this.aesout = aesout;
	}

	public String getSerny() {
		return serny;
	}

	public void setSerny(String serny) {
		this.serny = serny;
	}

	public String getAesdth() {
		return aesdth;
	}

	public void setAesdth(String aesdth) {
		this.aesdth = aesdth;
	}

	public String getAeslife() {
		return aeslife;
	}

	public void setAeslife(String aeslife) {
		this.aeslife = aeslife;
	}

	public String getAeshosp() {
		return aeshosp;
	}

	public void setAeshosp(String aeshosp) {
		this.aeshosp = aeshosp;
	}

	public String getAesaddtc() {
		return aesaddtc;
	}

	public void setAesaddtc(String aesaddtc) {
		this.aesaddtc = aesaddtc;
	}

	public String getAesdsdtc() {
		return aesdsdtc;
	}

	public void setAesdsdtc(String aesdsdtc) {
		this.aesdsdtc = aesdsdtc;
	}

	public String getAesdisab() {
		return aesdisab;
	}

	public void setAesdisab(String aesdisab) {
		this.aesdisab = aesdisab;
	}

	public String getAescong() {
		return aescong;
	}

	public void setAescong(String aescong) {
		this.aescong = aescong;
	}

	public String getAesmie() {
		return aesmie;
	}

	public void setAesmie(String aesmie) {
		this.aesmie = aesmie;
	}

}
