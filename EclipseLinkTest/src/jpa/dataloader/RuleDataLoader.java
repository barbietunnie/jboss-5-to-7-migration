package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.RuleCategory;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleDataName;
import jpa.constant.RuleNameCustom;
import jpa.constant.RuleNameBuiltin;
import jpa.constant.RuleNameSubrule;
import jpa.constant.StatusId;
import jpa.constant.XHeaderName;
import jpa.model.RuleElement;
import jpa.model.RuleElementPK;
import jpa.model.RuleLogic;
import jpa.model.RuleSubruleMap;
import jpa.model.RuleSubruleMapPK;
import jpa.service.RuleElementService;
import jpa.service.RuleLogicService;
import jpa.service.RuleSubruleMapService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class RuleDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(RuleDataLoader.class);
	private RuleLogicService service;
	private RuleElementService elementService;
	private RuleSubruleMapService mapService;

	public static void main(String[] args) {
		RuleDataLoader loader = new RuleDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (RuleLogicService) SpringUtil.getAppContext().getBean("ruleLogicService");
		elementService = (RuleElementService) SpringUtil.getAppContext().getBean("ruleElementService");
		mapService = (RuleSubruleMapService) SpringUtil.getAppContext().getBean("ruleSubruleMapService");
		startTransaction();
		try {
			loadBuiltInRules();
			loadCustomAndSubRules();
			//loadSubRules();
			loadRuleElements();
			loadRuleSubruleMaps();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadBuiltInRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// built-in rules
		int ruleSeq = 100;
		for (RuleNameBuiltin ruleName : RuleNameBuiltin.values()) {
			RuleLogic data = new RuleLogic();
			data.setRuleName(ruleName.getValue());
			data.setEvalSequence(++ruleSeq);
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(RuleCategory.MAIN_RULE.getValue());
			data.setSubrule(false);
			data.setBuiltinRule(true);
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		// end of built-in rules

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadCustomAndSubRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// Custom Rules
		int ruleSeq = 200;
		for (RuleNameCustom ruleName : RuleNameCustom.values()) {
			RuleLogic data = new RuleLogic();
			data.setRuleName(ruleName.getValue());
			if (RuleNameCustom.UNATTENDED_MAILBOX.equals(ruleName)) {
				data.setEvalSequence(0);
			}
			else {
				data.setEvalSequence(++ruleSeq);
			}
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(ruleName.getRuleCategory().getValue());
			data.setSubrule(false);
			data.setBuiltinRule(false);
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		// Built-in Sub Rules
		ruleSeq = 225;
		for (RuleNameSubrule ruleName : RuleNameSubrule.values()) {
			RuleLogic data = new RuleLogic();
			data.setRuleName(ruleName.getValue());
			if (RuleNameCustom.UNATTENDED_MAILBOX.equals(ruleName)) {
				data.setEvalSequence(0);
			}
			else {
				data.setEvalSequence(++ruleSeq);
			}
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(ruleName.getRuleCategory().getValue());
			data.setSubrule(true);
			data.setBuiltinRule(true);
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}

	
	private void loadRuleElements() {
		RuleLogic obj = service.getByRuleName(RuleNameCustom.UNATTENDED_MAILBOX.getValue());
		RuleElementPK pk = null;
		
		RuleElement data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.MAILBOX_USER.getValue());
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("noreply");
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.RETURN_PATH.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^<?.+@.+>?$"); // make sure the return path is not blank or <>
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.HARD_BOUNCE.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:postmaster|mailmaster|mailadmin|administrator)\\S*\\@");
		data.setExclusions("postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME);
		data.setExclListProcName("excludingPostmastersService");
		data.setDelimiter(",");
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@");
		data.setExclusions("mailer-daemon@legacytojave.com,mailer-daemon@" + Constants.VENDER_DOMAIN_NAME);
		data.setExclListProcName(null);
		data.setDelimiter(",");
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.MAILBOX_FULL.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:postmaster|mailmaster|mailadmin|administrator" +
				"|mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@");
		data.setExclusions("postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME);
		data.setExclListProcName(null);
		data.setDelimiter(",");
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.SPAM_BLOCK.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^Spam rapport \\/ Spam report \\S+ -\\s+\\(\\S+\\)$" +
				"|^GWAVA Sender Notification .(?:RBL block|Spam|Content filter).$" +
				"|^\\[MailServer Notification\\]" +
				"|^MailMarshal has detected possible spam in your message");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("EarthLink\\b.*(?:spamBlocker|spamArrest)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,2);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:^surfcontrol|.*You_Got_Spammed)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,3);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.X_HEADER.getValue());
		data.setHeaderName(XHeaderName.RETURN_PATH.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:pleaseforward|quotaagent)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,4);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.X_HEADER.getValue());
		data.setHeaderName("Precedence");
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:spam)$");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.CHALLENGE_RESPONSE.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.X_HEADER.getValue());
		data.setHeaderName(XHeaderName.RETURN_PATH.getValue());
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:spamblocker-challenge|spamhippo|devnull-quarantine)\\@" +
				"|\\@(?:spamstomp\\.com|ipermitmail\\.com)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:Your email requires verification verify:" +
				"|Please Verify Your Email Address" +
				"|Unverified email to " +
				"|Your mail to .* requires confirmation)" +
				"|\\[Qurb .\\d+\\]$");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,2);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("confirm-\\S+\\@spamguard\\.vanquish\\.com");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.AUTO_REPLY.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:Exception.*(?:Out\\b.*of\\b.*Office|Autoreply:)|\\(Auto Response\\))" +
			 	"|^(?:Automatically Generated Response from|Auto-Respond E-?Mail from" +
			 	"|AutoResponse - Email Returned|automated response|Yahoo! Auto Response)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:automated-response|autoresponder|autoresponse-\\S+)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,2);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^This messages was created automatically by mail delivery software" +
				"|(?:\\bThis is an autoresponder. I'll never see your message\\b" +
				"|(?:\\bI(?:.m|\\s+am|\\s+will\\s+be|.ll\\s+be)\\s+(?:(?:out\\s+of|away\\s+from)\\s+the\\s+office|on\\s+vacation)\\s+(?:from|to|until|after)\\b)" +
				"|\\bI\\s+am\\s+currently\\s+out\\s+of\\s+the\\s+office\\b" +
				"|\\bI ?.m\\s+away\\s+until\\s+.{10,20}\\s+and\\s+am\\s+unable\\s+to\\s+read\\s+your\\s+message\\b)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.VIRUS_BLOCK.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:Disallowed attachment type found" +
				"|Norton Anti.?Virus failed to scan an attachment in a message you sent" +
				"|Norton Anti.?Virus detected and quarantined" +
				"|Warning - You sent a Virus Infected Email to " +
				"|Warning:\\s*E-?mail virus(es)? detected" +
				"|MailMarshal has detected a Virus in your message" +
				"|Banned or potentially offensive material" +
				"|Failed to clean virus\\b" +
				"|Virus Alert\\b" +
				"|Virus detected " +
				"|Virus to sender" +
				"|NAV detected a virus in a document " +
				"|InterScan MSS for SMTP has delivered a message" +
				"|InterScan NT Alert" +
				"|Antigen found\\b" +
				"|MMS Notification" +
				"|VIRUS IN YOUR MAIL " +
				"|Scan.?Mail Message: ?.{0,30} virus found " +
				"|McAfee GroupShield Alert)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:Undeliverable mail, invalid characters in header" +
				"|Delivery (?:warning|error) report id=" +
				"|The MIME information you requested" +
				"|Content violation" +
				"|Report to Sender" +
				"|RAV Anti.?Virus scan results" +
				"|Symantec AVF detected " +
				"|Symantec E-Mail-Proxy " +
				"|Virus Found in message" +
				"|Inflex scan report \\[" +
				"|\\[Mail Delivery .{10,100} infected attachment.*removed)" +
			"|(?:(Re: ?)+Wicked screensaver\\b" +
				"|\\bmailsweeper\\b" +
				"|\\bFile type Forbidden\\b" +
				"|AntiVirus scan results" +
				"|Security.?Scan Anti.?Virus" +
				"|Norton\\sAntiVirus\\b.*detected)" +
			"|^(?:Message Undeliverable: Possible Junk\\/Spam Mail Identified" +
				"|EMAIL REJECTED" +
				"|Virusmelding)$");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,2);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FROM_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:virus|scanner|devnull)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.MAIL_BLOCK.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("Message\\b.*blocked\\b.*bulk email filter");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("blocked by\\b.*Spam Firewall");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.BROADCAST.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.RULE_NAME.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameBuiltin.BROADCAST.getValue());
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.UNSUBSCRIBE.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.TO_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^mailinglist@.*|^jwang@localhost$");
		data.setTargetProcName("mailingListRegExService");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("unsubscribe");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.SUBSCRIBE.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.TO_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^mailinglist@.*|^jwang@localhost$");
		data.setTargetProcName("mailingListRegExService");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("\\s*subscribe\\s*");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameBuiltin.RMA_REQUEST.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.RULE_NAME.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameBuiltin.RMA_REQUEST.getValue());
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameCustom.OUF_OF_OFFICE_AUTO_REPLY.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:out\\s+of\\s+.*office|\\(away from the office\\)$)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^.{0,100}\\bwill\\b.{0,50}return|^.{4,100}\\breturning\\b|^.{2,100}\\bvacation\\b");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameCustom.CONTACT_US.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.MAILBOX_USER.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("support");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.STARTS_WITH.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("Inquiry About:");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameCustom.EXECUTABLE_ATTACHMENT.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.VALUED.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("dummy");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.FILE_NAME.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText(".*\\.(?:exe|bat|cmd|com|msi|ocx)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameCustom.XHEADER_SPAM_SCORE.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.X_HEADER.getValue());
		data.setHeaderName("X_Spam_Score");
		data.setCriteria(RuleCriteria.GREATER_THAN.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("100");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameCustom.HARD_BOUNCE_WATCHED_MAILBOX.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.RULE_NAME.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameBuiltin.HARD_BOUNCE.getValue());
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.TO_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.STARTS_WITH.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("watched_maibox@");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameCustom.HARD_BPUNCE_NO_FINAL_RCPT.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.RULE_NAME.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.EQUALS.getValue());
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameBuiltin.HARD_BOUNCE.getValue());
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(EmailAddrType.FINAL_RCPT_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.NOT_VALUED.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,2);
		data.setRuleElementPK(pk);
		data.setDataName(EmailAddrType.ORIG_RCPT_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.NOT_VALUED.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameSubrule.HardBounce_Subj_Match.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.SUBJECT.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("^(?:Returned mail:\\s(?:User unknown|Data format error)" +
				"|Undeliverable: |Undeliver(?:able|ed) Mail\\b|Undeliverable Message" +
				"|Returned mail.{0,5}(?:Error During Delivery|see transcript for details)" +
				"|e-?mail addressing error \\(|No valid recipient in )" +
			"|(?:User.*unknown|failed.*delivery|delivery.*(?:failed|failure|problem)" +
				"|Returned mail:.*(?:failed|failure|error)|\\(Failure\\)|failure notice" +
				"|not.*delivered)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameSubrule.HardBounce_Body_Match.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:\\bYou(?:.ve| have) reached a non.?working address\\.\\s+Please check\\b" +
				"|eTrust Secure Content Manager SMTPMAIL could not deliver the e-?mail" +
				"|\\bPlease do not resend your original message\\." +
				"|\\s[45]\\.\\d{1,3}\\.\\d{1,3}\\s)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameSubrule.MailboxFull_Body_Match.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:mailbox|inbox|account).{1,50}(?:exceed|is|was).{1,40}(?:storage|full|limit|size|quota)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:storage|full|limit|size|quota)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameSubrule.SpamBlock_Body_Match.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.MSG_REF_ID.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.NOT_VALUED.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("dummy");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameSubrule.ChalResp_Body_Match.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:Your mail .* requires your confirmation" +
				"|Your message .* anti-spam system.* iPermitMail" +
				"|apologize .* automatic reply.* control spam.* approved senders" +
				"|Vanquish to avoid spam.* automated message" +
				"|automated message.* apologize .* approved senders)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameSubrule.VirusBlock_Body_Match.getValue());

		data = new RuleElement();
		pk = new RuleElementPK(obj,0);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:a potentially executable attachment " +
				"|\\bhas stripped one or more attachments from the following message\\b" +
				"|message contains file attachments that are not permitted" +
				"|host \\S+ said: 5\\d\\d\\s+Error: Message content rejected" +
				"|TRANSACTION FAILED - Unrepairable Virus Detected. " +
				"|Mail.?Marshal Rule: Inbound Messages : Block Dangerous Attachments" +
				"|The mail message \\S+ \\S+ you sent to \\S+ contains the virus" +
				"|mailsweeper has found that a \\S+ \\S+ \\S+ \\S+ one or more virus" +
				"|Attachment.{0,40}was Deleted" +
				"|Virus.{1,40}was found" +
				"|\\bblocked by Mailsweeper\\b" +
				"|\\bvirus scanner deleted your message\\b" +
				"|\\bThe attachment was quarantined\\b" +
				"|\\bGROUP securiQ.Wall\\b)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		pk = new RuleElementPK(obj,1);
		data.setRuleElementPK(pk);
		data.setDataName(RuleDataName.BODY.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleCriteria.REG_EX.getValue());
		data.setCaseSensitive(false);
		data.setTargetText("(?:Reason: Rejected by filter" +
				"|antivirus system report" +
				"|the antivirus module has" +
				"|the infected attachment" +
				"|illegal attachment" +
				"|Unrepairable Virus Detected" +
				"|Reporting-MTA: Norton Anti.?Virus Gateway" +
				"|\\bV I R U S\\b)" +
			"|^(?:Found virus \\S+ in file \\S+" +
				"|Incident Information:)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadRuleSubruleMaps() {
		RuleLogic obj1 = service.getByRuleName(RuleNameBuiltin.HARD_BOUNCE.getValue());
		RuleLogic obj2 = service.getByRuleName(RuleNameSubrule.HardBounce_Subj_Match.getValue());
		RuleSubruleMapPK pk1;
		
		RuleSubruleMap data = new RuleSubruleMap();
		pk1 = new RuleSubruleMapPK(obj1,obj2);
		data.setRuleSubruleMapPK(pk1);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
		
		obj2 = service.getByRuleName(RuleNameSubrule.HardBounce_Body_Match.getValue());
		
		data = new RuleSubruleMap();
		pk1 = new RuleSubruleMapPK(obj1,obj2);
		data.setRuleSubruleMapPK(pk1);
		data.setSubruleSequence(1);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
		
		obj1 = service.getByRuleName(RuleNameBuiltin.MAILBOX_FULL.getValue());
		obj2 = service.getByRuleName(RuleNameSubrule.MailboxFull_Body_Match.getValue());
			
		data = new RuleSubruleMap();
		pk1 = new RuleSubruleMapPK(obj1,obj2);
		data.setRuleSubruleMapPK(pk1);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		obj1 = service.getByRuleName(RuleNameBuiltin.SPAM_BLOCK.getValue());
		obj2 = service.getByRuleName(RuleNameSubrule.SpamBlock_Body_Match.getValue());
			
		data = new RuleSubruleMap();
		pk1 = new RuleSubruleMapPK(obj1,obj2);
		data.setRuleSubruleMapPK(pk1);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		obj1 = service.getByRuleName(RuleNameBuiltin.CHALLENGE_RESPONSE.getValue());
		obj2 = service.getByRuleName(RuleNameSubrule.ChalResp_Body_Match.getValue());
			
		data = new RuleSubruleMap();
		pk1 = new RuleSubruleMapPK(obj1,obj2);
		data.setRuleSubruleMapPK(pk1);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		obj1 = service.getByRuleName(RuleNameBuiltin.VIRUS_BLOCK.getValue());
		obj2 = service.getByRuleName(RuleNameSubrule.VirusBlock_Body_Match.getValue());
			
		data = new RuleSubruleMap();
		pk1 = new RuleSubruleMapPK(obj1,obj2);
		data.setRuleSubruleMapPK(pk1);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);

		logger.info("EntityManager persisted the record.");
	}
}

