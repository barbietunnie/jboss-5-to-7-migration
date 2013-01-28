package com.pra.rave.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ODM6_ITEM")
public class ItemData extends BaseModel {
	private static final long serialVersionUID = -8623715784096232428L;

	@Column(name="Item_OID", nullable=true, length=100)
	private String itemOID;
	@Column(name="Is_Null", nullable=true, length=10)
	private String isNull;
	@Column(name="Item_Value", nullable=true)
	private String itemValue;

	@ManyToOne(targetEntity=ItemGroup.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="Item_Group_Id", referencedColumnName="Id", insertable=true, updatable=false)
	private ItemGroup itemGroup;

	public ItemData() {}

	public String getItemOID() {
		return itemOID;
	}

	public void setItemOID(String itemOID) {
		this.itemOID = itemOID;
	}

	public String getIsNull() {
		return isNull;
	}

	public void setIsNull(String isNull) {
		this.isNull = isNull;
	}

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public void setItemGroup(ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

}
