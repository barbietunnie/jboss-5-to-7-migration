package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.EmailAddrType;
import jpa.constant.RuleActionEnum;
import jpa.constant.RuleDataTypeEnum;
import jpa.constant.RuleNameOther;
import jpa.constant.RuleNameEnum;
import jpa.constant.TableColumnName;
import jpa.model.ClientData;
import jpa.model.RuleAction;
import jpa.model.RuleActionDetail;
import jpa.model.RuleDataType;
import jpa.model.RuleDataValue;
import jpa.model.RuleDataValuePK;
import jpa.model.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.RuleActionDetailService;
import jpa.service.RuleActionService;
import jpa.service.RuleDataTypeService;
import jpa.service.RuleDataValueService;
import jpa.service.RuleLogicService;
import jpa.util.SpringUtil;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;

public class RuleActionLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(RuleActionLoader.class);
	private RuleDataTypeService typeService;
	private RuleDataValueService valueService;
	private RuleActionDetailService detailService;
	private RuleActionService actionService;
	private RuleLogicService logicService;
	private ClientDataService clientService;

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
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
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

		RuleDataType tp = new RuleDataType(RuleDataTypeEnum.EMAIL_ADDRESS.name(), RuleDataTypeEnum.EMAIL_ADDRESS.getDescription());
		typeService.insert(tp);
		RuleDataValuePK pk1;
		RuleDataValue data = null;
		for (EmailAddrType addrType : EmailAddrType.values()) {
			pk1 = new RuleDataValuePK(tp, "$" + addrType.getValue());
			data = new RuleDataValue(pk1, "MessageBean");
			valueService.insert(data);
		}
		
		for (TableColumnName addrColumn : TableColumnName.values()) {
			pk1 = new RuleDataValuePK(tp, "$" + addrColumn.getValue());
			data = new RuleDataValue(pk1, "clientDao");
			valueService.insert(data);
		}

		tp = new RuleDataType(RuleDataTypeEnum.QUEUE_NAME.name(), RuleDataTypeEnum.QUEUE_NAME.getDescription());
		typeService.insert(tp);
		pk1 = new RuleDataValuePK(tp, "$RMA_REQUEST_INPUT");
		data = new RuleDataValue(pk1, "rmaRequestInputJmsTemplate");
		valueService.insert(data);
		pk1 = new RuleDataValuePK(tp, "$CUSTOMER_CARE_INPUT");
		data = new RuleDataValue(pk1, "customerCareInputJmsTemplate");
		valueService.insert(data);

		tp = new RuleDataType(RuleDataTypeEnum.TEMPLATE_ID.name(), RuleDataTypeEnum.TEMPLATE_ID.getDescription());
		typeService.insert(tp);
		pk1 = new RuleDataValuePK(tp, "SubscribeByEmailReply");
		data = new RuleDataValue(pk1, jndiProperties);
		valueService.insert(data);
		
		// TODO
		tp = new RuleDataType(RuleDataTypeEnum.MAILING_LIST.name(), RuleDataTypeEnum.MAILING_LIST.getDescription());
		typeService.insert(tp);
		pk1 = new RuleDataValuePK(tp, "$MAILING_LIST_ID");
		data = new RuleDataValue(pk1, "SMPLLST1");
		valueService.insert(data);

		// insert rule names
		tp = new RuleDataType(RuleDataTypeEnum.RULE_NAME.name(), RuleDataTypeEnum.RULE_NAME.getDescription());
		typeService.insert(tp);
		for (RuleNameEnum name : RuleNameEnum.values()) {
			if (RuleNameEnum.GENERIC.equals(name)) {
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
		for (RuleActionEnum ruleAction : RuleActionEnum.values()) {
			RuleDataType tp1 = null;
			if (ruleAction.getDataType()!=null) {
				tp1 = typeService.getByDataType(ruleAction.getDataType().name());
			}
			RuleActionDetail act = new RuleActionDetail(tp1, ruleAction.name(),
					ruleAction.getDescription(), ruleAction.getServiceName(),
					ruleAction.getClassName());
			detailService.insert(act);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	void loadRuleActions() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		RuleAction action = null;
		// for build-in rules
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		RuleActionDetail actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.SUSPEND.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.MARK_DLVR_ERR.name());
		action = new RuleAction(logic,3,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,4,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.SOFT_BOUNCE.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.BOUNCE_UP.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,3,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.MAILBOX_FULL.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.BOUNCE_UP.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,3,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.SIZE_TOO_LARGE.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.TO_CSR.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$CUSTOMER_CARE_INPUT");
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.MAIL_BLOCK.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.FORWARD.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$" + TableColumnName.SPAM_CONTROL_ADDR);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.SPAM_BLOCK.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.FORWARD.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+TableColumnName.SPAM_CONTROL_ADDR);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.VIRUS_BLOCK.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.FORWARD.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+TableColumnName.VIRUS_CONTROL_ADDR);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.CHALLENGE_RESPONSE.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.FORWARD.name());
		action = new RuleAction(logic,1,now,null,actdtl,"$"+TableColumnName.CHALLENGE_HANDLER_ADDR);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.AUTO_REPLY.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.CC_USER.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.DROP.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.MDN_RECEIPT.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.ACTIVATE.name());
		action = new RuleAction(logic,1,now,null,actdtl,"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.DROP.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.CSR_REPLY.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.CSR_REPLY.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.SEND_MAIL.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SENDMAIL.name());
		action = new RuleAction(logic,1,now,null,actdtl,"$"+EmailAddrType.TO_ADDR.getValue());
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.RMA_REQUEST.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.ACTIVATE.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.TO_CSR.name());
		action = new RuleAction(logic,3,now,null,actdtl,"$RMA_REQUEST_INPUT");
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.UNSUBSCRIBE.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.UNSUBSCRIBE.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,3,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.SUBSCRIBE.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.SUBSCRIBE.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.ACTIVATE.name());
		action = new RuleAction(logic,3,now,null,actdtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.AUTO_REPLY.name());
		action = new RuleAction(logic,4,now,null,actdtl,"SubscribeByEmailReply");
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,5,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.BROADCAST.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.BROADCAST.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,3,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.ACTIVATE.name());
		action = new RuleAction(logic,2,now,null,actdtl,"$"+EmailAddrType.FROM_ADDR.getValue());
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.TO_CSR.name());
		action = new RuleAction(logic,3,now,null,actdtl,"$CUSTOMER_CARE_INPUT");
		actionService.insert(action);
		
		try {
			ClientData client = clientService.getByClientId("JBatchCorp");
			logger.debug("JbatchCorp Client found: " + StringUtil.prettyPrint(client));
		}
		catch (NoResultException e) {}
		/*
		logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		dtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		act = new RuleAction(logic,1,now,client,dtl,null);
		actionService.insert(act);
		dtl = detailService.getByActionId(RuleActionEnum.FORWARD.name());
		act = new RuleAction(logic,2,now,client,dtl,"$"+TableColumnName.CUSTOMER_CARE_ADDR);
		actionService.insert(act);
		 */
		
		// for custom rules
		logic = logicService.getByRuleName(RuleNameOther.UNATTENDED_MAILBOX.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.DROP.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameOther.OUF_OF_OFFICE_AUTO_REPLY.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameOther.CONTACT_US.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameOther.XHEADER_SPAM_SCORE.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.CLOSE.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameOther.EXECUTABLE_ATTACHMENT.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.DROP.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameOther.HARD_BOUNCE_WATCHED_MAILBOX.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.OPEN.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		logic = logicService.getByRuleName(RuleNameOther.HARD_BPUNCE_NO_FINAL_RCPT.getValue());
		actdtl = detailService.getByActionId(RuleActionEnum.SAVE.name());
		action = new RuleAction(logic,1,now,null,actdtl,null);
		actionService.insert(action);

		actdtl = detailService.getByActionId(RuleActionEnum.OPEN.name());
		action = new RuleAction(logic,2,now,null,actdtl,null);
		actionService.insert(action);

		logger.info("EntityManager persisted the record.");
	}
}

