package jpa.data.preload;

import jpa.constant.EmailAddrType;
import jpa.constant.TableColumnName;

public enum RuleActionEnum {
	act1(RuleNameEnum.HARD_BOUNCE,1,RuleActionDetailEnum.SAVE,null),
	act2(RuleNameEnum.HARD_BOUNCE,2,RuleActionDetailEnum.SUSPEND,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	act3(RuleNameEnum.HARD_BOUNCE,2,RuleActionDetailEnum.MARK_DLVR_ERR,null),
	act4(RuleNameEnum.HARD_BOUNCE,1,RuleActionDetailEnum.CLOSE,null),
	act5(RuleNameEnum.SOFT_BOUNCE,1,RuleActionDetailEnum.SAVE,null),
	act6(RuleNameEnum.SOFT_BOUNCE,2,RuleActionDetailEnum.BOUNCE_UP,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	act7(RuleNameEnum.SOFT_BOUNCE,3,RuleActionDetailEnum.CLOSE,null),
	act8(RuleNameEnum.MAILBOX_FULL,1,RuleActionDetailEnum.SAVE,null),
	act9(RuleNameEnum.MAILBOX_FULL,2,RuleActionDetailEnum.BOUNCE_UP,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	act10(RuleNameEnum.MAILBOX_FULL,3,RuleActionDetailEnum.CLOSE,null),
	act11(RuleNameEnum.SIZE_TOO_LARGE,1,RuleActionDetailEnum.SAVE,null),
	act12(RuleNameEnum.SIZE_TOO_LARGE,2,RuleActionDetailEnum.TO_CSR,"$" + QueueNameEnum.CUSTOMER_CARE_INPUT.name()),
	act13(RuleNameEnum.MAIL_BLOCK,1,RuleActionDetailEnum.SAVE,null),
	act14(RuleNameEnum.MAIL_BLOCK,2,RuleActionDetailEnum.FORWARD,"$" + TableColumnName.SPAM_CONTROL_ADDR.getValue()),
	act15(RuleNameEnum.SPAM_BLOCK,1,RuleActionDetailEnum.SAVE,null),
	act16(RuleNameEnum.SPAM_BLOCK,2,RuleActionDetailEnum.FORWARD,"$"+TableColumnName.SPAM_CONTROL_ADDR),
	act17(RuleNameEnum.VIRUS_BLOCK,1,RuleActionDetailEnum.SAVE,null),
	act18(RuleNameEnum.VIRUS_BLOCK,2,RuleActionDetailEnum.FORWARD,"$"+TableColumnName.VIRUS_CONTROL_ADDR),
	act19(RuleNameEnum.CHALLENGE_RESPONSE,1,RuleActionDetailEnum.FORWARD,"$"+TableColumnName.CHALLENGE_HANDLER_ADDR),
	act20(RuleNameEnum.AUTO_REPLY,1,RuleActionDetailEnum.SAVE,null),
	act21(RuleNameEnum.AUTO_REPLY,2,RuleActionDetailEnum.CLOSE,null),
	act22(RuleNameEnum.CC_USER,1,RuleActionDetailEnum.DROP,null),
	act23(RuleNameEnum.MDN_RECEIPT,1,RuleActionDetailEnum.ACTIVATE,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	act24(RuleNameEnum.MDN_RECEIPT,2,RuleActionDetailEnum.DROP,null),
	act25(RuleNameEnum.CSR_REPLY,1,RuleActionDetailEnum.CSR_REPLY,null),
	act26(RuleNameEnum.SEND_MAIL,1,RuleActionDetailEnum.SENDMAIL,"$"+EmailAddrType.TO_ADDR.getValue()),
	act27(RuleNameEnum.RMA_REQUEST,1,RuleActionDetailEnum.SAVE,null),
	act28(RuleNameEnum.RMA_REQUEST,2,RuleActionDetailEnum.ACTIVATE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	act29(RuleNameEnum.RMA_REQUEST,3,RuleActionDetailEnum.TO_CSR,"$" + QueueNameEnum.RMA_REQUEST_INPUT.name()),
	act30(RuleNameEnum.UNSUBSCRIBE,1,RuleActionDetailEnum.SAVE,null),
	act31(RuleNameEnum.UNSUBSCRIBE,2,RuleActionDetailEnum.UNSUBSCRIBE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	act32(RuleNameEnum.UNSUBSCRIBE,3,RuleActionDetailEnum.CLOSE,null),
	act33(RuleNameEnum.SUBSCRIBE,1,RuleActionDetailEnum.SAVE,null),
	act34(RuleNameEnum.SUBSCRIBE,2,RuleActionDetailEnum.SUBSCRIBE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	act35(RuleNameEnum.SUBSCRIBE,3,RuleActionDetailEnum.ACTIVATE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	act36(RuleNameEnum.SUBSCRIBE,4,RuleActionDetailEnum.AUTO_REPLY,EmailTemplateEnum.SubscribeByEmailReply.name()),
	act37(RuleNameEnum.SUBSCRIBE,5,RuleActionDetailEnum.CLOSE,null),
	act38(RuleNameEnum.BROADCAST,1,RuleActionDetailEnum.SAVE,null),
	act39(RuleNameEnum.BROADCAST,2,RuleActionDetailEnum.BROADCAST,null),
	act40(RuleNameEnum.BROADCAST,3,RuleActionDetailEnum.CLOSE,null),
	act41(RuleNameEnum.GENERIC,1,RuleActionDetailEnum.SAVE,null),
	act42(RuleNameEnum.GENERIC,2,RuleActionDetailEnum.ACTIVATE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	act43(RuleNameEnum.GENERIC,4,RuleActionDetailEnum.TO_CSR,"$"+QueueNameEnum.CUSTOMER_CARE_INPUT),
	act44(RuleNameEnum.UNATTENDED_MAILBOX,1,RuleActionDetailEnum.DROP,null),
	act45(RuleNameEnum.OUF_OF_OFFICE_AUTO_REPLY,1,RuleActionDetailEnum.SAVE,null),
	act46(RuleNameEnum.OUF_OF_OFFICE_AUTO_REPLY,2,RuleActionDetailEnum.CLOSE,null),
	act47(RuleNameEnum.CONTACT_US,1,RuleActionDetailEnum.SAVE,null),
	act48(RuleNameEnum.XHEADER_SPAM_SCORE,1,RuleActionDetailEnum.SAVE,null),
	act49(RuleNameEnum.XHEADER_SPAM_SCORE,2,RuleActionDetailEnum.CLOSE,null),
	act50(RuleNameEnum.EXECUTABLE_ATTACHMENT,1,RuleActionDetailEnum.DROP,null),
	act51(RuleNameEnum.HARD_BOUNCE_WATCHED_MAILBOX,1,RuleActionDetailEnum.SAVE,null),
	act52(RuleNameEnum.HARD_BOUNCE_WATCHED_MAILBOX,2,RuleActionDetailEnum.OPEN,null),
	act53(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,1,RuleActionDetailEnum.SAVE,null),
	act54(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,2,RuleActionDetailEnum.OPEN,null);
	
	private RuleNameEnum ruleName;
	private int sequence;
	private RuleActionDetailEnum actionDetail;
	private String fieldValues;
	private RuleActionEnum(RuleNameEnum ruleName, int sequence,
			RuleActionDetailEnum actionDetail, String fieldValues) {
		this.ruleName = ruleName;
		this.sequence = sequence;
		this.actionDetail = actionDetail;
		this.fieldValues = fieldValues;
	}
	public RuleNameEnum getRuleName() {
		return ruleName;
	}
	public int getSequence() {
		return sequence;
	}
	public RuleActionDetailEnum getActionDetail() {
		return actionDetail;
	}
	public String getFieldValues() {
		return fieldValues;
	}
}
