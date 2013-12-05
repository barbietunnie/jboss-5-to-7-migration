package com.es.vo.address;

import java.io.Serializable;
import com.es.vo.comm.BaseVoWithRowId;

public class MobileCarrierVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = 5518151528344528993L;

	private String carrierName = "";
	private String countryCode = null;
	private String multiMediaAddress = null;
	private String textAddress = "";

	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getMultiMediaAddress() {
		return multiMediaAddress;
	}
	public void setMultiMediaAddress(String multiMediaAddress) {
		this.multiMediaAddress = multiMediaAddress;
	}
	public String getTextAddress() {
		return textAddress;
	}
	public void setTextAddress(String textAddress) {
		this.textAddress = textAddress;
	}
	
}