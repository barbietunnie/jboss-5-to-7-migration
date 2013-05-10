package jpa.msgui.bean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.CodeType;
import jpa.constant.RuleCategory;
import jpa.constant.RuleType;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleElementPK;
import jpa.model.rule.RuleLogic;
import jpa.model.rule.RuleSubruleMap;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.rule.RuleActionService;
import jpa.service.rule.RuleElementService;
import jpa.service.rule.RuleLogicService;
import jpa.service.rule.RuleSubruleMapService;
import jpa.util.BlobUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class MsgRulesBean {
	protected static final Logger logger = Logger.getLogger(MsgRulesBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	protected RuleLogicService ruleLogicDao = null;
	protected DataModel<RuleLogic> ruleLogics = null;
	protected RuleLogic ruleLogic = null;
	protected boolean editMode = true;
	
	protected String testResult = null;
	protected String actionFailure = null;
	
	protected UIInput ruleNameInput = null;
	protected UIInput startDateInput = null;
	
	protected RuleElementService ruleElementDao = null;
	protected RuleSubruleMapService ruleSubRuleMapDao = null;
	protected RuleActionService msgActionDao = null;
	protected DataModel<RuleElement> ruleElements = null;
	protected DataModel<RuleSubruleMap> subRules = null;
	protected DataModel<RuleActionUIVo> msgActions = null;
	
	protected RuleElement ruleElement = null;
	protected RuleElement origRuleElement = null;
	
	protected static final String TO_FAILED = "msgrule.failed";
	protected static final String TO_SELF = "msgrule.toself";
	
	protected RuleLogicService getRuleLogicService() {
		if (ruleLogicDao == null) {
			ruleLogicDao = (RuleLogicService) SpringUtil.getWebAppContext().getBean("ruleLogicService");
		}
		return ruleLogicDao;
	}

	protected RuleElementService getRuleElementService() {
		if (ruleElementDao == null) {
			ruleElementDao = (RuleElementService) SpringUtil.getWebAppContext().getBean(
					"ruleElementService");
		}
		return ruleElementDao;
	}

	protected RuleSubruleMapService getRuleSubruleMapService() {
		if (ruleSubRuleMapDao == null) {
			ruleSubRuleMapDao = (RuleSubruleMapService) SpringUtil.getWebAppContext().getBean(
					"ruleSubruleMapService");
		}
		return ruleSubRuleMapDao;
	}

	protected RuleActionService getRuleActionService() {
		if (msgActionDao == null) {
			msgActionDao = (RuleActionService) SpringUtil.getWebAppContext().getBean("ruleActionService");
		}
		return msgActionDao;
	}

	/*
	 * Main Page Section 
	 */
	
	public DataModel<RuleLogic> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (ruleLogics == null) {
			List<RuleLogic> ruleLogicList = getRuleLogicService().getAll(false);
			ruleLogics = new ListDataModel(ruleLogicList);
		}
		return ruleLogics;
	}

	public String refresh() {
		ruleLogics = null;
		ruleElements = null;
		subRules = null;
		msgActions = null;
		return "";
	}
	
	public String viewRuleLogic() {
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewRuleLogic() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewRuleLogic() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		// clean up
		ruleElements = null;
		startDateInput = null; // so it could be rebound to a new record
		// end of clean up
		this.ruleLogic = (RuleLogic) ruleLogics.getRowData();
		logger.info("viewRuleLogic() - RuleLogic to be edited: " + ruleLogic.getRuleName());
		ruleLogic.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - RuleLogic to be passed to jsp: " + ruleLogic);
		
		return "msgrule.edit";
	}
	
	public String viewSubRules() {
		if (isDebugEnabled)
			logger.debug("viewSubRules() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewSubRules() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewSubRules() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		subRules = null;
		this.ruleLogic = (RuleLogic) ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		return "msgrule.subrule.edit";
	}
	
	public String viewMsgActions() {
		if (isDebugEnabled)
			logger.debug("viewMsgActions() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewMsgActions() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewMsgActions() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		msgActions = null;
		this.ruleLogic = (RuleLogic) ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		return "msgrule.msgaction.edit";
	}
	
	public String viewRuleElement() {
		if (isDebugEnabled)
			logger.debug("viewRuleElement() - Entering...");
		if (ruleElements == null) {
			logger.warn("viewRuleElement() - RuleElement List is null.");
			return TO_FAILED;
		}
		if (!ruleElements.isRowAvailable()) {
			logger.warn("viewRuleElement() - RuleElement Row not available.");
			return TO_FAILED;
		}
		reset();
		origRuleElement = (RuleElement) ruleElements.getRowData();
		ruleElement = (RuleElement) BlobUtil.deepCopy(origRuleElement);
		ruleElement.setMarkedForEdition(true);
		return "msgrule.ruleelement.edit";
	}
	
	public String doneRuleElementEdit() {
		if (isDebugEnabled)
			logger.debug("doneRuleElementEdit() - Entering...");
		if (ruleElement == null) {
			logger.warn("doneRuleElementEdit() - RuleElement is null.");
			return TO_FAILED;
		}
		copyProperties(origRuleElement, ruleElement);
		if (StringUtils.isNotBlank(origRuleElement.getExclusions())) {
			if (StringUtils.isBlank(origRuleElement.getDelimiter())) {
				origRuleElement.setDelimiter(",");
			}
		}
		return "msgrule.ruleelement.done";
	}

	private void copyProperties(RuleElement dest, RuleElement src) {
		RuleElementPK pk = new RuleElementPK();
		pk.setRuleLogic(src.getRuleElementPK().getRuleLogic());
		pk.setElementSequence(src.getRuleElementPK().getElementSequence());
		dest.setRuleElementPK(pk);
		dest.setDataName(src.getDataName());
		dest.setHeaderName(src.getHeaderName());
		dest.setCriteria(src.getCriteria());
		dest.setCaseSensitive(src.isCaseSensitive());
		dest.setTargetText(src.getTargetText());
		dest.setTargetProcName(src.getTargetProcName());
		dest.setExclusions(src.getExclusions());
		dest.setExclListProcName(src.getExclListProcName());
		dest.setDelimiter(src.getDelimiter());
	}
	
	public String saveRuleElement() {
		if (isDebugEnabled)
			logger.debug("saveRuleElement() - Entering...");
		if (ruleElement == null) {
			logger.warn("saveRuleElement() - RuleElement is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		// first delete the rule element
		getRuleElementService().deleteByPrimaryKey(ruleElement.getRuleElementPK());
		// insert the record
		getRuleElementService().insert(ruleElement);
		logger.info("saveRuleElement() - Element Rows Deleted: " + 1);
		return "msgrule.ruleelement.saved";
	}

	public String saveRuleLogic() {
		if (isDebugEnabled)
			logger.debug("saveRuleLogic() - Entering...");
		if (ruleLogic == null) {
			logger.warn("saveRuleLogic() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// set startTime from startDate, startHour and startMinute
		Calendar cal = Calendar.getInstance();
		cal.setTime(ruleLogic.getStartTime());
		ruleLogic.setStartTime(new Timestamp(cal.getTimeInMillis()));
		// end of startTime
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getRuleLogicService().update(ruleLogic);
			logger.info("saveRuleLogic() - Rows Updated: " + 1);
			int rowsDeleted = getRuleElementService().deleteByRuleName(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Deleted: " + rowsDeleted);
			int rowsInserted = insertRuleElements(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Inserted: " + rowsInserted);
		}
		else {
			getRuleLogicService().insert(ruleLogic);
			logger.info("saveRuleLogic() - Rows Inserted: " + 1);
			addToRuleList(ruleLogic);
			int elementsInserted = insertRuleElements(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Inserted: " + elementsInserted);
		}
		return "msgrule.saved";
	}

	protected int insertRuleElements(String _ruleName) {
		List<RuleElement> list = getRuleElementList();
		int rowsInserted = 0;
		for (int i=0; i<list.size(); i++) {
			RuleElement ruleElementVo = list.get(i);
			RuleElement vo = new RuleElement();
			ruleElementVo.copyPropertiesTo(vo);
			RuleElementPK pk = vo.getRuleElementPK();
			RuleLogic ruleLogic = getRuleLogicService().getByRuleName(_ruleName);
			pk.setRuleLogic(ruleLogic);
			pk.setElementSequence(i);
			getRuleElementService().insert(vo);
		}
		return rowsInserted;
	}
	
	@SuppressWarnings("unchecked")
	protected void addToRuleList(RuleLogic vo) {
		List<RuleLogic> list = (List<RuleLogic>) ruleLogics.getWrappedData();
		list.add(vo);
	}
	
	public String deleteRuleLogics() {
		if (isDebugEnabled)
			logger.debug("deleteRuleLogics() - Entering...");
		if (ruleLogics == null) {
			logger.warn("deleteRuleLogics() - RuleLogic List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleLogic> list = getRuleLogicList();
		for (int i = 0; i < list.size(); i++) {
			RuleLogic vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleLogicService().deleteByRuleName(vo.getRuleName());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleLogics() - RuleLogic deleted: " + vo.getRuleName());
				}
				list.remove(vo);
			}
		}
		return "msgrule.deleted";
	}
	
	public String testRuleLogic() {
		if (isDebugEnabled)
			logger.debug("testRuleLogic() - Entering...");
		if (ruleLogic == null) {
			logger.warn("testRuleLogic() - RuleLogic is null.");
			return TO_FAILED;
		}
		return TO_SELF;
	}
	
	public String copyRuleLogic() {
		if (isDebugEnabled)
			logger.debug("copyRuleLogic() - Entering...");
		if (ruleLogics == null) {
			logger.warn("copyRuleLogic() - RuleLogic List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleLogic> list = getRuleLogicList();
		for (int i=0; i<list.size(); i++) {
			RuleLogic vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				this.ruleLogic = new RuleLogic();
				try {
					vo.copyPropertiesTo(this.ruleLogic);
					this.ruleLogic.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				ruleElements = null;
				getRuleElements();
				// set processor fields to null as they are invisible from UI
				List<RuleElement> elements = getRuleElementList();
				for (RuleElement element : elements) {
					element.setTargetProcName(null);
					element.setExclListProcName(null);
				}
				// end of null
				ruleElements.getWrappedData();
				ruleLogic.setRuleName(null);
				ruleLogic.setEvalSequence(getRuleLogicService().getNextEvalSequence());
				ruleLogic.setRuleType(RuleType.SIMPLE.getValue());
				ruleLogic.setMarkedForEdition(true);
				editMode = false;
				return "msgrule.edit";
			}
		}
		return TO_SELF;
	}
	
	public String addRuleLogic() {
		if (isDebugEnabled)
			logger.debug("addRuleLogic() - Entering...");
		reset();
		ruleElements = null;
		this.ruleLogic = new RuleLogic();
		ruleLogic.setMarkedForEdition(true);
		//ruleLogic.setUpdtUserId(Constants.DEFAULT_USER_ID);
		ruleLogic.setEvalSequence(getRuleLogicService().getNextEvalSequence());
		ruleLogic.setRuleType(RuleType.SIMPLE.getValue());
		ruleLogic.setRuleCategory(RuleCategory.MAIN_RULE.getValue());
		ruleLogic.setStartTime(new Timestamp(new java.util.Date().getTime()));
		editMode = false;
		return "msgrule.edit";
	}
	
	public String cancelEdit() {
		if (isDebugEnabled)
			logger.debug("cancelEdit() - Entering...");
		refresh();
		return "msgrule.canceled";
	}
	
	public boolean getCanMoveUp() {
		if (ruleLogics == null || !ruleLogics.isRowAvailable()) {
			return false;
		}
		RuleLogic vo = (RuleLogic) ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx > 0) {
			RuleLogic up = getRuleLogicList().get(idx - 1);
			if (vo.getRuleCategory().equals(up.getRuleCategory())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getCanMoveDown() {
		if (ruleLogics == null || !ruleLogics.isRowAvailable()) {
			return false;
		}
		RuleLogic vo = (RuleLogic) ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx < (ruleLogics.getRowCount() + 1)) {
			RuleLogic down = getRuleLogicList().get(idx + 1);
			if (vo.getRuleCategory().equals(down.getRuleCategory())) {
				return true;
			}
		}
		return false;
	}
	
	public String moveUp() {
		if (isDebugEnabled)
			logger.debug("moveUp() - Entering...");
		moveUpDownRule(-1);
		return TO_SELF;
	}
	
	public String moveDown() {
		if (isDebugEnabled)
			logger.debug("moveDown() - Entering...");
		moveUpDownRule(1);
		return TO_SELF;
	}
	
	/*
	 * @param updown - move current rule up or down:
	 * 		-1 -> move up
	 * 		+1 -> move down
	 */
	protected void moveUpDownRule(int updown) {
		reset();
		RuleLogic currVo = (RuleLogic) getAll().getRowData();
		int index = ruleLogics.getRowIndex();
		List<RuleLogic> list = getRuleLogicList();
		RuleLogic prevVo = list.get(index + updown);
		if (currVo.getRuleCategory().equals(prevVo.getRuleCategory())) {
			int currSeq = currVo.getEvalSequence();
			int prevSeq = prevVo.getEvalSequence();
			currVo.setEvalSequence(prevSeq);
			prevVo.setEvalSequence(currSeq);
			getRuleLogicService().update(currVo);
			getRuleLogicService().update(prevVo);
			refresh();
		}
	}
	
	/*
	 * Rule Elements Section
	 */
	
	public String refreshElements() {
		ruleElements = null;
		getRuleElements();
		return "";
	}
	
	public DataModel<RuleElement> getRuleElements() {
		if (isDebugEnabled)
			logger.debug("getRuleElement() - Entering...");
		if (ruleLogic == null) {
			logger.warn("getRuleElements() - RuleLogic is null.");
			return null;
		}
		if (ruleElements == null) {
			String key = ruleLogic.getRuleName();
			List<RuleElement> list = getRuleElementService().getByRuleName(key);
			ruleElements = new ListDataModel(list);
		}
		return ruleElements;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleElement> getRuleElementList() {
		List<RuleElement> list = (List<RuleElement>) getRuleElements().getWrappedData();
		return list;
	}
	
	public String deleteRuleElements() {
		if (isDebugEnabled)
			logger.debug("deleteRuleElements() - Entering...");
		if (ruleElements == null) {
			logger.warn("deleteRuleElements() - RuleElement List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleElement> list = getRuleElementList();
		for (int i = 0; i < list.size(); i++) {
			RuleElement vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleElementService().deleteByPrimaryKey(vo.getRuleElementPK());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleElements() - RuleElement deleted: " + vo.getRuleElementPK());
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}
	
	public String copyRuleElement() {
		if (isDebugEnabled)
			logger.debug("copyRuleElement() - Entering...");
		if (ruleElements == null) {
			logger.warn("copyRuleElement() - RuleElement List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleElement> list = getRuleElementList();
		for (int i=0; i<list.size(); i++) {
			RuleElement vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleElement vo2 = new RuleElement();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.getRuleElementPK().setElementSequence(getNextRuleElementSeq());
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public String addRuleElement() {
		if (isDebugEnabled)
			logger.debug("addRuleElement() - Entering...");
		reset();
		List<RuleElement> list = getRuleElementList();
		RuleElement vo = new RuleElement();
		vo.getRuleElementPK().setElementSequence(getNextRuleElementSeq());
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	private int getNextRuleElementSeq() {
		List<RuleElement> list = getRuleElementList();
		if (list == null || list.isEmpty()) {
			return 0;
		}
		else {
			int seq = list.size() - 1;
			for (RuleElement vo : list) { // just for safety
				if (vo.getRuleElementPK().getElementSequence() > seq) {
					seq = vo.getRuleElementPK().getElementSequence();
				}
			}
			return seq + 1;
		}
	}
	
	/*
	 * Sub-Rules Section
	 */
	
	public String refreshSubRules() {
		subRules = null;
		getSubRules();
		return "";
	}
	
	public DataModel<RuleSubruleMap> getSubRules() {
		if (isDebugEnabled)
			logger.debug("getSubRules() - Entering...");
		if (ruleLogic == null) {
			logger.warn("getSubRules() - RuleLogic is null.");
			return null;
		}
		if (subRules == null) {
			String key = ruleLogic.getRuleName();
			List<RuleSubruleMap> list = getRuleSubruleMapService().getByRuleName(key);
			subRules = new ListDataModel(list);
		}
		return subRules;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleSubruleMap> getSubRuleList() {
		List<RuleSubruleMap> list = (List<RuleSubruleMap>) getSubRules().getWrappedData();
		return list;
	}
	
	public String deleteSubRules() {
		if (isDebugEnabled)
			logger.debug("deleteSubRules() - Entering...");
		if (subRules == null) {
			logger.warn("deleteSubRules() - SubRule List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleSubruleMap> list = getSubRuleList();
		for (int i = 0; i < list.size(); i++) {
			RuleSubruleMap vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleSubruleMapService().deleteByPrimaryKey(vo.getRuleSubruleMapPK());
				if (rowsDeleted > 0) {
					logger.info("deleteSubRules() - SubRule deleted: " + vo.getRuleSubruleMapPK());
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}

	public String copySubRule() {
		if (isDebugEnabled)
			logger.debug("copySubRule() - Entering...");
		if (subRules == null) {
			logger.warn("copySubRule() - SubRule List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleSubruleMap> list = getSubRuleList();
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleSubruleMap vo2 = new RuleSubruleMap();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public String addSubRule() {
		if (isDebugEnabled)
			logger.debug("addSubRule() - Entering...");
		reset();
		List<RuleSubruleMap> list = getSubRuleList();
		RuleSubruleMap vo = new RuleSubruleMap();
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	public String saveSubRules() {
		if (isDebugEnabled)
			logger.debug("saveSubRules() - Entering...");
		if (ruleLogic == null) {
			logger.warn("saveSubRules() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getRuleSubruleMapService().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveSubRules() - SubRule Rows Deleted: " + rowsDeleted);
		
		List<RuleSubruleMap> list = getSubRuleList();
		if (hasDuplicateSubRules(list)) {
			testResult = "duplicateSubRuleFound";
			/* Add to Face message queue. Not working. */
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", testResult, null);
			FacesContext.getCurrentInstance().addMessage(null, message);
			return null;
		}
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap ruleSubRuleMapVo = list.get(i);
			ruleSubRuleMapVo.getRuleSubruleMapPK().setRuleLogic(ruleLogic);
			ruleSubRuleMapVo.setSubruleSequence(i);
			getRuleSubruleMapService().insert(ruleSubRuleMapVo);
		}
		logger.info("saveSubRules() - SubRule Rows Inserted: " + list.size());
		return "msgrule.saved";
	}

	public String moveUpSubRule() {
		if (isDebugEnabled)
			logger.debug("moveUpSubRule() - Entering...");
		moveSubRule(-1);
		return TO_SELF;
	}
	
	public String moveDownSubRule() {
		if (isDebugEnabled)
			logger.debug("moveDownSubRule() - Entering...");
		moveSubRule(1);
		return TO_SELF;
	}
	
	protected boolean hasDuplicateSubRules(List<RuleSubruleMap> list) {
		if (list == null || list.size() <= 1)
			return false;
		
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap vo = list.get(i);
			for (int j=i+1; j<list.size(); j++) {
				RuleSubruleMap vo2 = list.get(j);
				if (vo.getRuleSubruleMapPK().getSubruleLogic().getRuleName().equals(
						vo2.getRuleSubruleMapPK().getSubruleLogic().getRuleName()))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * @param updown - move current sub-rule up or down:
	 * 		-1 -> move up
	 * 		+1 -> move down
	 */
	protected void moveSubRule(int updown) {
		reset();
		RuleSubruleMap currVo = (RuleSubruleMap) getSubRules().getRowData();
		int index = subRules.getRowIndex();
		List<RuleSubruleMap> list = getSubRuleList();
		RuleSubruleMap prevVo = list.get(index + updown);
		int currSeq = currVo.getSubruleSequence();
		int prevSeq = prevVo.getSubruleSequence();
		currVo.setSubruleSequence(prevSeq);
		prevVo.setSubruleSequence(currSeq);
		getRuleSubruleMapService().update(currVo);
		getRuleSubruleMapService().update(prevVo);
		refreshSubRules();
	}
	
	/*
	 * Message Actions Section
	 */
	
	public String refreshMsgActions() {
		msgActions = null;
		getMsgActions();
		return "";
	}
	
	public DataModel<RuleActionUIVo> getMsgActions() {
		if (isDebugEnabled)
			logger.debug("getMsgActions() - Entering...");
		if (ruleLogic == null) {
			logger.warn("getMsgActions() - RuleLogic is null.");
			return null;
		}
		if (msgActions == null) {
			String key = ruleLogic.getRuleName();
			List<RuleAction> list = getRuleActionService().getByRuleName(key);
			List<RuleActionUIVo> list2 = new ArrayList<RuleActionUIVo>();
			for (int i=0; i<list.size(); i++) {
				RuleActionUIVo vo2 = new RuleActionUIVo(list.get(i));
				list2.add(vo2);
			}
			msgActions = new ListDataModel(list2);
		}
		return msgActions;
	}
	
	protected List<RuleActionUIVo> getMsgActionList() {
		List<RuleActionUIVo> list = (List<RuleActionUIVo>) getMsgActions().getWrappedData();
		return list;
	}
	
	public String deleteMsgActions() {
		if (isDebugEnabled)
			logger.debug("deleteMsgActions() - Entering...");
		if (msgActions == null) {
			logger.warn("deleteMsgActions() - MsgAction List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleActionUIVo> list = getMsgActionList();
		for (int i = 0; i < list.size(); i++) {
			RuleActionUIVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleActionService().deleteByPrimaryKey(vo.getRuleAction().getRuleActionPK());
				if (rowsDeleted > 0) {
					logger.info("deleteMsgActions() - MsgAction deleted: " + vo.getRuleName() + "."
							+ vo.getActionSeq() + "." + vo.getStartTime() + "." + vo.getSenderId());
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}

	public String copyMsgAction() {
		if (isDebugEnabled)
			logger.debug("copyMsgAction() - Entering...");
		if (msgActions == null) {
			logger.warn("copyMsgAction() - MsgAction List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleActionUIVo> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			RuleActionUIVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleActionUIVo vo2 = null;
				try {
					vo2 = (RuleActionUIVo) vo.getClone();
					vo2.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					vo2 = new RuleActionUIVo(new RuleAction());
					vo2.setRuleName(vo.getRuleName());
					vo2.setActionSeq(vo.getActionSeq());
					vo2.setStartTime(vo.getStartTime());
					vo2.setActionId(vo.getActionId());
				}
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public String addMsgAction() {
		if (isDebugEnabled)
			logger.debug("addMsgAction() - Entering...");
		reset();
		List<RuleActionUIVo> list = getMsgActionList();
		RuleActionUIVo vo = new RuleActionUIVo(new RuleAction());
		vo.setActionSeq(0);
		vo.setStartTime(new Timestamp(new Date().getTime()));
		vo.setStatusId(CodeType.YES_CODE.getValue());
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	public String saveMsgActions() {
		if (isDebugEnabled)
			logger.debug("saveMsgActions() - Entering...");
		if (ruleLogic == null) {
			logger.warn("saveMsgActions() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getRuleActionService().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveMsgActions() - MsgAction Rows Deleted: " + rowsDeleted);
		
		List<RuleActionUIVo> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			RuleActionUIVo msgActionUIVo = list.get(i);
			msgActionUIVo.setRuleName(ruleLogic.getRuleName());
			// set startTime from startDate and startHour
			Calendar cal = Calendar.getInstance();
			cal.setTime(msgActionUIVo.getStartDate());
			cal.set(Calendar.HOUR_OF_DAY, msgActionUIVo.getStartHour());
			msgActionUIVo.setStartTime(new Timestamp(cal.getTimeInMillis()));
			// end of startTime
			RuleAction msgActionVo = msgActionUIVo.getRuleAction();
			getRuleActionService().insert(msgActionVo);
		}
		logger.info("saveMsgActions() - MsgAction Rows Inserted: " + list.size());
		return "msgrule.saved";
	}

	/*
	 * Logic Evaluation Section 
	 */
	
	public boolean getAnyRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyRulesMarkedForDeletion() - Entering...");
		List<RuleLogic> list = getRuleLogicList();
		for (Iterator<RuleLogic> it=list.iterator(); it.hasNext();) {
			RuleLogic vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyElementsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyElementsMarkedForDeletion() - Entering...");
		List<RuleElement> list = getRuleElementList();
		for (Iterator<RuleElement> it=list.iterator(); it.hasNext();) {
			RuleElement vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnySubRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySubRulesMarkedForDeletion() - Entering...");
		List<RuleSubruleMap> list = getSubRuleList();
		for (Iterator<RuleSubruleMap> it=list.iterator(); it.hasNext();) {
			RuleSubruleMap vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyMsgActionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMsgActionsMarkedForDeletion() - Entering...");
		List<RuleActionUIVo> list = getMsgActionList();
		for (Iterator<RuleActionUIVo> it=list.iterator(); it.hasNext();) {
			RuleActionUIVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getHasSubRules() {
		if (isDebugEnabled)
			logger.debug("getHasSubRules() - Entering...");
		if (ruleLogic != null) {
			List<RuleSubruleMap> list = getRuleSubruleMapService().getByRuleName(
					ruleLogic.getRuleName());
			if (list.size() > 0)
				return true;
		}
		return false;
	}

	public boolean getHasMsgActions() {
		if (isDebugEnabled)
			logger.debug("getHasMsgActions() - Entering...");
		if (ruleLogics != null) {
			RuleLogic vo = (RuleLogic) ruleLogics.getRowData();
			List<RuleAction> list = getRuleActionService().getByRuleName(vo.getRuleName());
			if (list.size() > 0)
				return true;
		}
		return false;
	}

	/*
	 * Validation Section
	 */
	
	/**
	 * check primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - ruleName: " + value);
		String ruleName = (String) value;
		if (ruleLogic != null) {
			int seq = ruleLogic.getEvalSequence();
			logger.debug("RuleLogic sequence: " + seq);
		}
		else {
			logger.error("validatePrimaryKey(() - RuleLogic is null");
			return;
		}
		RuleLogic vo = getRuleLogicService().getByRuleName(ruleName);
		if (editMode == true && vo == null) {
			// ruleLogic does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "ruleLogicDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// ruleLogic already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "ruleLogicAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * check regular expression
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateRegex(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateRegex() - regex: " + value);
		String regex = (String) value;
		try {
			Pattern.compile(regex);
		}
		catch (PatternSyntaxException e) {
			FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "invalidRegex", null);
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			//context.addMessage(component.getClientId(context), message);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * check start date
	 * 
	 * @param context
	 * @param component
	 * @param value
	 */
	public void checkStartDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("checkStartDate() - startDate = " + value);
		if (value instanceof Date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime((Date)value);
			return;
		}
		((UIInput)component).setValid(false);
		FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
				"com.legacytojava.msgui.messages", "invalidDate", null);
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		context.addMessage(component.getClientId(context), message);
	}
	
	/*
	 * "testResult" and "actionFailure" are messages generated by action
	 * commands, set them to null so they will no longer be rendered.
	 */
	void reset() {
		testResult = null;
		actionFailure = null;
		ruleNameInput = null;
		startDateInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	protected List<RuleLogic> getRuleLogicList() {
		if (ruleLogics == null) {
			return new ArrayList<RuleLogic>();
		}
		else {
			return (List<RuleLogic>)ruleLogics.getWrappedData();
		}
	}
	
	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getRuleNameInput() {
		return ruleNameInput;
	}

	public void setRuleNameInput(UIInput smtpHostInput) {
		this.ruleNameInput = smtpHostInput;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public DataModel<RuleLogic> getRuleLogics() {
		return ruleLogics;
	}

	public UIInput getStartDateInput() {
		return startDateInput;
	}

	public void setStartDateInput(UIInput startDateInput) {
		this.startDateInput = startDateInput;
	}

	public RuleElement getRuleElement() {
		return ruleElement;
	}

	public void setRuleElement(RuleElement ruleElement) {
		this.ruleElement = ruleElement;
	}
}