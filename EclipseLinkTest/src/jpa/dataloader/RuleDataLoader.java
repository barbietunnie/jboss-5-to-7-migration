package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.RuleNameType;
import jpa.constant.StatusId;
import jpa.constant.XHeaderName;
import jpa.model.RuleBase;
import jpa.model.RuleElement;
import jpa.model.RuleLogic;
import jpa.model.RuleSubruleMap;
import jpa.service.RuleElementService;
import jpa.service.RuleLogicService;
import jpa.service.RuleSubruleMapService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class RuleDataLoader implements AbstractDataLoader {
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
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("idtokens_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadBuiltInRules();
			loadCustomRules();
			loadSubRules();
			loadRuleElements();
			loadRuleSubruleMaps();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	private void loadBuiltInRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		RuleLogic data = new RuleLogic();
		data.setRuleName("Unattended_Mailbox");
		data.setEvalSequence(0);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.PRE_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("simply get rid of the messages from the mailbox.");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		// built-in rules
		data = new RuleLogic();
		data.setRuleName(RuleNameType.HARD_BOUNCE.getValue());
		data.setEvalSequence(101);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("from RFC Scan Routine, or from postmaster with sub-rules");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.SOFT_BOUNCE.getValue());
		data.setEvalSequence(102);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Soft bounce, from RFC scan routine");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.MAILBOX_FULL.getValue());
		data.setEvalSequence(102);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Mailbox full from postmaster with sub-rules");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.SIZE_TOO_LARGE.getValue());
		data.setEvalSequence(104);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Message size too large");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.MAIL_BLOCK.getValue());
		data.setEvalSequence(105);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Bounced from Bulk Email Filter");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.SPAM_BLOCK.getValue());
		data.setEvalSequence(106);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Bounced from Spam blocker");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.VIRUS_BLOCK.getValue());
		data.setEvalSequence(107);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Bounced from Virus blocker");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName(RuleNameType.CHALLENGE_RESPONSE.getValue());
		data.setEvalSequence(108);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Bounced from Challenge Response");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName(RuleNameType.AUTO_REPLY.getValue());
		data.setEvalSequence(109);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Auto reply from email client software");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.CC_USER.getValue());
		data.setEvalSequence(110);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("from scan routine, message received as recipient of CC or BCC");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.MDN_RECEIPT.getValue());
		data.setEvalSequence(111);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("from RFC scan, Message Delivery Notification, a positive receipt");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.GENERIC.getValue());
		data.setEvalSequence(112);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("Non bounce or system could not recognize it");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.UNSUBSCRIBE.getValue());
		data.setEvalSequence(113);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("remove from a mailing list");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName(RuleNameType.SUBSCRIBE.getValue());
		data.setEvalSequence(114);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("subscribe to a mailing list");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName(RuleNameType.RMA_REQUEST.getValue());
		data.setEvalSequence(115);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("RMA request, internal only");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.CSR_REPLY.getValue());
		data.setEvalSequence(116);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("called from internal program");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.BROADCAST.getValue());
		data.setEvalSequence(117);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("called from internal program");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName(RuleNameType.SEND_MAIL.getValue());
		data.setEvalSequence(118);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(true);
		data.setDescription("called from internal program");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		// end of built-in rules

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadCustomRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// Custom Rules
		RuleLogic data = new RuleLogic();
		data.setRuleName("Executable_Attachment");
		data.setEvalSequence(200);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.PRE_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("Emails with executable attachment file(s)");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName("Contact_Us");
		data.setEvalSequence(201);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.PRE_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("Contact Us Form submitted from web site");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName("OutOfOffice_AutoReply");
		data.setEvalSequence(205);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.PRE_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("ouf of the office auto reply");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName("XHeader_SpamScore");
		data.setEvalSequence(210);
		data.setRuleType(RuleBase.SIMPLE_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.PRE_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("Examine x-headers for SPAM score.");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName("HardBouce_WatchedMailbox");
		data.setEvalSequence(215);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.POST_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("post rule for hard bounced emails.");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new RuleLogic();
		data.setRuleName("HardBounce_NoFinalRcpt");
		data.setEvalSequence(216);
		data.setRuleType(RuleBase.ALL_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.POST_RULE);
		data.setSubrule(false);
		data.setBuiltinRule(false);
		data.setDescription("post rule for hard bounces without final recipient.");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		logger.info("EntityManager persisted the record.");
	}

	private void loadSubRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// SubRules
		RuleLogic data = new RuleLogic();
		data.setRuleName("HardBounce_Subj_Match");
		data.setEvalSequence(218);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(true);
		data.setBuiltinRule(true);
		data.setDescription("Sub rule for hard bounces from postmaster");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName("HardBounce_Body_Match");
		data.setEvalSequence(219);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(true);
		data.setBuiltinRule(true);
		data.setDescription("Sub rule for hard bounces from postmaster");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName("MailboxFull_Body_Match");
		data.setEvalSequence(220);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(true);
		data.setBuiltinRule(true);
		data.setDescription("Sub rule for mailbox full");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName("SpamBlock_Body_Match");
		data.setEvalSequence(221);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(true);
		data.setBuiltinRule(true);
		data.setDescription("Sub rule for spam block");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName("VirusBlock_Body_Match");
		data.setEvalSequence(222);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(true);
		data.setBuiltinRule(true);
		data.setDescription("Sub rule for virus block");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new RuleLogic();
		data.setRuleName("ChalResp_Body_Match");
		data.setEvalSequence(223);
		data.setRuleType(RuleBase.ANY_RULE);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setStartTime(startTime);
		data.setMailType(Constants.SMTP_MAIL);
		data.setRuleCategory(RuleBase.MAIN_RULE);
		data.setSubrule(true);
		data.setBuiltinRule(true);
		data.setDescription("Sub rule for challenge response");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadRuleElements() {
		RuleLogic obj = service.getByRuleName("Unattended_Mailbox");
		
		RuleElement data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.MAILBOX_USER);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(false);
		data.setTargetText("noreply");
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.RETURN_PATH);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^<?.+@.+>?$"); // make sure the return path is not blank or <>
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.HARD_BOUNCE.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^(?:postmaster|mailmaster|mailadmin|administrator)\\S*\\@");
		data.setExclusions("postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME);
		data.setExclListProcName("excludingPostmastersService");
		data.setDelimiter(",");
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^(?:mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@");
		data.setExclusions("mailer-daemon@legacytojave.com,mailer-daemon@" + Constants.VENDER_DOMAIN_NAME);
		data.setExclListProcName(null);
		data.setDelimiter(",");
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.MAILBOX_FULL.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^(?:postmaster|mailmaster|mailadmin|administrator" +
				"|mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@");
		data.setExclusions("postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME);
		data.setExclListProcName(null);
		data.setDelimiter(",");
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.SPAM_BLOCK.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setCriteria(RuleBase.REG_EX);
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
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.BODY);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("EarthLink\\b.*(?:spamBlocker|spamArrest)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(2);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:^surfcontrol|.*You_Got_Spammed)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(3);
		data.setDataName(RuleBase.X_HEADER);
		data.setHeaderName(XHeaderName.RETURN_PATH);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^(?:pleaseforward|quotaagent)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(4);
		data.setDataName(RuleBase.X_HEADER);
		data.setHeaderName("Precedence");
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^(?:spam)$");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.CHALLENGE_RESPONSE.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.X_HEADER);
		data.setHeaderName(XHeaderName.RETURN_PATH);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:spamblocker-challenge|spamhippo|devnull-quarantine)\\@" +
				"|\\@(?:spamstomp\\.com|ipermitmail\\.com)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(2);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("confirm-\\S+\\@spamguard\\.vanquish\\.com");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.AUTO_REPLY.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:Exception.*(?:Out\\b.*of\\b.*Office|Autoreply:)|\\(Auto Response\\))" +
			 	"|^(?:Automatically Generated Response from|Auto-Respond E-?Mail from" +
			 	"|AutoResponse - Email Returned|automated response|Yahoo! Auto Response)");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^(?:automated-response|autoresponder|autoresponse-\\S+)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(2);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		
		obj = service.getByRuleName(RuleNameType.VIRUS_BLOCK.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(2);
		data.setDataName(RuleBase.FROM_ADDR);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:virus|scanner|devnull)\\S*\\@");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.MAIL_BLOCK.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("Message\\b.*blocked\\b.*bulk email filter");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("blocked by\\b.*Spam Firewall");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.BROADCAST.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.RULE_NAME);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameType.BROADCAST.getValue());
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.UNSUBSCRIBE.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.TO_ADDR);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^mailinglist@.*|^jwang@localhost$");
		data.setTargetProcName("mailingListRegExService");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(false);
		data.setTargetText("unsubscribe");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.SUBSCRIBE.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.TO_ADDR);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^mailinglist@.*|^jwang@localhost$");
		data.setTargetProcName("mailingListRegExService");
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("\\s*subscribe\\s*");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName(RuleNameType.RMA_REQUEST.getValue());

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.RULE_NAME);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameType.RMA_REQUEST.getValue());
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("OutOfOffice_AutoReply");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:out\\s+of\\s+.*office|\\(away from the office\\)$)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("^.{0,100}\\bwill\\b.{0,50}return|^.{4,100}\\breturning\\b|^.{2,100}\\bvacation\\b");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("Contact_Us");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.MAILBOX_USER);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(false);
		data.setTargetText("support");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.STARTS_WITH);
		data.setCaseSensitive(false);
		data.setTargetText("Inquiry About:");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("Executable_Attachment");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.VALUED);
		data.setCaseSensitive(false);
		data.setTargetText("dummy");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.FILE_NAME);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText(".*\\.(?:exe|bat|cmd|com|msi|ocx)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("XHeader_SpamScore");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.X_HEADER);
		data.setHeaderName("X_Spam_Score");
		data.setCriteria(RuleBase.GREATER_THAN);
		data.setCaseSensitive(false);
		data.setTargetText("100");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("HardBouce_WatchedMailbox");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.RULE_NAME);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameType.HARD_BOUNCE.getValue());
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.TO_ADDR);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.STARTS_WITH);
		data.setCaseSensitive(false);
		data.setTargetText("watched_maibox@");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("HardBounce_NoFinalRcpt");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.RULE_NAME);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.EQUALS);
		data.setCaseSensitive(true);
		data.setTargetText(RuleNameType.HARD_BOUNCE.getValue());
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(EmailAddrType.FINAL_RCPT_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleBase.NOT_VALUED);
		data.setCaseSensitive(false);
		data.setTargetText("");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(2);
		data.setDataName(EmailAddrType.ORIG_RCPT_ADDR.getValue());
		data.setHeaderName(null);
		data.setCriteria(RuleBase.NOT_VALUED);
		data.setCaseSensitive(false);
		data.setTargetText("");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("HardBounce_Subj_Match");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.SUBJECT);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		
		obj = service.getByRuleName("HardBounce_Body_Match");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		
		obj = service.getByRuleName("MailboxFull_Body_Match");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:mailbox|inbox|account).{1,50}(?:exceed|is|was).{1,40}(?:storage|full|limit|size|quota)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
		data.setCaseSensitive(false);
		data.setTargetText("(?:storage|full|limit|size|quota)");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("SpamBlock_Body_Match");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.MSG_REF_ID);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.NOT_VALUED);
		data.setCaseSensitive(false);
		data.setTargetText("dummy");
		data.setTargetProcName(null);
		data.setExclusions(null);
		data.setExclListProcName(null);
		data.setDelimiter(null);
		elementService.insert(data);
		
		obj = service.getByRuleName("ChalResp_Body_Match");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		
		obj = service.getByRuleName("VirusBlock_Body_Match");

		data = new RuleElement();
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(0);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		data.setRuleLogic((RuleLogic)obj);
		data.setElementSequence(1);
		data.setDataName(RuleBase.BODY);
		data.setHeaderName(null);
		data.setCriteria(RuleBase.REG_EX);
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
		RuleLogic obj1 = service.getByRuleName(RuleNameType.HARD_BOUNCE.getValue());
		RuleLogic obj2 = service.getByRuleName("HardBounce_Subj_Match");
		
		RuleSubruleMap data = new RuleSubruleMap();
		data.setRuleLogic((RuleLogic)obj1);
		data.setSubruleLogic((RuleLogic)obj2);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
		
		obj2 = service.getByRuleName("HardBounce_Body_Match");
		
		data = new RuleSubruleMap();
		data.setRuleLogic((RuleLogic)obj1);
		data.setSubruleLogic((RuleLogic)obj2);
		data.setSubruleSequence(1);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
		
		obj1 = service.getByRuleName(RuleNameType.MAILBOX_FULL.getValue());
		obj2 = service.getByRuleName("MailboxFull_Body_Match");
			
		data = new RuleSubruleMap();
		data.setRuleLogic((RuleLogic)obj1);
		data.setSubruleLogic((RuleLogic)obj2);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		obj1 = service.getByRuleName(RuleNameType.SPAM_BLOCK.getValue());
		obj2 = service.getByRuleName("SpamBlock_Body_Match");
			
		data = new RuleSubruleMap();
		data.setRuleLogic((RuleLogic)obj1);
		data.setSubruleLogic((RuleLogic)obj2);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		obj1 = service.getByRuleName(RuleNameType.CHALLENGE_RESPONSE.getValue());
		obj2 = service.getByRuleName("ChalResp_Body_Match");
			
		data = new RuleSubruleMap();
		data.setRuleLogic((RuleLogic)obj1);
		data.setSubruleLogic((RuleLogic)obj2);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		obj1 = service.getByRuleName(RuleNameType.VIRUS_BLOCK.getValue());
		obj2 = service.getByRuleName("VirusBlock_Body_Match");
			
		data = new RuleSubruleMap();
		data.setRuleLogic((RuleLogic)obj1);
		data.setSubruleLogic((RuleLogic)obj2);
		data.setSubruleSequence(0);
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mapService.insert(data);
			
		logger.info("EntityManager persisted the record.");
	}
}

