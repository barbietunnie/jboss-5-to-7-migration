package jpa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class RuleBase implements java.io.Serializable {
	private static final long serialVersionUID = -2619176738651938695L;
	protected static final Logger logger = Logger.getLogger(RuleBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();

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