package jpa.constant;

public class Constants {
	public final static String DEFAULT_USER_ID = "MsgMaint";
	public final static String DEFAULT_CLIENTID = "System";

	public static enum Code {
		YES_CODE("Y"),
		NO_CODE("N"),
		YES("Yes"),
		NO("No");
		
		private final String value;
		private Code(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
}
