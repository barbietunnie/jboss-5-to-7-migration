package jpa.constant;

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

}
