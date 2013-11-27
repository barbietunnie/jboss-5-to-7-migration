package com.es.core.util;

import java.util.HashMap;
import java.util.Map;

import com.es.bo.render.RenderVariable;
import com.es.data.constant.CodeType;
import com.es.data.constant.VariableName;
import com.es.data.constant.VariableType;
import com.es.vo.address.MailingListVo;

public final class MailingListUtil {

	private MailingListUtil() {
		// static only
	}
	
	public static Map<String, RenderVariable> renderListVariables(MailingListVo listVo,
			String subscriberAddress, long subscriberAddressId) {
		Map<String, RenderVariable> variables = new HashMap<String, RenderVariable>();
		String varName = null;
		RenderVariable var = null;
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListId.toString();
		var = new RenderVariable(
				varName,
				listVo.getListId(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				CodeType.NO_CODE.getValue(),
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListName.toString();
		var = new RenderVariable(
				varName,
				listVo.getDisplayName(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				CodeType.NO_CODE.getValue(),
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListAddress.toString();
		var = new RenderVariable(
				varName,
				listVo.getEmailAddr(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				CodeType.NO_CODE.getValue(),
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddress.toString();
		var = new RenderVariable(
				varName,
				subscriberAddress,
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				CodeType.NO_CODE.getValue(),
				null);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddressId.toString();
		var = new RenderVariable(
				varName,
				String.valueOf(subscriberAddressId),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				CodeType.NO_CODE.getValue(),
				null);
		variables.put(varName, var);
		
		return variables;
	}
}