package com.legacytojava.message.bo.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RuleSimple extends RuleBase {
	private static final long serialVersionUID = -1386955504774162841L;
	protected static final Logger logger = Logger.getLogger(RuleSimple.class);
	
	final String targetText;
	
	private final String storedProcedure = null;
	private List<String> exclusionList = null;
	private Set<String> exclusionSet = null;
	private final Pattern pattern;
	
	public RuleSimple(String _ruleName, 
			String _ruleType, 
			String _mailType, 
			String _dataName,
			String _headerName,
			String _criteria, 
			String _case_sensitive, 
			String _targetText, 
			String _exclusion_list,
			String _stored_procedure,
			String _delimiter) {
		super(_ruleName, _ruleType, _mailType, _dataName, _headerName, _criteria, _case_sensitive);
		if (caseSensitive) {
			this.targetText = _targetText;
		}
		else {
			this.targetText = _targetText.toLowerCase();
		}
		setExclusionList(_exclusion_list, _delimiter);
		setStoredProcedure(_stored_procedure);
		if (REG_EX.equals(_criteria)) {
			if (caseSensitive) {
				// enables dotall mode
				pattern = Pattern.compile(this.targetText, Pattern.DOTALL);
			}
			else {
				// enables case-insensitive matching and dotall mode
				pattern = Pattern.compile(this.targetText, Pattern.CASE_INSENSITIVE
						| Pattern.DOTALL);
			}
		}
		else {
			pattern = null;
		}
		logger.info(">>>>> Simple-Rule initialized for " + ruleName);
	}

	private void setExclusionList(String _exclusionList, String _delimiter) {
		if (_exclusionList != null && _delimiter!=null) {
			exclusionList = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(_exclusionList, _delimiter);
			while (st.hasMoreTokens()) {
				exclusionList.add(st.nextToken());
			}
			
			if (!caseSensitive) {
				// convert the list to lower case
				for (int i = 0; i < this.exclusionList.size(); i++) {
					String str = (String) this.exclusionList.get(i);
					if (str != null) // just for safety
						this.exclusionList.set(i, str.toLowerCase());
				}
			}

			logger.info("----- Exclusion List for Rule: " + ruleName + ", type: " + mailType);
			for (int i = 0; i < this.exclusionList.size(); i++) {
				logger.info("      " + this.exclusionList.get(i));
			}
			
			//this.exclusionSet = Collections.synchronizedSet(new HashSet(this.exclusionList));
			this.exclusionSet = new HashSet<String>(this.exclusionList);
		}
	}

	private void setStoredProcedure(String _storedProcedure) {
	}

	public String match(String mail_type, String data_type, String data) {
		if (mail_type==null || !mail_type.equals(mailType))
			return null;
		if (data_type==null || !data_type.equals(dataName))
			return null;
		
		if (data == null) data = ""; // just for safety
		if (!caseSensitive) {
			data = data.toLowerCase();
		}
		if (isDebugEnabled) {
			logger.debug("[" + getRuleName(20) + "] [" + mailType + "] [" + data_type + "] data: ["
					+ data + "] targetText: [" + targetText + "]");
		}
		boolean criteria_met = false;

		if (criteria.equals(STARTS_WITH)) {
			if (data.startsWith(targetText))
				criteria_met = true;
		}
		else if (criteria.equals(ENDS_WITH)) {
			if (data.endsWith(targetText))
				criteria_met = true;
		}
		else if (criteria.equals(CONTAINS)) {
			if (data.indexOf(targetText) >= 0)
				criteria_met = true;
		}
		else if (criteria.equals(EQUALS)) {
			if (data.equals(targetText))
				criteria_met = true;
		}
		else if (criteria.equals(GREATER_THAN)) {
			if (data.compareTo(targetText)>0)
				criteria_met = true;
		}
		else if (criteria.equals(LESS_THAN)) {
			if (data.compareTo(targetText)<0)
				criteria_met = true;
		}
		else if (criteria.equals(VALUED)) {
			if (data.trim().length() > 0)
				criteria_met = true;
		}
		else if (criteria.equals(NOT_VALUED)) {
			if (data.trim().length() == 0)
				criteria_met = true;
		}
		else if (criteria.equals(REG_EX)) {
			Matcher matcher = pattern.matcher(data);
			if (matcher.find())
				criteria_met = true;
		}

		if (criteria_met) {
			// is the data listed on exclusion list?
			if (exclusionSet != null && exclusionSet.contains(data)) {
				return null; // the data is on exclusion list
			}
			else {
				return ruleName;
			}
		}
		else {
			return null;
		}
	}
	
	public String getRuleContent() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.getRuleContent());
		
		if (storedProcedure != null)
			sb.append("Stored Procedure: " + storedProcedure + LF);
		if (exclusionList != null) {
			sb.append("Exclusion List:" + LF);
			for (int i = 0; i < exclusionList.size(); i++) {
				sb.append("     " + exclusionList.get(i) + LF);
			}
		}
		return sb.toString();
	}

	public String match(String mail_type, Object mail_obj) {
		// dummy implementation satisfying the super class
		return null;
	}
}