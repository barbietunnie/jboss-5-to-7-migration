package jpa.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jpa.constant.EmailAddrType;
import jpa.constant.VariableName;

import org.apache.log4j.Logger;

public abstract class RuleBase implements java.io.Serializable {
	private static final long serialVersionUID = -2619176738651938695L;
	protected static final Logger logger = Logger.getLogger(RuleBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();

	/** define rule type constants */
	public static final String SIMPLE_RULE = "Simple";
	public static final String ALL_RULE = "All";
	public static final String ANY_RULE = "Any";
	public static final String NONE_RULE = "None";
	
	public static final String PRE_RULE = "E";
	public static final String MAIN_RULE = "M";
	public static final String POST_RULE = "P";

	/** define criteria constants for simple rule */
	public static final String STARTS_WITH = "starts_with";
	public static final String ENDS_WITH = "ends_with";
	public static final String CONTAINS = "contains";
	public static final String EQUALS = "equals";
	public static final String GREATER_THAN = "greater_than";
	public static final String LESS_THAN = "less_than";
	public static final String VALUED = "valued";
	public static final String NOT_VALUED = "not_valued";
	public static final String REG_EX = "reg_ex";

	public static final String[] CRITERIAS = 
		{ STARTS_WITH, ENDS_WITH, CONTAINS, EQUALS, GREATER_THAN, LESS_THAN, VALUED, NOT_VALUED, REG_EX };

	/** define data type constants for Internet mail */
	public static final String FROM_ADDR = EmailAddrType.FROM_ADDR.getValue();
	public static final String TO_ADDR = EmailAddrType.TO_ADDR.getValue();
	public static final String REPLYTO_ADDR = EmailAddrType.REPLYTO_ADDR.getValue();
	public static final String CC_ADDR = EmailAddrType.CC_ADDR.getValue();
	public static final String BCC_ADDR = EmailAddrType.BCC_ADDR.getValue();

	public static final String SUBJECT = VariableName.SUBJECT;
	public static final String BODY = VariableName.BODY;
	public static final String MSG_REF_ID = VariableName.MSG_REF_ID;
	public static final String RULE_NAME = VariableName.RULE_NAME;
	public static final String X_HEADER = VariableName.XHEADER_DATA_NAME;
	public static final String RETURN_PATH = "ReturnPath";
	// mailbox properties
	public static final String MAILBOX_USER = VariableName.MAILBOX_USER;
	public static final String MAILBOX_HOST = VariableName.MAILBOX_HOST;
	// the next two items are not implemented yet
	public static final String RFC822 = VariableName.RFC822;
	public static final String DELIVERY_STATUS = VariableName.DELIVERY_STATUS;

	public static final String[] DATATYPES = 
		{ FROM_ADDR, TO_ADDR, REPLYTO_ADDR, CC_ADDR, BCC_ADDR,
			SUBJECT, BODY, MSG_REF_ID, RULE_NAME, MAILBOX_USER, MAILBOX_HOST };

	/** define data type constants for Internet email attachments */
	public static final String MIME_TYPE = "MimeType";
	public static final String FILE_NAME = "FileName";

	final static String LF = System.getProperty("line.separator", "\n");

	// store rule names found in rules.xml
	private final static Set<String> ruleNameList = Collections.synchronizedSet(new HashSet<String>());

	protected final String ruleName, ruleType;
	protected String dataName;
	protected String headerName;
	protected final String mailType, criteria;
	protected final boolean caseSensitive;

	protected final List<String> subRuleList = new ArrayList<String>();

	public RuleBase(String _ruleName, 
			String _ruleType, 
			String _mailType, 
			String _dataName,
			String _headerName,
			String _criteria, 
			String _caseSensitive) {
		this.ruleName = _ruleName;
		this.ruleType = _ruleType;
		this.mailType = _mailType;
		this.dataName = _dataName;
		this.headerName = _headerName;
		this.criteria = _criteria;
		this.caseSensitive = "Y".equalsIgnoreCase(_caseSensitive);
		if (this.ruleName != null && !ruleNameList.contains(this.ruleName))
			ruleNameList.add(this.ruleName);
	}
	
	public String getRuleName() {
		return ruleName;
	}

	protected String getRuleName(int len) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<len-ruleName.length(); i++)
			sb.append(" ");
		return ruleName + sb.toString();
	}
	
	public String getRuleType() {
		return mailType;
	}

	public String getDataName() {
		return dataName;
	}

	public String getHeaderName() {
		return headerName;
	}

	public List<String> getSubRules() {
		return subRuleList;
	}

	public String getRuleContent() {
		StringBuffer sb = new StringBuffer();
		sb.append(LF + "---- listing rule content for " + ruleName + " ----" + LF);
		sb.append("Rule Name: " + ruleName + LF);
		sb.append("Rule Type: " + ruleType + LF);
		sb.append("Mail Type: " + mailType + LF);
		sb.append("Data Type: " + dataName + LF);
		if (headerName != null)
			sb.append("Header Name: " + headerName + LF);
		sb.append("Criteria : " + criteria + LF);
		sb.append("Case Sensitive : " + caseSensitive + LF);
		if (subRuleList != null) {
			sb.append("SubRule List:" + LF);
			for (int i = 0; i < subRuleList.size(); i++) {
				sb.append("     " + subRuleList.get(i) + LF);
			}
		}

		return sb.toString();
	}
	
	public abstract String match(String mail_type, String data_type, String data);
	
	public abstract String match(String mail_type, Object mail_obj);
}