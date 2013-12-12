package com.es.bo.mlist;

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
	
	public static Map<String, RenderVariable> buildRenderVariables(MailingListVo listVo,
			String subscriberAddress, long subscriberAddressId) {
		Map<String, RenderVariable> variables = new HashMap<String, RenderVariable>();
		String varName = null;
		RenderVariable var = null;
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListId.name();
		var = new RenderVariable(
				varName,
				listVo.getListId(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListName.name();
		var = new RenderVariable(
				varName,
				listVo.getDisplayName(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListAddress.name();
		var = new RenderVariable(
				varName,
				listVo.getEmailAddr(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddress.name();
		var = new RenderVariable(
				varName,
				subscriberAddress,
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddressId.name();
		var = new RenderVariable(
				varName,
				String.valueOf(subscriberAddressId),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		return variables;
	}
}
