package jpa.constant;

public enum RuleActionType {
	ACTIVATE("activete email address","activateService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	BOUNCE_UP("increase bounce count","bounceService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	CLOSE("close the message","closeService",null,null),
	CSR_REPLY("send off the reply from csr","csrReplyService",null,null),
	AUTO_REPLY("reply to the message automatically","autoReplyService",null,RuleDataTypeEnum.TEMPLATE_ID),
	MARK_DLVR_ERR("mark delivery error","deliveryErrorService",null,null),
	DROP("drop the message","dropService","jpa.service.DropService", null),
	FORWARD("forward the message","forwardService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	TO_CSR("redirect to message queue","toCsrService",null,RuleDataTypeEnum.QUEUE_NAME),
	SAVE("save the message","saveService",null,null),
	SENDMAIL("simply send the mail off","sendMailService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SUSPEND("suspend email address","suspendService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	UNSUBSCRIBE("remove from the mailing list","unsubscribeService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SUBSCRIBE("subscribe to the mailing list","subscribeService",null,RuleDataTypeEnum.EMAIL_ADDRESS),
	ASSIGN_RULENAME("set a rule mame and re-queue","assignRuleNameService",null,RuleDataTypeEnum.RULE_NAME),
	OPEN("open the message","openService",null,null);

	private String description;
	private String serviceName;
	private String className;
	private RuleDataTypeEnum dataType;

	private RuleActionType(String description, String serviceName,
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
