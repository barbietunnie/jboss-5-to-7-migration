package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.EmailAddrType;
import jpa.constant.RuleNameType;
import jpa.constant.TableColumnName;
import jpa.model.RuleAction;
import jpa.model.RuleActionDetail;
import jpa.model.RuleDataType;
import jpa.model.RuleDataValue;
import jpa.model.RuleDataValuePK;
import jpa.model.RuleLogic;
import jpa.service.RuleActionDetailService;
import jpa.service.RuleActionService;
import jpa.service.RuleDataTypeService;
import jpa.service.RuleDataValueService;
import jpa.service.RuleLogicService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class RuleActionLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(RuleActionLoader.class);
	private RuleDataTypeService typeService;
	private RuleDataValueService valueService;
	private RuleActionDetailService detailService;
	private RuleActionService actionService;
	private RuleLogicService logicService;

	public static void main(String[] args) {
		RuleActionLoader loader = new RuleActionLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		typeService = (RuleDataTypeService) SpringUtil.getAppContext().getBean("ruleDataTypeService");
		valueService = (RuleDataValueService) SpringUtil.getAppContext().getBean("ruleDataValueService");
		detailService = (RuleActionDetailService) SpringUtil.getAppContext().getBean("ruleActionDetailService");
		actionService = (RuleActionService) SpringUtil.getAppContext().getBean("ruleActionService");
		logicService = (RuleLogicService) SpringUtil.getAppContext().getBean("ruleLogicService");
		startTransaction();
		try {
			loadRuleDataTypeAndValues();
			loadRuleActionDetails();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
		startTransaction();
		try {
			loadRuleActions();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	void loadRuleDataTypeAndValues() throws SQLException {
		String jndiProperties = 
				"java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" + LF +
				"java.naming.provider.url=jnp:////localhost:2099" + LF +
				"java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces";

		RuleDataType tp = new RuleDataType("EMAIL_ADDRESS", "Email Address");
		typeService.insert(tp);
		RuleDataValuePK pk1;
		RuleDataValue data = null;
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.FROM_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.TO_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.CC_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.BCC_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.FINAL_RCPT_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.ORIG_RCPT_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + EmailAddrType.FORWARD_ADDR.getValue());
		data = new RuleDataValue(pk1, "MessageBean");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + TableColumnName.SECURITY_DEPT_ADDR);
		data = new RuleDataValue(pk1, "clientDao");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + TableColumnName.CUSTOMER_CARE_ADDR);
		data = new RuleDataValue(pk1, "clientDao");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + TableColumnName.RMA_DEPT_ADDR);
		data = new RuleDataValue(pk1, "clientDao");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + TableColumnName.VIRUS_CONTROL_ADDR);
		data = new RuleDataValue(pk1, "clientDao");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + TableColumnName.SPAM_CONTROL_ADDR);
		data = new RuleDataValue(pk1, "clientDao");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$" + TableColumnName.CHALLENGE_HANDLER_ADDR);
		data = new RuleDataValue(pk1, "clientDao");
		valueService.insert(data);

		tp = new RuleDataType("QUEUE_NAME", "queue name");
		typeService.insert(tp);
		pk1 = new RuleDataValuePK(tp, "$RMA_REQUEST_INPUT");
		data = new RuleDataValue(pk1, "rmaRequestInputJmsTemplate");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$CUSTOMER_CARE_INPUT");
		data = new RuleDataValue(pk1, "customerCareInputJmsTemplate");
		valueService.insert(data);

		tp = new RuleDataType(RuleDataValue.TEMPLATE_ID, "template id");
		typeService.insert(tp);
		pk1 = new RuleDataValuePK(tp, "SubscribeByEmailReply");
		data = new RuleDataValue(pk1, jndiProperties);
		valueService.insert(data);
		
		// insert rule names
		tp = new RuleDataType("RULE_NAME", "Rule Name");
		typeService.insert(tp);
		for (RuleNameType name : RuleNameType.values()) {
			if (RuleNameType.GENERIC.equals(name)) {
				continue; // skip GENERIC
			}
			String ruleName = name.getValue();
			pk1 = new RuleDataValuePK(tp, ruleName);
			data = new RuleDataValue(pk1, null);
			valueService.insert(data);
		}

		logger.info("EntityManager persisted the record.");
	}
	
	void loadRuleActionDetails() {
		RuleDataType tp1 = typeService.getByDataType("EMAIL_ADDRESS");
		RuleActionDetail act = null;
		act = new RuleActionDetail(tp1, "ACTIVATE","activete email address","activateBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(tp1, "BOUNCE++","increase bounce count","bounceBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "CLOSE","close the message","closeBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "CSR_REPLY","send off the reply from csr","csrReplyBo",null);
		detailService.insert(act);
		RuleDataType tp2 = typeService.getByDataType(RuleDataValue.TEMPLATE_ID);
		act = new RuleActionDetail(tp2, "AUTO_REPLY","reply to the message automatically","autoReplyBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "MARK_DLVR_ERR","mark delivery error","deliveryErrorBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "DROP","drop the message","dropBo","com.legacytojava.message.bo.DropBoImpl");
		detailService.insert(act);
		act = new RuleActionDetail(tp1, "FORWARD","forward the message","forwardBo",null);
		detailService.insert(act);
		RuleDataType tp3 = typeService.getByDataType("QUEUE_NAME");
		act = new RuleActionDetail(tp3, "TO_CSR","redirect to message queue","toCsrBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "SAVE","save the message","saveBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(tp1, "SENDMAIL","simply send the mail off","sendMailBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(tp1, "SUSPEND","suspend email address","suspendBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(tp1, "UNSUBSCRIBE","remove from the mailing list","unsubscribeBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(tp1, "SUBSCRIBE","subscribe to the mailing list","subscribeBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "BROADCAST","broadcast to a mailing list","broadcastBo",null);
		detailService.insert(act);
		RuleDataType tp4 = typeService.getByDataType("RULE_NAME");
		act = new RuleActionDetail(tp4, "ASSIGN_RULENAME","set a rule mame and re-queue","assignRuleNameBo",null);
		detailService.insert(act);
		act = new RuleActionDetail(null, "OPEN","open the message","openBo",null);
		detailService.insert(act);
		
		logger.info("EntityManager persisted the record.");
	}
	
	void loadRuleActions() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		RuleAction act = null;
		// for build-in rules
		RuleLogic logic = logicService.getByRuleName(RuleNameType.HARD_BOUNCE.getValue());
		RuleActionDetail dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("SUSPEND");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("MARK_DLVR_ERR");
		act = new RuleAction(logic,3,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,4,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.SOFT_BOUNCE.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("BOUNCE++");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,3,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.MAILBOX_FULL.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("BOUNCE++");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,3,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.SIZE_TOO_LARGE.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("TO_CSR");
		act = new RuleAction(logic,2,now,null,dtl,"$CUSTOMER_CARE_INPUT");
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.MAIL_BLOCK.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("FORWARD");
		act = new RuleAction(logic,2,now,null,dtl,"$" + TableColumnName.SPAM_CONTROL_ADDR);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.SPAM_BLOCK.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("FORWARD");
		act = new RuleAction(logic,2,now,null,dtl,"$"+TableColumnName.SPAM_CONTROL_ADDR);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.VIRUS_BLOCK.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("FORWARD");
		act = new RuleAction(logic,2,now,null,dtl,"$"+TableColumnName.VIRUS_CONTROL_ADDR);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.CHALLENGE_RESPONSE.getValue());
		dtl = detailService.getByActionId("FORWARD");
		act = new RuleAction(logic,1,now,null,dtl,"$"+TableColumnName.CHALLENGE_HANDLER_ADDR);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.AUTO_REPLY.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.CC_USER.getValue());
		dtl = detailService.getByActionId("DROP");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.MDN_RECEIPT.getValue());
		dtl = detailService.getByActionId("ACTIVATE");
		act = new RuleAction(logic,1,now,null,dtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("DROP");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.CSR_REPLY.getValue());
		dtl = detailService.getByActionId("CSR_REPLY");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.SEND_MAIL.getValue());
		dtl = detailService.getByActionId("SENDMAIL");
		act = new RuleAction(logic,1,now,null,dtl,"$"+EmailAddrType.TO_ADDR.getValue());
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.RMA_REQUEST.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("ACTIVATE");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("TO_CSR");
		act = new RuleAction(logic,3,now,null,dtl,"$RMA_REQUEST_INPUT");
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.UNSUBSCRIBE.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("UNSUBSCRIBE");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,3,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.SUBSCRIBE.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("SUBSCRIBE");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("ACTIVATE");
		act = new RuleAction(logic,3,now,null,dtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("AUTO_REPLY");
		act = new RuleAction(logic,4,now,null,dtl,"SubscribeByEmailReply");
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,5,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.BROADCAST.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("BROADCAST");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,3,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName(RuleNameType.GENERIC.getValue());
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("ACTIVATE");
		act = new RuleAction(logic,2,now,null,dtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(act);

		dtl = detailService.getByActionId("TO_CSR");
		act = new RuleAction(logic,3,now,null,dtl,"$CUSTOMER_CARE_INPUT");
		actionService.insert(act);
		//act = new RuleAction(Constants.RULENAME.UNIDENTIFIED.getValue(),1,now,"JBatchCorp","SAVE","A",null);
		//actionService.insert(act);
		//act = new RuleAction(Constants.RULENAME.UNIDENTIFIED.getValue(),2,now,"JBatchCorp","FORWARD","A","$"+Constants.CUSTOMER_CARE_ADDR);
		//actionService.insert(act);
		
		// for custom rules
		logic = logicService.getByRuleName("Unattended_Mailbox");
		dtl = detailService.getByActionId("DROP");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName("OutOfOffice_AutoReply");
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName("Contact_Us");
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName("XHeader_SpamScore");
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("CLOSE");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName("Executable_Attachment");
		dtl = detailService.getByActionId("DROP");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName("HardBouce_WatchedMailbox");
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("OPEN");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		logic = logicService.getByRuleName("HardBounce_NoFinalRcpt");
		dtl = detailService.getByActionId("SAVE");
		act = new RuleAction(logic,1,now,null,dtl,null);
		actionService.insert(act);

		dtl = detailService.getByActionId("OPEN");
		act = new RuleAction(logic,2,now,null,dtl,null);
		actionService.insert(act);

		logger.info("EntityManager persisted the record.");
	}
}

