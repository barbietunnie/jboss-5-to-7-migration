package com.legacytojava.message.bo.rule;

import static com.legacytojava.message.constant.Constants.DEFAULT_CLIENTID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.client.ClientDao;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.message.vo.ReloadFlagsVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.message.vo.rule.RuleElementVo;
import com.legacytojava.message.vo.rule.RuleLogicVo;
import com.legacytojava.message.vo.rule.RuleSubRuleMapVo;
import com.legacytojava.message.vo.rule.RuleVo;

@Component("ruleLoader")
@Scope(value="prototype")
public final class RuleLoader implements java.io.Serializable {
	private static final long serialVersionUID = 5251082728950956779L;
	static final Logger logger = Logger.getLogger(RuleLoader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final List<RuleBase>[] mainRules;
	final List<RuleBase>[] preRules;
	final List<RuleBase>[] postRules;
	final HashMap<String, List<RuleBase>>[] subRules;
	
	private int currIndex = 0;
	private transient RulesDataBo rulesDataBo = null;
	
	private int currIndex2 = 0;
	private final Map<String, Pattern>[] patternMaps;
	
	private ReloadFlagsVo reloadFlagsVo;
	private long lastTimeLoaded;
	final static int INTERVAL = 5 * 60 * 1000; // 5 minutes

	@Autowired(required=true)
	public RuleLoader(@Qualifier("rulesDataBo") RulesDataBo rulesDataBo) {
		this(rulesDataBo.getCurrentRules());
		this.rulesDataBo = rulesDataBo;
	}
	
	@SuppressWarnings("unchecked")
	public RuleLoader(List<RuleVo> ruleVos) {
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
				if (reloadFlagsVo.getClients() < vo.getClients()
						|| reloadFlagsVo.getTemplates() < vo.getTemplates()) {
					logger.info("====== Clients/Templates changed, reload Address Patterns ======");
					reloadFlagsVo.setClients(vo.getClients());
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
			RuleVo ruleVo = (RuleVo) ruleVos.get(i);
			List<RuleBase> rules = createRules(ruleVo);
			if (rules.size() == 0) {
				continue;
			}
			
			if (RuleBase.PRE_RULE.equals(ruleVo.getRuleLogicVo().getRuleCategory())) {
				preRules[index].addAll(rules);
			}
			else if (RuleBase.POST_RULE.equals(ruleVo.getRuleLogicVo().getRuleCategory())) {
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
		if (RuleBase.SIMPLE_RULE.equals(logicVo.getRuleType()))	{
			for (int i=0; i<elementVos.size(); i++) {
				RuleElementVo elementVo = elementVos.get(i);
				RuleSimple ruleSimple = new RuleSimple(
					logicVo.getRuleName(),
					logicVo.getRuleType(),
					logicVo.getMailType(),
					elementVo.getDataName(),
					elementVo.getHeaderName(),
					elementVo.getCriteria(),
					elementVo.getCaseSensitive(),
					elementVo.getTargetText(),
					elementVo.getExclusions(),
					elementVo.getExclListProc(),
					elementVo.getDelimiter()
					);
				
				for (int j=0; j<subRuleVos.size(); j++) {
					RuleSubRuleMapVo subRuleVo = subRuleVos.get(j);
					ruleSimple.subRuleList.add(subRuleVo.getSubRuleName());
				}
				
				rules.add(ruleSimple);
			}
		}
		else { // all/any/none rule
			List<RuleBase> ruleList = new ArrayList<RuleBase>();
			for (int i=0; i<elementVos.size(); i++) {
				RuleElementVo elementVo = (RuleElementVo)elementVos.get(i);
				RuleSimple ruleSimple = new RuleSimple(
					logicVo.getRuleName(),
					logicVo.getRuleType(),
					logicVo.getMailType(),
					elementVo.getDataName(),
					elementVo.getHeaderName(),
					elementVo.getCriteria(),
					elementVo.getCaseSensitive(),
					elementVo.getTargetText(),
					elementVo.getExclusions(),
					elementVo.getExclListProc(),
					elementVo.getDelimiter()
					);
				ruleList.add(ruleSimple);
			}

			RuleComplex ruleComplex = new RuleComplex(
					ruleVo.getRuleName(),
					logicVo.getRuleType(),
					logicVo.getMailType(),
					ruleList
					);
			
			for (int j=0; j<subRuleVos.size(); j++) {
				RuleSubRuleMapVo subRuleVo = (RuleSubRuleMapVo)subRuleVos.get(j);
				ruleComplex.subRuleList.add(subRuleVo.getSubRuleName());
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
			prt.print("RuleLoader.1 - " + ruleLit + ": " + ruleName);
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
				prt.println("RuleLoader.2 - " + ruleLit + ": " + ruleName);
				listSubRuleNames(r.getSubRules(), prt);
 			}
			else {
				String ruleName = (String) obj;
				prt.println("RuleLoader.3 - " + ruleLit + ": " + ruleName);
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
	
	public String findClientIdByAddr(String addr) {
		if (StringUtil.isEmpty(addr)) {
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
		// make sure the default client is the first on the list
		ClientVo client0 = getClientDao().getByClientId(DEFAULT_CLIENTID);
		if (client0 != null) {
			String clientId = client0.getClientId();
			String returnPath = buildReturnPath(client0);
			map.put(clientId, returnPath);
		}
		List<ClientVo> clients = getClientDao().getAll();
		// now add all other clients' return path
		for (ClientVo client : clients) {
			String clientId = client.getClientId();
			if (DEFAULT_CLIENTID.equalsIgnoreCase(clientId)) {
				continue; // skip the default client
			}
			String returnPath = buildReturnPath(client);
			if (map.containsKey(clientId)) {
				map.put(clientId, map.get(clientId) + "|" + returnPath);
			}
			else {
				map.put(clientId, returnPath);
			}
		}
		// add mailing list addresses
		List<MailingListVo> lists = getMailingListDao().getAll(true);
		for (MailingListVo list : lists) {
			String clientId = list.getClientId();
			String returnPath = list.getEmailAddr();
			if (map.containsKey(clientId)) {
				map.put(clientId, map.get(clientId) + "|" + returnPath);
			}
			else {
				map.put(clientId, returnPath);
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
	
	private String buildReturnPath(ClientVo vo) {
		String domainName = vo.getDomainName().trim();
		String returnPath = vo.getReturnPathLeft().trim() + "@" + domainName;
		if (Constants.YES.equalsIgnoreCase(vo.getIsVerpEnabled())) {
			// if VERP is enabled, add VERP addresses to the pattern 
			String verpSub = vo.getVerpSubDomain();
			verpSub = (StringUtil.isEmpty(verpSub) ? "" : verpSub.trim() + ".");
			if (!StringUtil.isEmpty(vo.getVerpInboxName())) {
				returnPath += "|" + vo.getVerpInboxName().trim() + "@" + verpSub + domainName;
			}
			if (!StringUtil.isEmpty(vo.getVerpRemoveInbox())) {
				returnPath += "|" + vo.getVerpRemoveInbox().trim() + "@" + verpSub + domainName;
			}
		}
		if (Constants.YES.equalsIgnoreCase(vo.getUseTestAddr())) {
			// if in test mode, add test address to the pattern
			if (!StringUtil.isEmpty(vo.getTestFromAddr())) {
				returnPath += "|" + vo.getTestFromAddr().trim();
			}
			if (!StringUtil.isEmpty(vo.getTestReplytoAddr())) {
				returnPath += "|" + vo.getTestReplytoAddr().trim();
			}
			if (!StringUtil.isEmpty(vo.getTestToAddr())) {
				returnPath += "|" + vo.getTestToAddr().trim();
			}
		}
		return returnPath;
	}
	
	// called from constructor, could not be Autowired
	private ClientDao clientDao = null;
	private ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao)SpringUtil.getDaoAppContext().getBean("clientDao");
		}
		return clientDao;
	}
	
	private MailingListDao mailingListDao = null;
	private MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao)SpringUtil.getDaoAppContext().getBean("mailingListDao");
		}
		return mailingListDao;
	}
	
	private ReloadFlagsDao reloadFlagsDao = null;
	private ReloadFlagsDao getReloadFlagsDao() {
		if (reloadFlagsDao == null) {
			reloadFlagsDao = (ReloadFlagsDao) SpringUtil.getDaoAppContext().getBean("reloadFlagsDao");
		}
		return reloadFlagsDao;
	}
}
