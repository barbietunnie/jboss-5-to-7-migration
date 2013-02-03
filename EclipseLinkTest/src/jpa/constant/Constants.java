package jpa.constant;

public class Constants {
	public final static String DEFAULT_USER_ID = "MsgMaint";
	public final static String DEFAULT_CLIENTID = "System";

	public final static String VENDER_DOMAIN_NAME = "Emailsphere.com";
	public final static String VENDER_SUPPORT_EMAIL = "support@" + VENDER_DOMAIN_NAME;
	public final static String POWERED_BY_HTML_TAG = "<div style='color: blue;'>Powered by " +
			"<a style='color: darkblue;' href='http://www." + VENDER_DOMAIN_NAME + "' target='_blank'>"+ VENDER_DOMAIN_NAME + "</a></div>";
	public final static String POWERED_BY_TEXT = "Powered by " + VENDER_DOMAIN_NAME;

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
