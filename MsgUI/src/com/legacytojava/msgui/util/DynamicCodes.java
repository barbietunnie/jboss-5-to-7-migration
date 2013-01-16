package com.legacytojava.msgui.util;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.springframework.web.context.WebApplicationContext;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.VariableName;
import com.legacytojava.message.dao.action.MsgActionDetailDao;
import com.legacytojava.message.dao.action.MsgDataTypeDao;
import com.legacytojava.message.dao.client.ClientDao;
import com.legacytojava.message.dao.emailaddr.EmailTemplateDao;
import com.legacytojava.message.dao.emailaddr.EmailVariableDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.dao.rule.RuleLogicDao;
import com.legacytojava.message.dao.template.ClientVariableDao;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;
import com.legacytojava.message.vo.emailaddr.EmailVariableVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.message.vo.rule.RuleLogicVo;
import com.legacytojava.message.vo.template.ClientVariableVo;

public class DynamicCodes {
	static WebApplicationContext webContext = null;
	
	private static RuleLogicDao ruleLogicDao = null;
	public static RuleLogicDao getRuleLogicDao() {
		if (ruleLogicDao == null) {
			ruleLogicDao = (RuleLogicDao) SpringUtil.getWebAppContext().getBean("ruleLogicDao");
		}
		return ruleLogicDao;
	}
	
	private static MsgActionDetailDao msgActionDetailDao = null;
	public static MsgActionDetailDao getMsgActionDetailDao() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (MsgActionDetailDao) SpringUtil.getWebAppContext().getBean(
					"msgActionDetailDao");
		}
		return msgActionDetailDao;
	}
	
	private static MsgDataTypeDao msgDataTypeDao = null;
	public static MsgDataTypeDao getMsgDataTypeDao() {
		if (msgDataTypeDao == null) {
			msgDataTypeDao = (MsgDataTypeDao) SpringUtil.getWebAppContext().getBean(
					"msgDataTypeDao");
		}
		return msgDataTypeDao;
	}
	
	private static ClientDao clientDao = null;
	public static ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao) SpringUtil.getWebAppContext().getBean("clientDao");
		}
		return clientDao;
	}
	
	private static MailingListDao mailingListDao = null;
	public static MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext().getBean("mailingListDao");
		}
		return mailingListDao;
	}
	
	private static EmailVariableDao emailVariableDao = null;
	public static EmailVariableDao getEmailVariableDao() {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableDao) SpringUtil.getWebAppContext().getBean("emailVariableDao");
		}
		return emailVariableDao;
	}
	
	private static EmailTemplateDao emailTemplateDao = null;
	public static EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateDao) SpringUtil.getWebAppContext().getBean("emailTemplateDao");
		}
		return emailTemplateDao;
	}
	
	private static ClientVariableDao clientVariableDao = null;
	public static ClientVariableDao getClientVariableDao() {
		if (clientVariableDao == null) {
			clientVariableDao = (ClientVariableDao) SpringUtil.getWebAppContext().getBean("clientVariableDao");
		}
		return clientVariableDao;
	}
	
	// PROPERTY: SubRule Items 
	public SelectItem[] getSubRuleItems() {
		List<RuleLogicVo> list = getRuleLogicDao().getAllSubRules(true);
		SelectItem[] subRules = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			subRules[i] = new SelectItem(list.get(i).getRuleName());
		}
		return subRules;
	}
	
	// PROPERTY: Rule Name Items 
	public SelectItem[] getBuiltinRuleNameItems() {
		List<String> list = getRuleLogicDao().getBuiltinRuleNames4Web();
		SelectItem[] ruleNames = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			ruleNames[i] = new SelectItem(list.get(i));
		}
		return ruleNames;
	}
	
	// PROPERTY: Custom Rule Name Items 
	public SelectItem[] getCustomRuleNameItems() {
		List<String> list = getRuleLogicDao().getCustomRuleNames4Web();
		SelectItem[] ruleNames = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			ruleNames[i] = new SelectItem(list.get(i));
		}
		return ruleNames;
	}
	
	// PROPERTY: ActionId Items 
	public SelectItem[] getActionIdItems() {
		List<String> list = getMsgActionDetailDao().getActionIds();
		//list.add("-- New Action");
		SelectItem[] actionIds = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			actionIds[i] = new SelectItem(list.get(i));
		}
		return actionIds;
	}
	
	// PROPERTY: DataType Items 
	public SelectItem[] getMsgDataTypeItems() {
		List<String> list = getMsgDataTypeDao().getDataTypes();
		SelectItem[] dataTypes = new SelectItem[list.size() + 1];
		dataTypes[0] = new SelectItem(null, "");
		for (int i=0; i<list.size(); i++) {
			dataTypes[i + 1] = new SelectItem(list.get(i), list.get(i));
		}
		return dataTypes;
	}

	// PROPERTY: ClientId Items 
	public SelectItem[] getClientIdItems() {
		List<ClientVo> list = getClientDao().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String clientId = list.get(i).getClientId();
			dataTypes[i] = new SelectItem(clientId, clientId);
		}
		return dataTypes;
	}
	
	// PROPERTY: Mailing List listId Items 
	public SelectItem[] getMailingListIdItems() {
		List<MailingListVo> list = getMailingListDao().getAll(false);
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String listId = list.get(i).getListId();
			String display = listId + " - " + list.get(i).getEmailAddr();
			dataTypes[i] = new SelectItem(listId, display);
		}
		return dataTypes;
	}
	
	// PROPERTY: Email Variable Name Items 
	public SelectItem[] getEmailVariableNameItems() {
		// 1) custom variables
		List<EmailVariableVo> list = getEmailVariableDao().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		// custom variable names
		for (int i=0; i<list.size(); i++) {
			String variableName = list.get(i).getVariableName();
			dataTypes[i] = new SelectItem(variableName);
		}
		return dataTypes;
	}
	
	// PROPERTY: Global Email Variable Name Items 
	public SelectItem[] getGlobalVariableNameItems() {
		// 2) Mailing List built-in variables
		VariableName.LIST_VARIABLE_NAME[] listVarNames = VariableName.LIST_VARIABLE_NAME.values();
		// 3) Client built-in variables
		List<ClientVariableVo> clientVars = getClientVariableDao().getCurrentByClientId(
				Constants.DEFAULT_CLIENTID);
		List<String> clientVarNames = new ArrayList<String>();
		for (ClientVariableVo clientVar : clientVars) {
			if (!"ClientId".equalsIgnoreCase(clientVar.getVariableName())) {
				clientVarNames.add(clientVar.getVariableName());
			}
		}
		SelectItem[] dataTypes = new SelectItem[listVarNames.length
				+ clientVarNames.size()];
		// include mailing list variable names
		for (int i = 0; i < listVarNames.length; i++) {
			String variableName = listVarNames[i].toString();
			dataTypes[i] = new SelectItem(variableName);
		}
		// include client variable names
		for (int i = 0; i < clientVarNames.size(); i++) {
			String variableName = clientVarNames.get(i);
			dataTypes[i + listVarNames.length] = new SelectItem(variableName);
		}
		return dataTypes;
	}
	
	// PROPERTY: Email Template Id Items 
	public SelectItem[] getEmailTemplateIdItems() {
		List<EmailTemplateVo> list = getEmailTemplateDao().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String templateId = list.get(i).getTemplateId();
			dataTypes[i] = new SelectItem(templateId);
		}
		return dataTypes;
	}
}
