package com.es.data.loader;
/**
 * The RuleBean referenced by ActionBean with a FOREIGN key restraint.
 */
import java.sql.Timestamp;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.rule.RuleElementDao;
import com.es.dao.rule.RuleLogicDao;
import com.es.dao.rule.RuleSubRuleMapDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.RuleCategory;
import com.es.data.constant.StatusId;
import com.es.data.preload.RuleElementEnum;
import com.es.data.preload.RuleNameEnum;
import com.es.data.preload.RuleSubruleMapEnum;
import com.es.vo.rule.RuleElementVo;
import com.es.vo.rule.RuleLogicVo;
import com.es.vo.rule.RuleSubRuleMapVo;

public class RuleTables extends AbstractTableBase {

	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE RULE_SUBRULE_MAP");
			System.out.println("Dropped RULE_SUBRULE_MAP Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE RULE_ELEMENT");
			System.out.println("Dropped RULE_ELEMENT Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE RULE_LOGIC");
			System.out.println("Dropped RULE_LOGIC Table...");
		}
		catch (DataAccessException e) {
		}
	}
	
	public void createTables() throws DataAccessException {
		createRuleLogicTable();
		createRuleElementTable();
		createRuleSubRuleMapTable();
	}
	
	public void loadTestData() throws DataAccessException {
		loadBuiltInRules();
		loadCustomRules();
		loadSubrules();
		loadRuleElements();
		loadRuleSubruleMaps();
	}
	
	void createRuleLogicTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RULE_LOGIC ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"RuleSeq int NOT NULL, " +
			"RuleType varchar(8) NOT NULL, " + // simple/or/and/none
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " +
			"StartTime datetime NOT NULL, " +
			"MailType varchar(8) NOT NULL, " + // smtpmail, webmail, ...
			"RuleCategory char(1) DEFAULT '" + RuleCategory.MAIN_RULE.getValue() + "', " + // E - Pre Scan, 'M' - Main Rule, P - Post Scan
			"IsSubRule char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', " +
			"builtInRule char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', " +
			"Description varchar(255), " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (RuleName) " + // use index to allow update to rule name
			") ENGINE=InnoDB");
			System.out.println("Created RULE_LOGIC Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleElementTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RULE_ELEMENT ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"ElementSeq int NOT NULL, " +
			"DataName varchar(26) NOT NULL, " +
			"HeaderName varchar(50), " + // X-Header name if DataName is X-Header
			"Criteria varchar(16) NOT NULL, " +
			"CaseSensitive char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', " + // Y/N
			"TargetText varchar(2000), " + 
			"TargetProc varchar(100), " +
			"Exclusions text, " + // delimited
			"ExclListProc varchar(100), " + // valid bean id
			"Delimiter char(5) DEFAULT ',', " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RULE_LOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(RuleName), " +
			"UNIQUE INDEX (RuleName, ElementSeq) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULE_ELEMENT Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleSubRuleMapTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RULE_SUBRULE_MAP ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"SubRuleName varchar(26) NOT NULL, " +
			"SubRuleSeq int NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RULE_LOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"FOREIGN KEY (SubRuleName) REFERENCES RULE_LOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (RuleName, SubRuleName) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULESUBRULEMAP Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	
	private void loadBuiltInRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		RuleLogicDao service = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		// built-in rules
		int ruleSeq = 100;
		for (RuleNameEnum ruleName : RuleNameEnum.getBuiltinRules()) {
			RuleLogicVo data = new RuleLogicVo();
			data.setRuleName(ruleName.getValue());
			data.setRuleSeq(++ruleSeq);
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(RuleCategory.MAIN_RULE.getValue());
			data.setIsSubRule(ruleName.isSubrule()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			data.setBuiltInRule(CodeType.YES_CODE.getValue());
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		// end of built-in rules

		System.out.println("Built-in Rule Logic records inserted.");
	}
	
	private void loadCustomRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		RuleLogicDao service = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		// Custom Rules
		int ruleSeq = 200;
		for (RuleNameEnum ruleName : RuleNameEnum.getCustomRules()) {
			RuleLogicVo data = new RuleLogicVo();
			data.setRuleName(ruleName.getValue());
			if (RuleNameEnum.UNATTENDED_MAILBOX.equals(ruleName)) {
				data.setRuleSeq(0);
			}
			else {
				data.setRuleSeq(++ruleSeq);
			}
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(ruleName.getRuleCategory().getValue());
			data.setIsSubRule(ruleName.isSubrule()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			data.setBuiltInRule(CodeType.NO_CODE.getValue());
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		System.out.println("Custom Rule Logic records inserted.");
	}

	private void loadSubrules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		RuleLogicDao service = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		// Built-in Sub Rules
		int ruleSeq = 225;
		for (RuleNameEnum ruleName : RuleNameEnum.getSubRules()) {
			RuleLogicVo data = new RuleLogicVo();
			data.setRuleName(ruleName.getValue());
			if (RuleNameEnum.UNATTENDED_MAILBOX.equals(ruleName)) {
				data.setRuleSeq(0);
			}
			else {
				data.setRuleSeq(++ruleSeq);
			}
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(ruleName.getRuleCategory().getValue());
			data.setIsSubRule(CodeType.YES_CODE.getValue());
			data.setBuiltInRule(CodeType.YES_CODE.getValue());
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		System.out.println("Built-in Subrule records inserted.");
	}
	
	private void loadRuleElements() {
		RuleLogicDao service = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		RuleElementDao elementService = SpringUtil.getAppContext().getBean(RuleElementDao.class);
		for (RuleElementEnum elm : RuleElementEnum.values()) {
			RuleLogicVo logic = service.getByRuleName(elm.getRuleName().getValue());
			RuleElementVo data = new RuleElementVo();
			data.setRuleName(logic.getRuleName());
			data.setElementSeq(elm.getRuleSequence());
			data.setDataName(elm.getRuleDataName().getValue());
			if (elm.getXheaderName()!=null) {
				data.setHeaderName(elm.getXheaderName().getValue());
			}
			data.setCriteria(elm.getRuleCriteria().getValue());
			data.setTextCaseSensitive(elm.isCaseSensitive());
			data.setTargetText(elm.getTargetText());
			data.setTargetProc(elm.getTargetProcName());
			data.setExclusions(elm.getExclusions());
			data.setExclListProc(elm.getExclListProcName());
			data.setDelimiter(elm.getDelimiter());
			elementService.insert(data);
		}

		System.out.println("Rule Element records inserted.");
	}
	
	private void loadRuleSubruleMaps() {
		RuleLogicDao service = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		RuleSubRuleMapDao mapService = SpringUtil.getAppContext().getBean(RuleSubRuleMapDao.class);
		for (RuleSubruleMapEnum map : RuleSubruleMapEnum.values()) {
			RuleLogicVo rule = service.getByRuleName(map.getRuleName().getValue());
			RuleLogicVo subrule = service.getByRuleName(map.getSubruleName().getValue());
			RuleSubRuleMapVo data = new RuleSubRuleMapVo();
			data.setRuleName(rule.getRuleName());
			data.setSubRuleName(subrule.getRuleName());
			data.setSubRuleSeq(map.getSequence());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			mapService.insert(data);
		}
		
		System.out.println("Subrule records inserted.");
	}
	
}