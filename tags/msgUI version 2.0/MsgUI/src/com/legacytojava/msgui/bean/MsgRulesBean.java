package com.legacytojava.msgui.bean;

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

import org.apache.log4j.Logger;

import com.legacytojava.message.bo.rule.RuleBase;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.action.MsgActionDao;
import com.legacytojava.message.dao.rule.RuleElementDao;
import com.legacytojava.message.dao.rule.RuleLogicDao;
import com.legacytojava.message.dao.rule.RuleSubRuleMapDao;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.action.MsgActionVo;
import com.legacytojava.message.vo.rule.RuleElementVo;
import com.legacytojava.message.vo.rule.RuleLogicVo;
import com.legacytojava.message.vo.rule.RuleSubRuleMapVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;
import com.legacytojava.msgui.vo.MsgActionUIVo;

public class MsgRulesBean {
	protected static final Logger logger = Logger.getLogger(MsgRulesBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	protected RuleLogicDao ruleLogicDao = null;
	protected DataModel ruleLogics = null;
	protected RuleLogicVo ruleLogic = null;
	protected boolean editMode = true;
	
	protected String testResult = null;
	protected String actionFailure = null;
	
	protected UIInput ruleNameInput = null;
	protected UIInput startDateInput = null;
	
	protected RuleElementDao ruleElementDao = null;
	protected RuleSubRuleMapDao ruleSubRuleMapDao = null;
	protected MsgActionDao msgActionDao = null;
	protected DataModel ruleElements = null;
	protected DataModel subRules = null;
	protected DataModel msgActions = null;
	
	protected RuleElementVo ruleElement = null;
	protected RuleElementVo origRuleElement = null;
	
	protected static final String TO_FAILED = "msgrule.failed";
	protected static final String TO_SELF = "msgrule.toself";
	
	protected RuleLogicDao getRuleLogicDao() {
		if (ruleLogicDao == null) {
			ruleLogicDao = (RuleLogicDao) SpringUtil.getWebAppContext().getBean("ruleLogicDao");
		}
		return ruleLogicDao;
	}

	protected RuleElementDao getRuleElementDao() {
		if (ruleElementDao == null) {
			ruleElementDao = (RuleElementDao) SpringUtil.getWebAppContext().getBean(
					"ruleElementDao");
		}
		return ruleElementDao;
	}

	protected RuleSubRuleMapDao getRuleSubRuleMapDao() {
		if (ruleSubRuleMapDao == null) {
			ruleSubRuleMapDao = (RuleSubRuleMapDao) SpringUtil.getWebAppContext().getBean(
					"ruleSubRuleMapDao");
		}
		return ruleSubRuleMapDao;
	}

	protected MsgActionDao getMsgActionDao() {
		if (msgActionDao == null) {
			msgActionDao = (MsgActionDao) SpringUtil.getWebAppContext().getBean("msgActionDao");
		}
		return msgActionDao;
	}

	/*
	 * Main Page Section 
	 */
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (ruleLogics == null) {
			List<RuleLogicVo> ruleLogicList = getRuleLogicDao().getAll(false);
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
		this.ruleLogic = (RuleLogicVo) ruleLogics.getRowData();
		logger.info("viewRuleLogic() - RuleLogic to be edited: " + ruleLogic.getRuleName());
		ruleLogic.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - RuleLogicVo to be passed to jsp: " + ruleLogic);
		
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
		this.ruleLogic = (RuleLogicVo) ruleLogics.getRowData();
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
		this.ruleLogic = (RuleLogicVo) ruleLogics.getRowData();
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
		origRuleElement = (RuleElementVo) ruleElements.getRowData();
		ruleElement = (RuleElementVo) BlobUtil.deepCopy(origRuleElement);
		ruleElement.setMarkedForEdition(true);
		return "msgrule.ruleelement.edit";
	}
	
	public String doneRuleElementEdit() {
		if (isDebugEnabled)
			logger.debug("doneRuleElementEdit() - Entering...");
		if (ruleElement == null) {
			logger.warn("doneRuleElementEdit() - RuleElementVo is null.");
			return TO_FAILED;
		}
		copyProperties(origRuleElement, ruleElement);
		if (!StringUtil.isEmpty(origRuleElement.getExclusions())) {
			if (StringUtil.isEmpty(origRuleElement.getDelimiter())) {
				origRuleElement.setDelimiter(",");
			}
		}
		return "msgrule.ruleelement.done";
	}

	private void copyProperties(RuleElementVo dest, RuleElementVo src) {
		dest.setRuleName(src.getRuleName());
		dest.setElementSeq(src.getElementSeq());
		dest.setDataName(src.getDataName());
		dest.setHeaderName(src.getHeaderName());
		dest.setCriteria(src.getCriteria());
		dest.setCaseSensitive(src.getCaseSensitive());
		dest.setTargetText(src.getTargetText());
		dest.setTargetProc(src.getTargetProc());
		dest.setExclusions(src.getExclusions());
		dest.setExclListProc(src.getExclListProc());
		dest.setDelimiter(src.getDelimiter());
	}
	
	public String saveRuleElement() {
		if (isDebugEnabled)
			logger.debug("saveRuleElement() - Entering...");
		if (ruleElement == null) {
			logger.warn("saveRuleElement() - RuleElementVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		// first delete the rule element
		getRuleElementDao().deleteByPrimaryKey(ruleElement.getRuleName(),
				ruleElement.getElementSeq());
		// insert the record
		int rowsInserted = getRuleElementDao().insert(ruleElement);
		logger.info("saveRuleElement() - Element Rows Deleted: " + rowsInserted);
		return "msgrule.ruleelement.saved";
	}

	public String saveRuleLogic() {
		if (isDebugEnabled)
			logger.debug("saveRuleLogic() - Entering...");
		if (ruleLogic == null) {
			logger.warn("saveRuleLogic() - RuleLogicVo is null.");
			return TO_FAILED;
		}
		reset();
		// set startTime from startDate, startHour and startMinute
		Calendar cal = Calendar.getInstance();
		cal.setTime(ruleLogic.getStartDate());
		cal.set(Calendar.HOUR_OF_DAY, ruleLogic.getStartHour());
		cal.set(Calendar.MINUTE, ruleLogic.getStartMinute());
		ruleLogic.setStartTime(new Timestamp(cal.getTimeInMillis()));
		// end of startTime
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getRuleLogicDao().update(ruleLogic);
			logger.info("saveRuleLogic() - Rows Updated: " + rowsUpdated);
			int rowsDeleted = getRuleElementDao().deleteByRuleName(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Deleted: " + rowsDeleted);
			int rowsInserted = insertRuleElements(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Inserted: " + rowsInserted);
		}
		else {
			int rowsInserted = getRuleLogicDao().insert(ruleLogic);
			logger.info("saveRuleLogic() - Rows Inserted: " + rowsInserted);
			if (rowsInserted > 0) {
				addToRuleList(ruleLogic);
			}
			int elementsInserted = insertRuleElements(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Inserted: " + elementsInserted);
		}
		return "msgrule.saved";
	}

	protected int insertRuleElements(String _ruleName) {
		List<RuleElementVo> list = getRuleElementList();
		int rowsInserted = 0;
		for (int i=0; i<list.size(); i++) {
			RuleElementVo ruleElementVo = list.get(i);
			ruleElementVo.setRuleName(_ruleName);
			ruleElementVo.setElementSeq(i);
			rowsInserted += getRuleElementDao().insert(ruleElementVo);
		}
		return rowsInserted;
	}
	
	@SuppressWarnings("unchecked")
	protected void addToRuleList(RuleLogicVo vo) {
		List<RuleLogicVo> list = (List<RuleLogicVo>) ruleLogics.getWrappedData();
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
		List<RuleLogicVo> list = getRuleLogicList();
		for (int i = 0; i < list.size(); i++) {
			RuleLogicVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleLogicDao().deleteByPrimaryKey(vo.getRuleName(), vo.getRuleSeq());
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
			logger.warn("testRuleLogic() - RuleLogicVo is null.");
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
		List<RuleLogicVo> list = getRuleLogicList();
		for (int i=0; i<list.size(); i++) {
			RuleLogicVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.ruleLogic = (RuleLogicVo) vo.getClone();
					this.ruleLogic.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.ruleLogic = new RuleLogicVo();
				}
				ruleElements = null;
				getRuleElements();
				// set processor fields to null as they are invisible from UI
				List<RuleElementVo> elements = getRuleElementList();
				for (RuleElementVo element : elements) {
					element.setTargetProc(null);
					element.setExclListProc(null);
				}
				// end of null
				ruleElements.getWrappedData();
				ruleLogic.setRuleName(null);
				ruleLogic.setRuleSeq(getRuleLogicDao().getNextRuleSequence());
				ruleLogic.setRuleType(RuleBase.SIMPLE_RULE);
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
		this.ruleLogic = new RuleLogicVo();
		ruleLogic.setMarkedForEdition(true);
		//ruleLogic.setUpdtUserId(Constants.DEFAULT_USER_ID);
		ruleLogic.setRuleSeq(getRuleLogicDao().getNextRuleSequence());
		ruleLogic.setRuleType(RuleBase.SIMPLE_RULE);
		ruleLogic.setRuleCategory(RuleBase.MAIN_RULE);
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
		RuleLogicVo vo = (RuleLogicVo) ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx > 0) {
			RuleLogicVo up = getRuleLogicList().get(idx - 1);
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
		RuleLogicVo vo = (RuleLogicVo) ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx < (ruleLogics.getRowCount() + 1)) {
			RuleLogicVo down = getRuleLogicList().get(idx + 1);
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
		RuleLogicVo currVo = (RuleLogicVo) getAll().getRowData();
		int index = ruleLogics.getRowIndex();
		List<RuleLogicVo> list = getRuleLogicList();
		RuleLogicVo prevVo = list.get(index + updown);
		if (currVo.getRuleCategory().equals(prevVo.getRuleCategory())) {
			int currSeq = currVo.getRuleSeq();
			int prevSeq = prevVo.getRuleSeq();
			currVo.setRuleSeq(prevSeq);
			prevVo.setRuleSeq(currSeq);
			getRuleLogicDao().update(currVo);
			getRuleLogicDao().update(prevVo);
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
	
	public DataModel getRuleElements() {
		if (isDebugEnabled)
			logger.debug("getRuleElement() - Entering...");
		if (ruleLogic == null) {
			logger.warn("getRuleElements() - RuleLogicVo is null.");
			return null;
		}
		if (ruleElements == null) {
			String key = ruleLogic.getRuleName();
			List<RuleElementVo> list = getRuleElementDao().getByRuleName(key);
			ruleElements = new ListDataModel(list);
		}
		return ruleElements;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleElementVo> getRuleElementList() {
		List<RuleElementVo> list = (List<RuleElementVo>) getRuleElements().getWrappedData();
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
		List<RuleElementVo> list = getRuleElementList();
		for (int i = 0; i < list.size(); i++) {
			RuleElementVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleElementDao().deleteByPrimaryKey(vo.getRuleName(),
						vo.getElementSeq());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleElements() - RuleElement deleted: " + vo.getRuleName()
							+ ":" + vo.getElementSeq());
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
		List<RuleElementVo> list = getRuleElementList();
		for (int i=0; i<list.size(); i++) {
			RuleElementVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleElementVo vo2 = null;
				try {
					vo2 = (RuleElementVo) vo.getClone();
					vo2.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					vo2 = new RuleElementVo();
					vo2.setRuleName(vo.getRuleName());
				}
				vo2.setElementSeq(getNextRuleElementSeq());
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
		List<RuleElementVo> list = getRuleElementList();
		RuleElementVo vo = new RuleElementVo();
		vo.setElementSeq(getNextRuleElementSeq());
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	private int getNextRuleElementSeq() {
		List<RuleElementVo> list = getRuleElementList();
		if (list == null || list.isEmpty()) {
			return 0;
		}
		else {
			int seq = list.size() - 1;
			for (RuleElementVo vo : list) { // just for safety
				if (vo.getElementSeq() > seq) {
					seq = vo.getElementSeq();
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
	
	public DataModel getSubRules() {
		if (isDebugEnabled)
			logger.debug("getSubRules() - Entering...");
		if (ruleLogic == null) {
			logger.warn("getSubRules() - RuleLogicVo is null.");
			return null;
		}
		if (subRules == null) {
			String key = ruleLogic.getRuleName();
			List<RuleSubRuleMapVo> list = getRuleSubRuleMapDao().getByRuleName(key);
			subRules = new ListDataModel(list);
		}
		return subRules;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleSubRuleMapVo> getSubRuleList() {
		List<RuleSubRuleMapVo> list = (List<RuleSubRuleMapVo>) getSubRules().getWrappedData();
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
		List<RuleSubRuleMapVo> list = getSubRuleList();
		for (int i = 0; i < list.size(); i++) {
			RuleSubRuleMapVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleSubRuleMapDao().deleteByPrimaryKey(vo.getRuleName(),
						vo.getSubRuleName());
				if (rowsDeleted > 0) {
					logger.info("deleteSubRules() - SubRule deleted: " + vo.getRuleName()
							+ ":" + vo.getSubRuleName());
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
		List<RuleSubRuleMapVo> list = getSubRuleList();
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleSubRuleMapVo vo2 = null;
				try {
					vo2 = (RuleSubRuleMapVo) vo.getClone();
					vo2.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					vo2 = new RuleSubRuleMapVo();
					vo2.setRuleName(vo.getRuleName());
					vo2.setSubRuleName(vo.getSubRuleName());
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
		List<RuleSubRuleMapVo> list = getSubRuleList();
		RuleSubRuleMapVo vo = new RuleSubRuleMapVo();
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	public String saveSubRules() {
		if (isDebugEnabled)
			logger.debug("saveSubRules() - Entering...");
		if (ruleLogic == null) {
			logger.warn("saveSubRules() - RuleLogicVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getRuleSubRuleMapDao().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveSubRules() - SubRule Rows Deleted: " + rowsDeleted);
		
		List<RuleSubRuleMapVo> list = getSubRuleList();
		if (hasDuplicateSubRules(list)) {
			testResult = "duplicateSubRuleFound";
			/* Add to Face message queue. Not working. */
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", testResult, null);
			FacesContext.getCurrentInstance().addMessage(null, message);
			return null;
		}
		int rowsInserted = 0;
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo ruleSubRuleMapVo = list.get(i);
			ruleSubRuleMapVo.setRuleName(ruleLogic.getRuleName());
			ruleSubRuleMapVo.setSubRuleSeq(i);
			rowsInserted += getRuleSubRuleMapDao().insert(ruleSubRuleMapVo);
		}
		logger.info("saveSubRules() - SubRule Rows Inserted: " + rowsInserted);
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
	
	protected boolean hasDuplicateSubRules(List<RuleSubRuleMapVo> list) {
		if (list == null || list.size() <= 1)
			return false;
		
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo vo = list.get(i);
			for (int j=i+1; j<list.size(); j++) {
				RuleSubRuleMapVo vo2 = list.get(j);
				if (vo.getSubRuleName().equals(vo2.getSubRuleName()))
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
		RuleSubRuleMapVo currVo = (RuleSubRuleMapVo) getSubRules().getRowData();
		int index = subRules.getRowIndex();
		List<RuleSubRuleMapVo> list = getSubRuleList();
		RuleSubRuleMapVo prevVo = list.get(index + updown);
		int currSeq = currVo.getSubRuleSeq();
		int prevSeq = prevVo.getSubRuleSeq();
		currVo.setSubRuleSeq(prevSeq);
		prevVo.setSubRuleSeq(currSeq);
		getRuleSubRuleMapDao().update(currVo);
		getRuleSubRuleMapDao().update(prevVo);
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
	
	public DataModel getMsgActions() {
		if (isDebugEnabled)
			logger.debug("getMsgActions() - Entering...");
		if (ruleLogic == null) {
			logger.warn("getMsgActions() - RuleLogicVo is null.");
			return null;
		}
		if (msgActions == null) {
			String key = ruleLogic.getRuleName();
			List<MsgActionVo> list = getMsgActionDao().getByRuleName(key);
			List<MsgActionUIVo> list2 = new ArrayList<MsgActionUIVo>();
			for (int i=0; i<list.size(); i++) {
				MsgActionUIVo vo2 = new MsgActionUIVo(list.get(i));
				list2.add(vo2);
			}
			msgActions = new ListDataModel(list2);
		}
		return msgActions;
	}
	
	@SuppressWarnings("unchecked")
	protected List<MsgActionUIVo> getMsgActionList() {
		List<MsgActionUIVo> list = (List<MsgActionUIVo>) getMsgActions().getWrappedData();
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
		List<MsgActionUIVo> list = getMsgActionList();
		for (int i = 0; i < list.size(); i++) {
			MsgActionUIVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMsgActionDao().deleteByUniqueKey(vo.getRuleName(),
						vo.getActionSeq(), vo.getStartTime(), vo.getClientId());
				if (rowsDeleted > 0) {
					logger.info("deleteMsgActions() - MsgAction deleted: " + vo.getRuleName() + "."
							+ vo.getActionSeq() + "." + vo.getStartTime() + "." + vo.getClientId());
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
		List<MsgActionUIVo> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			MsgActionUIVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				MsgActionUIVo vo2 = null;
				try {
					vo2 = (MsgActionUIVo) vo.getClone();
					vo2.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					vo2 = new MsgActionUIVo(new MsgActionVo());
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
		List<MsgActionUIVo> list = getMsgActionList();
		MsgActionUIVo vo = new MsgActionUIVo(new MsgActionVo());
		vo.setActionSeq(0);
		vo.setStartTime(new Timestamp(new Date().getTime()));
		vo.setStatusId(Constants.YES_CODE);
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	public String saveMsgActions() {
		if (isDebugEnabled)
			logger.debug("saveMsgActions() - Entering...");
		if (ruleLogic == null) {
			logger.warn("saveMsgActions() - RuleLogicVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getMsgActionDao().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveMsgActions() - MsgAction Rows Deleted: " + rowsDeleted);
		
		List<MsgActionUIVo> list = getMsgActionList();
		int rowsInserted = 0;
		for (int i=0; i<list.size(); i++) {
			MsgActionUIVo msgActionUIVo = list.get(i);
			msgActionUIVo.setRuleName(ruleLogic.getRuleName());
			// set startTime from startDate and startHour
			Calendar cal = Calendar.getInstance();
			cal.setTime(msgActionUIVo.getStartDate());
			cal.set(Calendar.HOUR_OF_DAY, msgActionUIVo.getStartHour());
			msgActionUIVo.setStartTime(new Timestamp(cal.getTimeInMillis()));
			// end of startTime
			MsgActionVo msgActionVo = msgActionUIVo.getMsgActionVo();
			rowsInserted += getMsgActionDao().insert(msgActionVo);
		}
		logger.info("saveMsgActions() - MsgAction Rows Inserted: " + rowsInserted);
		return "msgrule.saved";
	}

	/*
	 * Logic Evaluation Section 
	 */
	
	public boolean getAnyRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyRulesMarkedForDeletion() - Entering...");
		List<RuleLogicVo> list = getRuleLogicList();
		for (Iterator<RuleLogicVo> it=list.iterator(); it.hasNext();) {
			RuleLogicVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyElementsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyElementsMarkedForDeletion() - Entering...");
		List<RuleElementVo> list = getRuleElementList();
		for (Iterator<RuleElementVo> it=list.iterator(); it.hasNext();) {
			RuleElementVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnySubRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySubRulesMarkedForDeletion() - Entering...");
		List<RuleSubRuleMapVo> list = getSubRuleList();
		for (Iterator<RuleSubRuleMapVo> it=list.iterator(); it.hasNext();) {
			RuleSubRuleMapVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyMsgActionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMsgActionsMarkedForDeletion() - Entering...");
		List<MsgActionUIVo> list = getMsgActionList();
		for (Iterator<MsgActionUIVo> it=list.iterator(); it.hasNext();) {
			MsgActionUIVo vo = it.next();
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
			List<RuleSubRuleMapVo> list = getRuleSubRuleMapDao().getByRuleName(
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
			RuleLogicVo vo = (RuleLogicVo) ruleLogics.getRowData();
			List<MsgActionVo> list = getMsgActionDao().getByRuleName(vo.getRuleName());
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
		int seq;
		if (ruleLogic != null) {
			seq = ruleLogic.getRuleSeq();
		}
		else {
			logger.error("validatePrimaryKey(() - RuleLogicVo is null");
			return;
		}
		RuleLogicVo vo = getRuleLogicDao().getByPrimaryKey(ruleName, seq);
		if (editMode == true && vo == null) {
			// ruleLogic does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "ruleLogicDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// ruleLogic already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
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
			FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
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
		FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
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
	protected List<RuleLogicVo> getRuleLogicList() {
		if (ruleLogics == null) {
			return new ArrayList<RuleLogicVo>();
		}
		else {
			return (List<RuleLogicVo>)ruleLogics.getWrappedData();
		}
	}
	
	public RuleLogicVo getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogicVo ruleLogic) {
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

	public DataModel getRuleLogics() {
		return ruleLogics;
	}

	public UIInput getStartDateInput() {
		return startDateInput;
	}

	public void setStartDateInput(UIInput startDateInput) {
		this.startDateInput = startDateInput;
	}

	public RuleElementVo getRuleElement() {
		return ruleElement;
	}

	public void setRuleElement(RuleElementVo ruleElement) {
		this.ruleElement = ruleElement;
	}
}
