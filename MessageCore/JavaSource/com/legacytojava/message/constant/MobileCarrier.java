package com.legacytojava.message.constant;

public enum MobileCarrier {
	TMobile("T-Mobile USA", "tmomail.net", ""),
	Verizon("Verizon Wireless", "vtext.com", "vzwpix.com"),
	ATT("AT&T Mobility", "txt.att.net", "mms.att.net"),
	Sprint("Sprint Nextel", "messaging.sprintpcs.com", ""),
	TracFone("TracFone Wireless", "", ""),
	MetroPCS("MetroPCS", "mymetropcs.com", ""),
	USCellular("U.S. Cellular", "email.uscc.net", ""),
	Leap("Leap Wireless", "", ""),
	Alltel("Alltel", "message.alltel.com", ""),
	Boost("Boost Mobile","myboostmobile.com",""),
	Nextel("Nextel", "messaging.nextel.com", ""),
	Virgin("Virgin Mobile USA", "vmobl.com", "");
	
	private String value;
	private String text;
	private String mmedia;
	MobileCarrier(String value, String text, String mmedia) {
		this.value=value;
		this.text=text;
		this.mmedia=mmedia;
	}
	public String getValue() {
		return value;
	}
	public String getText() {
		return text;
	}
	public String getMmedia() {
		return mmedia;
	}

	public static MobileCarrier getByValue(String value) {
		for (MobileCarrier c : MobileCarrier.values()) {
			if (c.getValue().equalsIgnoreCase(value)) {
				return c;
			}
		}
		throw new IllegalArgumentException("No enum value (" + value + ") defined in class com.legacytojava.message.constant.MobileCarrier.");
	}

	public static void main(String[] args) {
		System.out.println(MobileCarrier.valueOf("TMobile"));
		System.out.println(getByValue("T-Mobile USA").getText());
	}
}
