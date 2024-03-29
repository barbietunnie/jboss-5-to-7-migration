package jpa.service.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.constant.Constants;
import jpa.constant.RuleCategory;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleType;
import jpa.constant.XHeaderName;
import jpa.model.ClientData;
import jpa.model.MailingList;
import jpa.model.ReloadFlags;
import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleLogic;
import jpa.model.rule.RuleSubruleMap;
import jpa.service.ClientDataService;
import jpa.service.MailingListService;
import jpa.service.ReloadFlagsService;
import jpa.util.SpringUtil;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleLoader")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public final class RuleLoader implements java.io.Serializable {
	private static final long serialVersionUID = 5251082728950956779L;
	static final Logger logger = Logger.getLogger(RuleLoader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final List<RuleBase>[] mainRules;
	final List<RuleBase>[] preRules;
	final List<RuleBase>[] postRules;
	final HashMap<String, List<RuleBase>>[] subRules;
	
	private int currIndex = 0;
	@Autowired
	private RuleDataService ruleDataService;
	@Autowired
	private ReloadFlagsService flagsService;
	@Autowired
	private ClientDataService clientService;
	@Autowired
	private MailingListService listService;
	
	private int currIndex2 = 0;
	private Map<String, Pattern>[] patternMaps;
	
	private ReloadFlags reloadFlags;
	private long lastTimeLoaded;
	final static int INTERVAL = 5 * 60 * 1000; // 5 minutes

	@SuppressWarnings("unchecked")
	public RuleLoader() {
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
	}
	
	public static void main(String[] args) {
		RuleLoader loader = (RuleLoader) SpringUtil.getAppContext().getBean("ruleLoader");
		SpringUtil.startTransaction();
		try {
			loader.loadRules();
			loader.listRuleNames();
		}
		finally {
			SpringUtil.commitTransaction();
		}
	}

	@SuppressWarnings("unchecked")
	public void loadRules() {
		/*
		 * Two sets of rules are used in turns. When one of the set becomes active set,
		 * another set becomes inactive set. The rules getters always return rules from
		 * the active set. <br>
		 * When this method is called, it reloads rules from database and stores them in
		 * the inactive set. It then switch the inactive set to active before it quits.
		 */
		List<RuleLogic> ruleVos = ruleDataService.getCurrentRules();
		int newIndex = (currIndex + 1) % 2;
		loadRuleSets(ruleVos, newIndex);
		currIndex = newIndex;
		
		/*
		 * define place holder for two sets of pattern maps
		 */
		patternMaps = new LinkedHashMap[2];
		patternMaps[0] = loadAddressPatterns();
		patternMaps[1] = loadAddressPatterns();
		
		lastTimeLoaded = new java.util.Date().getTime();
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
		long currTime = System.currentTimeMillis();
		if (currTime > (lastTimeLoaded + INTERVAL)) {
			// check change flags and reload rule and address patterns
			ReloadFlags vo = flagsService.select();
			if (reloadFlags != null && vo != null) {
				if (reloadFlags.getRules() < vo.getRules()
						|| reloadFlags.getActions() < vo.getActions()
						|| reloadFlags.getTemplates() < vo.getTemplates()) {
					logger.info("====== Rules and/or Actions changed, reload Rules ======");
					reloadFlags.setRules(vo.getRules());
					reloadFlags.setActions(vo.getActions());
					loadRules();
				}
				if (reloadFlags.getClients() < vo.getClients()
						|| reloadFlags.getTemplates() < vo.getTemplates()) {
					logger.info("====== Clients/Templates changed, reload Address Patterns ======");
					reloadFlags.setClients(vo.getClients());
					reloadAddressPatterns();
				}
				reloadFlags.setTemplates(vo.getTemplates());
			}
			lastTimeLoaded = currTime;
		}
	}
	
	private void loadRuleSets(List<RuleLogic> ruleLogics, int index) {
		mainRules[index].clear();
		preRules[index].clear();
		postRules[index].clear();
		subRules[index].clear();
		
		for (int i = 0; i < ruleLogics.size(); i++) {
			RuleLogic logic = ruleLogics.get(i);
			List<RuleBase> rules = createRules(logic);
			if (rules.isEmpty()) {
				continue;
			}
			
			if (RuleCategory.PRE_RULE.getValue().equals(logic.getRuleCategory())) {
				preRules[index].addAll(rules);
			}
			else if (RuleCategory.POST_RULE.getValue().equals(logic.getRuleCategory())) {
				postRules[index].addAll(rules);
			}
			else if (!(logic.isSubrule())) {
				mainRules[index].addAll(rules);
			}
			
			// a non sub-rule could also be used as a sub-rule
			subRules[index].put(logic.getRuleName(), rules);
		}
	}
	
	private List<RuleBase> createRules(RuleLogic ruleLogic) {
		List<RuleBase> rules = new ArrayList<RuleBase>();
		List<RuleElement> elements = ruleLogic.getRuleElements();
		List<RuleSubruleMap> subruleMaps = ruleLogic.getRuleSubruleMaps();
		
		// build rules
		if (RuleType.SIMPLE.getValue().equals(ruleLogic.getRuleType()))	{
			for (int i=0; i<elements.size(); i++) {
				RuleElement element = elements.get(i);
				RuleSimple ruleSimple = new RuleSimple(
					ruleLogic.getRuleName(),
					RuleType.getByValue(ruleLogic.getRuleType()),
					ruleLogic.getMailType(),
					element.getDataName(),
					XHeaderName.getByValue(element.getHeaderName()),
					RuleCriteria.getByValue(element.getCriteria()),
					element.isCaseSensitive(),
					element.getTargetText(),
					element.getExclusions(),
					element.getExclListProcName(),
					element.getDelimiter()
					);
				
				for (int j=0; j<subruleMaps.size(); j++) {
					RuleSubruleMap subRuleLogic = subruleMaps.get(j);
					ruleSimple.subruleList.add(subRuleLogic.getRuleSubruleMapPK().getSubruleLogic().getRuleName());
				}
				
				rules.add(ruleSimple);
			}
		}
		else { // all/any/none rule
			List<RuleBase> ruleList = new ArrayList<RuleBase>();
			for (int i=0; i<elements.size(); i++) {
				RuleElement element = (RuleElement)elements.get(i);
				RuleSimple ruleSimple = new RuleSimple(
					ruleLogic.getRuleName(),
					RuleType.getByValue(ruleLogic.getRuleType()),
					ruleLogic.getMailType(),
					element.getDataName(),
					XHeaderName.getByValue(element.getHeaderName()),
					RuleCriteria.getByValue(element.getCriteria()),
					element.isCaseSensitive(),
					element.getTargetText(),
					element.getExclusions(),
					element.getExclListProcName(),
					element.getDelimiter()
					);
				ruleList.add(ruleSimple);
			}

			RuleComplex ruleComplex = new RuleComplex(
					ruleLogic.getRuleName(),
					RuleType.getByValue(ruleLogic.getRuleType()),
					ruleLogic.getMailType(),
					ruleList
					);
			
			for (int j=0; j<subruleMaps.size(); j++) {
				RuleSubruleMap subruleMap = (RuleSubruleMap)subruleMaps.get(j);
				ruleComplex.subruleList.add(subruleMap.getRuleSubruleMapPK().getSubruleLogic().getRuleName());
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
			listRuleNames("Pre  Rule", getPostRuleSet(), prt);
			listRuleNames("Main Rule", getRuleSet(), prt);
			listRuleNames("Post Rule", getPostRuleSet(), prt);
			listRuleNames("Sub  Rule", subRules[currIndex], prt);
		}
		catch (Exception e) {
			logger.error("Exception caught during ListRuleNames", e);
		}
	}

	private static boolean isPrintRuleContents = true;

	private void listRuleNames(String ruleLit, List<RuleBase> rules, java.io.PrintStream prt) {
		Iterator<RuleBase> it = rules.iterator();
		while (it.hasNext()) {
			RuleBase r = it.next();
			String ruleName = StringUtils.rightPad(r.getRuleName(), 28, " ");
			prt.print("RuleLoader.1 - " + ruleLit + ": " + ruleName);
			if (isPrintRuleContents) {
				prt.print(r.getRuleContent());
				prt.println();
			}
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
				if (isPrintRuleContents) {
					prt.print(r.getRuleContent());
					prt.println();
				}
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
		if (patternMaps == null) {
			throw new IllegalStateException("Rules have not been loaded, please execute loadRules() first.");
		}
		return patternMaps[currIndex2];
	}
	
	private final Map<String, Pattern> loadAddressPatterns() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		// make sure the default client is the first on the list
		ClientData client0 = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		if (client0 != null) {
			String clientId = client0.getClientId();
			String returnPath = buildReturnPath(client0);
			map.put(clientId, returnPath);
		}
		List<ClientData> clients = clientService.getAll();
		// now add all other clients' return path
		for (ClientData client : clients) {
			String clientId = client.getClientId();
			if (Constants.DEFAULT_CLIENTID.equalsIgnoreCase(clientId)) {
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
		List<MailingList> lists = listService.getAll(true);
		for (MailingList list : lists) {
			String clientId = list.getClientData().getClientId();
			String returnPath = list.getListEmailAddr();
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
	
	private String buildReturnPath(ClientData client) {
		String domainName = client.getDomainName().trim();
		String returnPath = client.getReturnPathLeft().trim() + "@" + domainName;
		if (client.isVerpEnabled()) {
			// if VERP is enabled, add VERP addresses to the pattern 
			String verpSub = client.getVerpSubDomain();
			verpSub = (StringUtil.isEmpty(verpSub) ? "" : verpSub.trim() + ".");
			if (!StringUtil.isEmpty(client.getVerpInboxName())) {
				returnPath += "|" + client.getVerpInboxName().trim() + "@" + verpSub + domainName;
			}
			if (!StringUtil.isEmpty(client.getVerpRemoveInbox())) {
				returnPath += "|" + client.getVerpRemoveInbox().trim() + "@" + verpSub + domainName;
			}
		}
		if (client.isUseTestAddr()) {
			// if in test mode, add test address to the pattern
			if (!StringUtil.isEmpty(client.getTestFromAddr())) {
				returnPath += "|" + client.getTestFromAddr().trim();
			}
			if (!StringUtil.isEmpty(client.getTestReplytoAddr())) {
				returnPath += "|" + client.getTestReplytoAddr().trim();
			}
			if (!StringUtil.isEmpty(client.getTestToAddr())) {
				returnPath += "|" + client.getTestToAddr().trim();
			}
		}
		return returnPath;
	}
}
