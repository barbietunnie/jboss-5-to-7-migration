package com.es.bo.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.es.core.util.EmailAddrUtil;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.Constants;
import com.es.data.constant.VariableName;
import com.es.msgbean.BodypartBean;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageBeanBuilder;
import com.es.msgbean.MessageBeanUtil;
import com.es.msgbean.MessageNode;
import com.es.msgbean.MsgHeader;

public class RuleMatcher implements java.io.Serializable {
	private static final long serialVersionUID = 5389476995961087231L;
	static final Logger logger = Logger.getLogger(RuleMatcher.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	public RuleMatcher() {
		// empty
	}
	
	/**
	 * search mail object for fields that are associated with rules, and perform
	 * rule matching logic against the values of these fields.
	 * 
	 * @param mailObj -
	 *            mail object
	 * @param rule_set -
	 *            main rules
	 * @param subRuleSet -
	 *            sub-rules
	 * @return a rule name if matched
	 */
	public String match(Object mailObj, List<RuleBase> rule_set,
			Map<String, List<RuleBase>> subRuleSet) {
		String ruleName = null;

		for (int i = 0; i < rule_set.size(); i++) {
			Object obj = rule_set.get(i);
			RuleBase r = null;
			if (obj instanceof RuleBase) {
				r = (RuleBase) obj;
			}
			if (r == null) {
				throw new RuntimeException("Internal Error, Rule instance is null");
			}
			String data = null;
			if (mailObj instanceof MessageBean) { // just for safety
				String mailType = Constants.SMTP_MAIL;
				if (CarrierCode.WEBMAIL.equals(((MessageBean)mailObj).getCarrierCode())) {
					mailType = Constants.WEB_MAIL;
				}
				if (r instanceof RuleComplex) {
					ruleName = r.match(mailType, mailObj);
				}
				else {
					data = getFieldData(r, (MessageBean) mailObj);
					ruleName = r.match(mailType, r.getDataName(), data);
				}
				// now check attachment rules
				if (ruleName == null 
						&& (RuleBase.MIME_TYPE.equals(r.getDataName()) 
							|| RuleBase.FILE_NAME.equals(r.getDataName()))) {
					ruleName = matchMimeTypes((MessageBean)mailObj, r);
				}
			}
			else {
				// other object types, not implemented yet.
			}
			if (ruleName != null) { // matched
				logger.info("$$$ Rule matched (name/type/data): " + r.getRuleName() + "/"
						+ r.getDataName() + "/" + data + ", RuleName: " + ruleName);
			}
			/*
			 * sub-rule can be used to evaluate: (A or B) and (C or D)
			 * 1) create a main-rule that evaluates A or B (a "Any" rule)
			 * 2) create a sub-rule that evaluates C or D (a "Any" rule)
			 * 3) link the sub-rule created in step 1 to the main-rule
			 * 4) returns a rule name only if one of the sub-rules is matched
			 */
			// now match sub-rules
			if (ruleName != null && r.getSubRules().size() > 0 && subRuleSet != null) {
				if (isDebugEnabled) {
					logger.debug("BEGIN - SubRules");
				}
				String subRuleName = matchSubRules(mailObj, r.getSubRules(), subRuleSet);
				if (isDebugEnabled) {
					logger.debug("END - SubRules, sub-rule ruleName: " + subRuleName);
				}
				if (subRuleName == null) {
					ruleName = null; // did not match sub-rules, reset rule name
				}
			}
			if (ruleName != null) {
				break;
			}
		}
		return ruleName;
	}

	private String matchSubRules(Object mailObj, List<String> ruleNames,
			Map<String, List<RuleBase>> subRuleSet) {
		List<RuleBase> subRules = new ArrayList<RuleBase>();
		for (int i = 0; i < ruleNames.size(); i++) {
			String ruleName = (String)ruleNames.get(i);
			List<RuleBase> rules = subRuleSet.get(ruleName);
			subRules.addAll(rules);
		}
		
		return match(mailObj, subRules, null);
	}

	static String getFieldData(RuleBase r, MessageBean mobj) {
		String data_name = r.getDataName();
		String data = null;
		if (VariableName.XHEADER_DATA_NAME.getValue().equals(data_name)) {
			String headerName = r.getHeaderName();
			if (headerName != null) {
				List<MsgHeader> headers = mobj.getHeaders();
				for (int i = 0; headers != null && i < headers.size(); i++) {
					String name = headers.get(i).getName();
					if (headerName.equalsIgnoreCase(name)) {
						data = headers.get(i).getValue();
						break;
					}
				}
			}
		}
		else if (RuleBase.FILE_NAME.equals(data_name)) {
			// ignored
		}
		else {
			data = MessageBeanUtil.invokeMethod(mobj, data_name);
		}
		return data;
	}

	/* @deprecated, replaced by above method using reflection */
	static String getFieldDataV0(RuleBase r, MessageBean mobj) {
		String data = null;
		String data_type = r.getDataName();
		if (RuleBase.FROM_ADDR.equals(data_type))
			data = EmailAddrUtil.addressToString(mobj.getFrom());
		else if (RuleBase.TO_ADDR.equals(data_type))
			data = EmailAddrUtil.addressToString(mobj.getTo());
		else if (RuleBase.REPLYTO_ADDR.equals(data_type))
			data = EmailAddrUtil.addressToString(mobj.getReplyto());
		else if (RuleBase.CC_ADDR.equals(data_type))
			data = EmailAddrUtil.addressToString(mobj.getCc());
		else if (RuleBase.BCC_ADDR.equals(data_type))
			data = EmailAddrUtil.addressToString(mobj.getBcc());
		else if (RuleBase.SUBJECT.equals(data_type))
			data = mobj.getSubject();
		else if (RuleBase.BODY.equals(data_type))
			data = mobj.getBody();
		else if (RuleBase.MSG_REF_ID.equals(data_type))
			data = mobj.getMsgRefId() == null ? null : mobj.getMsgRefId().toString();
		else if (RuleBase.MAILBOX_USER.equals(data_type))
			data = mobj.getMailboxUser();
		else if (RuleBase.MAILBOX_HOST.equals(data_type))
			data = mobj.getMailboxHost();
		else if (RuleBase.RULE_NAME.equals(data_type)) {
			data = mobj.getRuleName();
		}
		return data;
	}
	
	static String matchMimeTypes(MessageBean mailObj, RuleBase r) {
		String ruleName = null;
		List<MessageNode> nodes = ((MessageBean) mailObj).getAttachments();
		if (nodes != null && nodes.size() > 0) {
			for (Iterator<MessageNode> it=nodes.iterator(); it.hasNext(); ) {
				String data = null;
				BodypartBean anode = it.next().getBodypartNode();
				if (RuleBase.MIME_TYPE.equals(r.getDataName())) {
					data = anode.getMimeType();
				}
				else if (RuleBase.FILE_NAME.equals(r.getDataName())) {
					data = anode.getDescription();
					// if file name is not present, extract it from
					// content type
					if (data == null) {
						String ctype = anode.getContentType();
						try {
							data = MessageBeanBuilder.getFileName(ctype);
						}
						catch (Exception e) {
							logger.error("ERROR!!! - caught unchecked Exception.", e);
						}
					}
				}
				if (r instanceof RuleSimple) {
					ruleName = r.match(Constants.SMTP_MAIL, r.getDataName(), data);
				}
				//else
				//	ruleName = r.match(Constants.SMTP_MAIL, mailObj); // why this?
				
				if (ruleName != null) {
					break;
				}
			}
		}
		return ruleName;
	}
}
