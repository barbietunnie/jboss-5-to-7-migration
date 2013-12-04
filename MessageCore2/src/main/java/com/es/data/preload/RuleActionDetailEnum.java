package com.es.data.preload;

/*
 * define rule actions
 */
public enum RuleActionDetailEnum {
	ACTIVATE("activete email address","activateAddress",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	BOUNCE_UP("increase bounce count","bounceUpAddress",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	CLOSE("close the message","closeMessage",null,null),
	CSR_REPLY("send off the reply from csr","csrReplyMessage",null,null),
	AUTO_REPLY("reply to the message automatically","autoReplyMessage",null,RuleDataTypeEnum.TEMPLATE_ID),
	MARK_DLVR_ERR("mark delivery error","deliveryError",null,null),
	DROP("drop the message","dropMessage","com.es.bo.task.DropMessage", null),
	FORWARD("forward the message","forwardMessage",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	TO_CSR("redirect to message queue","forwardToCsr",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SAVE("save the message","saveMessage",null,null),
	SENDMAIL("simply send the mail off","sendMessage",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SUSPEND("suspend email address","suspendAddress",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	UNSUBSCRIBE("remove from the mailing list","unsubscribeFromList",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SUBSCRIBE("subscribe to the mailing list","subscribeToList",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	ASSIGN_RULENAME("set a rule mame and re-process","assignRuleName",null,RuleDataTypeEnum.RULE_NAME),
	OPEN("open the message","openMessage",null,null),
	BROADCAST("broadcast to mailing list","broadcastToList",null,RuleDataTypeEnum.MAILING_LIST);

	private String description;
	private String serviceName;
	private String className;
	private RuleDataTypeEnum dataType;

	private RuleActionDetailEnum(String description, String serviceName,
			String className, RuleDataTypeEnum dataType) {
		this.description = description;
		this.serviceName = serviceName;
		this.className = className;
		this.dataType = dataType;
	}

	public String getDescription() {
		return description;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getClassName() {
		return className;
	}

	public RuleDataTypeEnum getDataType() {
		return dataType;
	}

}
