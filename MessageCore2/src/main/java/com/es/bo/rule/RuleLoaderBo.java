package com.es.bo.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.es.core.util.SpringUtil;
import com.es.dao.address.MailingListDao;
import com.es.dao.sender.ReloadFlagsDao;
import com.es.dao.sender.SenderDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.RuleCategory;
import com.es.data.constant.RuleCriteria;
import com.es.data.constant.RuleType;
import com.es.data.constant.XHeaderName;
import com.es.vo.address.MailingListVo;
import com.es.vo.comm.ReloadFlagsVo;
import com.es.vo.comm.SenderVo;
import com.es.vo.rule.RuleElementVo;
import com.es.vo.rule.RuleLogicVo;
import com.es.vo.rule.RuleSubRuleMapVo;
import com.es.vo.rule.RuleVo;

@Component("ruleLoaderBo")
@Scope(value="prototype")
public class RuleLoaderBo implements java.io.Serializable {
	private static final long serialVersionUID = -1239400387960912573L;
	static final Logger logger = Logger.getLogger(RuleLoaderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final List<RuleBase>[] mainRules;
	final List<RuleBase>[] preRules;
	final List<RuleBase>[] postRules;
	final HashMap<String, List<RuleBase>>[] subRules;
	
	private int currIndex = 0;
	@Autowired
	private RulesDataBo rulesDataBo;
	
	private int currIndex2 = 0;
	private final Map<String, Pattern>[] patternMaps;
	
	private ReloadFlagsVo reloadFlagsVo;
	private long lastTimeLoaded;
	final static int INTERVAL = 5 * 60 * 1000; // 5 minutes

	public RuleLoaderBo() {
		this(SpringUtil.getAppContext().getBean(RulesDataBo.class).getCurrentRules());
	}
	
	@SuppressWarnings("unchecked")
	RuleLoaderBo(List<RuleVo> ruleVos) {
		/*
		 * define place holders for two sets of rules
		 */
		mainRules = new List[2];
		preRules = new List[2];
		postRules = new List[2];
		subRules = new HashMap[2];
		
		mainRules[0] = new ArrayList<RuleBase>();
		mainRules[1] = new ArrayList<RuleBase>();
		preRules[0] = new ArrayList<RuleBase>();
		preRules[1] = new ArrayList<RuleBase>();
		postRules[0] = new ArrayList<RuleBase>();
		postRules[1] = new ArrayList<RuleBase>();
		subRules[0] = new HashMap<String, List<RuleBase>>();
		subRules[1] = new HashMap<String, List<RuleBase>>();
		
		// load the first set of rules
		loadRuleSets(ruleVos, currIndex);
		
		/*
		 * define place holder for two sets of pattern maps
		 */
		patternMaps = new LinkedHashMap[2];
		patternMaps[0] = loadAddressPatterns();
		patternMaps[1] = loadAddressPatterns();
		
		reloadFlagsVo = getReloadFlagsDao().select();
		lastTimeLoaded = new java.util.Date().getTime();
	}
	
	private void reloadRules() {
		/*
		 * Two sets of rules are used in turns. When one of the set becomes active set,
		 * another set becomes inactive set. The rules getters always return rules from
		 * the active set. <br>
		 * When this method is called, it reloads rules from database and stores them in
		 * the inactive set. It then switch the inactive set to active before it quits.
		 */
		if (rulesDataBo != null) {
			List<RuleVo> ruleVos = rulesDataBo.getCurrentRules();
			int newIndex = (currIndex + 1) % 2;
			loadRuleSets(ruleVos, newIndex);
			currIndex = newIndex;
		}
		else {
			logger.warn("reloadRules() - ruleDataBo is null, will not reload");
		}
	}
	
	private void reloadAddressPatterns() {
		/*
		 * Two sets of patterns are used in turns. When one of the set becomes active 
		 * set, another set becomes inactive set. The patterns getter always return
		 * patterns from the active set. <br>
		 * When this method is called, it reloads patterns from database and stores
		 * them in the inactive set. It then switch the inactive set to active before
		 * it quits.
		 */
		int newIndex = (currIndex2 + 1) % 2;
		patternMaps[newIndex] = loadAddressPatterns();
		currIndex2 = newIndex;
	}
	
	private synchronized void checkChangesAndPerformReload() {
		long currTime = new java.util.Date().getTime();
		if (currTime > (lastTimeLoaded + INTERVAL)) {
			// check change flags and reload rule and address patterns
			ReloadFlagsVo vo = getReloadFlagsDao().select();
			if (reloadFlagsVo != null && vo != null) {
				if (reloadFlagsVo.getRules() < vo.getRules()
						|| reloadFlagsVo.getActions() < vo.getActions()
						|| reloadFlagsVo.getTemplates() < vo.getTemplates()) {
					logger.info("====== Rules and/or Actions changed, reload Rules ======");
					reloadFlagsVo.setRules(vo.getRules());
					reloadFlagsVo.setActions(vo.getActions());
					reloadRules();
				}
				if (reloadFlagsVo.getSenders() < vo.getSenders()
						|| reloadFlagsVo.getTemplates() < vo.getTemplates()) {
					logger.info("====== Senderts/Templates changed, reload Address Patterns ======");
					reloadFlagsVo.setSenders(vo.getSenders());
					reloadAddressPatterns();
				}
				reloadFlagsVo.setTemplates(vo.getTemplates());
			}
			lastTimeLoaded = currTime;
		}
	}
	
	private void loadRuleSets(List<RuleVo> ruleVos, int index) {
		mainRules[index].clear();
		preRules[index].clear();
		postRules[index].clear();
		subRules[index].clear();
		
		for (int i = 0; i < ruleVos.size(); i++) {
			RuleVo ruleVo = ruleVos.get(i);
			List<RuleBase> rules = createRules(ruleVo);
			if (rules.size() == 0) {
				continue;
			}
			
			if (RuleCategory.PRE_RULE.getValue().equals(ruleVo.getRuleLogicVo().getRuleCategory())) {
				preRules[index].addAll(rules);
			}
			else if (RuleCategory.POST_RULE.getValue().equals(ruleVo.getRuleLogicVo().getRuleCategory())) {
				postRules[index].addAll(rules);
			}
			else if (!(ruleVo.getRuleLogicVo().isSubRule())) {
				mainRules[index].addAll(rules);
			}
			
			// a non sub-rule could also be used as a sub-rule
			subRules[index].put(ruleVo.getRuleName(), rules);
		}
	}
	
	private List<RuleBase> createRules(RuleVo ruleVo) {
		List<RuleBase> rules = new ArrayList<RuleBase>();
		RuleLogicVo logicVo = ruleVo.getRuleLogicVo();
		List<RuleElementVo> elementVos = ruleVo.getRuleElementVos();
		List<RuleSubRuleMapVo> subRuleVos = ruleVo.getRuleSubRuleVos();
		
		// build rules
		if (RuleType.SIMPLE.getValue().equals(logicVo.getRuleType()))	{
			for (int i=0; i<elementVos.size(); i++) {
				RuleElementVo elementVo = elementVos.get(i);
				RuleSimple ruleSimple = new RuleSimple(
					logicVo.getRuleName(),
					RuleType.getByValue(logicVo.getRuleType()),
					logicVo.getMailType(),
					elementVo.getDataName(),
					XHeaderName.getByValue(elementVo.getHeaderName()),
					RuleCriteria.getByValue(elementVo.getCriteria()),
					CodeType.YES_CODE.equals(elementVo.getCaseSensitive()),
					elementVo.getTargetText(),
					elementVo.getExclusions(),
					elementVo.getExclListProc(),
					elementVo.getDelimiter()
					);
				
				for (int j=0; j<subRuleVos.size(); j++) {
					RuleSubRuleMapVo subRuleVo = subRuleVos.get(j);
					ruleSimple.subruleList.add(subRuleVo.getSubRuleName());
				}
				
				rules.add(ruleSimple);
			}
		}
		else { // all/any/none rule
			List<RuleBase> ruleList = new ArrayList<RuleBase>();
			for (int i=0; i<elementVos.size(); i++) {
				RuleElementVo elementVo = elementVos.get(i);
				RuleSimple ruleSimple = new RuleSimple(
					logicVo.getRuleName(),
					RuleType.getByValue(logicVo.getRuleType()),
					logicVo.getMailType(),
					elementVo.getDataName(),
					XHeaderName.getByValue(elementVo.getHeaderName()),
					RuleCriteria.getByValue(elementVo.getCriteria()),
					CodeType.YES_CODE.getValue().equals(elementVo.getCaseSensitive()),
					elementVo.getTargetText(),
					elementVo.getExclusions(),
					elementVo.getExclListProc(),
					elementVo.getDelimiter()
					);
				ruleList.add(ruleSimple);
			}

			RuleComplex ruleComplex = new RuleComplex(
					ruleVo.getRuleName(),
					RuleType.getByValue(logicVo.getRuleType()),
					logicVo.getMailType(),
					ruleList
					);
			
			for (int j=0; j<subRuleVos.size(); j++) {
				RuleSubRuleMapVo subRuleVo = subRuleVos.get(j);
				ruleComplex.subruleList.add(subRuleVo.getSubRuleName());
			}

			rules.add(ruleComplex);
		}
		
		return rules;
	}

	public List<RuleBase> getPreRuleSet() {
		checkChangesAndPerformReload();
		return preRules[currIndex];
	}

	public List<RuleBase> getRuleSet() {
		return mainRules[currIndex];
	}

	public List<RuleBase> getPostRuleSet() {
		return postRules[currIndex];
	}

	public Map<String, List<RuleBase>> getSubRuleSet() {
		return subRules[currIndex];
	}

	public void listRuleNames() {
		listRuleNames(System.out);
	}

	public void listRuleNames(java.io.PrintStream prt) {
		try {
			listRuleNames("Pre  Rule", preRules[currIndex], prt);
			listRuleNames("Main Rule", mainRules[currIndex], prt);
			listRuleNames("Post Rule", postRules[currIndex], prt);
			listRuleNames("Sub  Rule", subRules[currIndex], prt);
		}
		catch (Exception e) {
			logger.error("Exception caught during ListRuleNames", e);
		}
	}

	private void listRuleNames(String ruleLit, List<RuleBase> rules, java.io.PrintStream prt) {
		Iterator<RuleBase> it = rules.iterator();
		while (it.hasNext()) {
			RuleBase r = it.next();
			String ruleName = StringUtils.rightPad(r.getRuleName(), 28, " ");
			prt.print("RuleLoaderBo.1 - " + ruleLit + ": " + ruleName);
			listSubRuleNames(r.getSubRules(), prt);
			prt.println();
		}
	}

	private void listRuleNames(String ruleLit, Map<String,?> rules, java.io.PrintStream prt) {
		Set<?> keys = rules.keySet();
		for (Iterator<?> it=keys.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (obj instanceof RuleBase) {
				RuleBase r = (RuleBase) obj;
				String ruleName = StringUtils.rightPad(r.getRuleName(), 28, " ");
				prt.println("RuleLoaderBo.2 - " + ruleLit + ": " + ruleName);
				listSubRuleNames(r.getSubRules(), prt);
 			}
			else {
				String ruleName = (String) obj;
				prt.println("RuleLoaderBo.3 - " + ruleLit + ": " + ruleName);
			}
		}
	}

	private void listSubRuleNames(List<String> subRuleNames, java.io.PrintStream prt) {
		if (subRuleNames != null) {
			for (int i = 0; i < subRuleNames.size(); i++) {
				if (i == 0)
					prt.print("SubRules: " + subRuleNames.get(i));
				else
					prt.print(", " + subRuleNames.get(i));
			}
		}		
	}
	
	public String findSenderIdByAddr(String addr) {
		if (StringUtils.isEmpty(addr)) {
			return null;
		}
		Map<String, Pattern> patterns = getPatterns();
		Set<String> set = patterns.keySet();
		for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
			String key = it.next();
			Pattern pattern = patterns.get(key);
			if (pattern == null) { // should never happen
				String error = "Threading Error, Contact Programming!!!";
				logger.fatal(error, new Exception(error));
				continue;
			}
			Matcher m = pattern.matcher(addr);
			if (m.find()) {
				return key;
			}
		}
		return null;
	}
	
