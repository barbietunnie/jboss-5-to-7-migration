package com.pra.rave.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name="Rave_Srf_1")
public class RaveSrf1 extends BaseModel {
	private static final long serialVersionUID = -5618904804975189364L;

	@OneToOne(targetEntity=ItemGroup.class, optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="Item_Group_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private ItemGroup itemGroup;

	@Index(name="srf_1_case_number")
	@Column(nullable=true, length=100)
	private String srcasnum;
	@Column(nullable=true, length=10)
	private String srser;
	@Column(nullable=true, length=30)
	private String srctry;
	@Column(nullable=true, length=255)
	private String srcasdtl;
	@Column(nullable=true, length=255)
	private String srlabdtl;
	@Column(nullable=true, length=10)
	private String srnull;
	@Column(nullable=true, length=10)
	private String srdth;
	@Column(nullable=true, length=10)
	private String srlife;
	@Column(nullable=true, length=10)
	private String srhosp;
	@Column(nullable=true, length=20)
	private String srdisab;
	@Column(nullable=true, length=10)
	private String srcong;
	@Column(nullable=true, length=255)
	private String srmie;
	@Column(nullable=true, length=20)
	private String srdthdtc;
	@Column(nullable=true, length=100)
	private String sraedth;
	@Column(nullable=true, length=20)
	private String srdthfmt;
	@Column(nullable=true, length=100)
	private String srtrnid;
	@Column(nullable=true, length=30)
	private String sraeform;
	@Column(nullable=true, length=30)
	private String sraefld;	
	@Column(nullable=true)
	private java.sql.Date srtrndtc;
	@Column(nullable=true, length=10)
	private String srtrnfl;
	
	public RaveSrf1() {}

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public void setItemGroup(ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

	public String getSrcasnum() {
		return srcasnum;
	}

	public void setSrcasnum(String srcasnum) {
		this.srcasnum = srcasnum;
	}

	public String getSrser() {
		return srser;
	}

	public void setSrser(String srser) {
		this.srser = srser;
	}

	public String getSrctry() {
		return srctry;
	}

	public void setSrctry(String srctry) {
		this.srctry = srctry;
	}

	public String getSrcasdtl() {
		return srcasdtl;
	}

	public void setSrcasdtl(String srcasdtl) {
		this.srcasdtl = srcasdtl;
	}

	public String getSrlabdtl() {
		return srlabdtl;
	}

	public void setSrlabdtl(String srlabdtl) {
		this.srlabdtl = srlabdtl;
	}

	public String getSrnull() {
		return srnull;
	}

	public void setSrnull(String srnull) {
		this.srnull = srnull;
	}

	public String getSrdth() {
		return srdth;
	}

	public void setSrdth(String srdth) {
		this.srdth = srdth;
	}

	public String getSrlife() {
		return srlife;
	}

	public void setSrlife(String srlife) {
		this.srlife = srlife;
	}

	public String getSrhosp() {
		return srhosp;
	}

	public void setSrhosp(String srhosp) {
		this.srhosp = srhosp;
	}

	public String getSrdisab() {
		return srdisab;
	}

	public void setSrdisab(String srdisab) {
		this.srdisab = srdisab;
	}

	public String getSrcong() {
		return srcong;
	}

	public void setSrcong(String srcong) {
		this.srcong = srcong;
	}

	public String getSrmie() {
		return srmie;
	}

	public void setSrmie(String srmie) {
		this.srmie = srmie;
	}

	public String getSrdthdtc() {
		return srdthdtc;
	}

	public void setSrdthdtc(String srdthdtc) {
		this.srdthdtc = srdthdtc;
	}

	public String getSraedth() {
		return sraedth;
	}

	public void setSraedth(String sraedth) {
		this.sraedth = sraedth;
	}

	public String getSrdthfmt() {
		return srdthfmt;
	}

	public void setSrdthfmt(String srdthfmt) {
		this.srdthfmt = srdthfmt;
	}

	public String getSrtrnfl() {
		return srtrnfl;
	}

	public void setSrtrnfl(String srtrnfl) {
		this.srtrnfl = srtrnfl;
	}

	public String getSrtrnid() {
		return srtrnid;
	}

	public void setSrtrnid(String srtrnid) {
		this.srtrnid = srtrnid;
	}

	public java.sql.Date getSrtrndtc() {
		return srtrndtc;
	}

	public void setSrtrndtc(java.sql.Date srtrndtc) {
		this.srtrndtc = srtrndtc;
	}

	public String getSraeform() {
		return sraeform;
	}

	public void setSraeform(String sraeform) {
		this.sraeform = sraeform;
	}

	public String getSraefld() {
		return sraefld;
	}

	public void setSraefld(String sraefld) {
		this.sraefld = sraefld;
	}

}