	private Map<String, Pattern> getPatterns() {
		return patternMaps[currIndex2];
	}
	
	private final Map<String, Pattern> loadAddressPatterns() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		// make sure the default sender is the first on the list
		SenderVo sender0 = getSenderDao().getBySenderId(Constants.DEFAULT_SENDER_ID);
		if (sender0 != null) {
			String senderId = sender0.getSenderId();
			String returnPath = buildReturnPath(sender0);
			map.put(senderId, returnPath);
		}
		List<SenderVo> senders = getSenderDao().getAll();
		// now add all other senders' return path
		for (SenderVo sender : senders) {
			String senderId = sender.getSenderId();
			if (Constants.DEFAULT_SENDER_ID.equalsIgnoreCase(senderId)) {
				continue; // skip the default sender
			}
			String returnPath = buildReturnPath(sender);
			if (map.containsKey(senderId)) {
				map.put(senderId, map.get(senderId) + "|" + returnPath);
			}
			else {
				map.put(senderId, returnPath);
			}
		}
		// add mailing list addresses
		List<MailingListVo> lists = getMailingListDao().getAll(true);
		for (MailingListVo list : lists) {
			String senderId = list.getSenderId();
			String returnPath = list.getEmailAddr();
			if (map.containsKey(senderId)) {
				map.put(senderId, map.get(senderId) + "|" + returnPath);
			}
			else {
				map.put(senderId, returnPath);
			}
		}
		// create regular expressions
		Map<String, Pattern> patterns = new LinkedHashMap<String, Pattern>();
		Set<String> set = map.keySet();
		for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
			String key = it.next();
			String regex = map.get(key);
			logger.info(">>>>> Address Pathern: "
					+ StringUtils.rightPad(key, 10, " ") + " -> " + regex);
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			patterns.put(key, pattern);
		}
		return patterns;
	}
	
	private String buildReturnPath(SenderVo vo) {
		String domainName = vo.getDomainName().trim();
		String returnPath = vo.getReturnPathLeft().trim() + "@" + domainName;
		if (CodeType.YES.getValue().equalsIgnoreCase(vo.getIsVerpEnabled())) {
			// if VERP is enabled, add VERP addresses to the pattern 
			String verpSub = vo.getVerpSubDomain();
			verpSub = (StringUtils.isEmpty(verpSub) ? "" : verpSub.trim() + ".");
			if (!StringUtils.isEmpty(vo.getVerpInboxName())) {
				returnPath += "|" + vo.getVerpInboxName().trim() + "@" + verpSub + domainName;
			}
			if (!StringUtils.isEmpty(vo.getVerpRemoveInbox())) {
				returnPath += "|" + vo.getVerpRemoveInbox().trim() + "@" + verpSub + domainName;
			}
		}
		if (CodeType.YES.getValue().equalsIgnoreCase(vo.getUseTestAddr())) {
			// if in test mode, add test address to the pattern
			if (!StringUtils.isEmpty(vo.getTestFromAddr())) {
				returnPath += "|" + vo.getTestFromAddr().trim();
			}
			if (!StringUtils.isEmpty(vo.getTestReplytoAddr())) {
				returnPath += "|" + vo.getTestReplytoAddr().trim();
			}
			if (!StringUtils.isEmpty(vo.getTestToAddr())) {
				returnPath += "|" + vo.getTestToAddr().trim();
			}
		}
		return returnPath;
	}
	
	/*
	 *  called from constructor, can not be Autowired
	 */
	private SenderDao senderDao = null;
	private SenderDao getSenderDao() {
		if (senderDao == null) {
			senderDao = SpringUtil.getAppContext().getBean(SenderDao.class);
		}
		return senderDao;
	}
	
	private MailingListDao mailingListDao = null;
	private MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getAppContext().getBean(MailingListDao.class);
		}
		return mailingListDao;
	}
	
	private ReloadFlagsDao reloadFlagsDao = null;
	private ReloadFlagsDao getReloadFlagsDao() {
		if (reloadFlagsDao == null) {
			reloadFlagsDao = SpringUtil.getAppContext().getBean(ReloadFlagsDao.class);
		}
		return reloadFlagsDao;
	}
}
