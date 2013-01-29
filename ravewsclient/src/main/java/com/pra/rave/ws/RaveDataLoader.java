package com.pra.rave.ws;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cdisc.ns.odm.v1.ODM;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionClinicalData;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionFormData;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionItemData;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionItemGroupData;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionStudyEventData;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionSubjectData;
import org.cdisc.ns.odm.v1.TransactionType;
import org.slf4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.pra.rave.jpa.model.FormData;
import com.pra.rave.jpa.model.ItemData;
import com.pra.rave.jpa.model.ItemGroup;
import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyEvent;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.FormDataService;
import com.pra.rave.jpa.service.ItemDataService;
import com.pra.rave.jpa.service.ItemGroupService;
import com.pra.rave.jpa.service.StudyEventService;
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.service.SubjectService;
import com.pra.rave.jpa.util.FormOid;
import com.pra.rave.jpa.util.SpringUtil;
import com.pra.rave.xml.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

public class RaveDataLoader {
	static Logger logger = LoggerHelper.getLogger();
	
	public static void main(String[] args) {
		RaveDataLoader loader = new RaveDataLoader();
		RaveClinicalView client = new RaveClinicalView();
		try {
			loader.persistForm("ADVERSE_1", client);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}

	private PlatformTransactionManager txmgr;
	private TransactionStatus status;

	private void startTransaction(String tranName) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName(tranName);
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("raveTransactionManager");
		status = txmgr.getTransaction(def);
	}

	private void commitTransaction() {
		txmgr.commit(status);
	}

	private StudyService studyService = null;
	private SubjectService subjService = null;
	private StudyEventService seService = null;
	private FormDataService fmService = null;
	private ItemGroupService igService = null;
	private ItemDataService idService = null;
	
	boolean printOutStudyStructure = false;
	
	public void persistForm(String formOid, RaveClinicalView client) {
		java.sql.Timestamp startTime = null;
		ODM odm = client.getClinicalViewByForm(formOid, startTime);
		if (odm == null) {
			throw new IllegalArgumentException("FormOid (" + formOid + ") is invalid.");
		}
		try {
			studyService = (StudyService) SpringUtil.getAppContext().getBean("studyService");
			subjService = (SubjectService) SpringUtil.getAppContext().getBean("subjectService");
			seService = (StudyEventService) SpringUtil.getAppContext().getBean("studyEventService");
			fmService = (FormDataService) SpringUtil.getAppContext().getBean("formDataService");
			igService = (ItemGroupService) SpringUtil.getAppContext().getBean("itemGroupService");
			idService = (ItemDataService) SpringUtil.getAppContext().getBean("itemDataService");
			int tran_idx = 0;
			/*
			 * Study
			 */
			for (ODMcomplexTypeDefinitionClinicalData clinical : odm.getClinicalData()) {
				startTransaction(clinical.getStudyOID() + (tran_idx++));
				StudyPK pk = new StudyPK(clinical.getStudyOID());
				Study study = null;
				try {
					study = studyService.getByStudyPK(pk);
					logger.debug("Study ({}) found, update...", pk.getStudyOID());
					study.setMetaDataVersionOID(clinical.getMetaDataVersionOID());
					setStudyLoadTimes(study, odm, formOid);
					studyService.update(study);
				}
				catch (NoResultException e) {
					logger.debug("Study ({}) not found, insert...", pk.getStudyOID());
					study = new Study();
					study.setStudyPK(pk);
					study.setMetaDataVersionOID(clinical.getMetaDataVersionOID());
					setStudyLoadTimes(study, odm, formOid);
					studyService.insert(study);
				}
				/*
				 * Subject
				 */
				for (ODMcomplexTypeDefinitionSubjectData subject : clinical.getSubjectData()) {
					study = studyService.getByStudyPK(pk);
					Subject subj = null;
					try {
						subj = subjService.getByPrimaryKey(pk, subject.getSubjectKey());
						logger.debug("Subject ({}.{}) found, update...", pk.getStudyOID(), subject.getSubjectKey());
						if (subject.getSiteRef()!=null) {
							subj.setLocationOID(subject.getSiteRef().getLocationOID());
						}
						subjService.update(subj);
					}
					catch (NoResultException e) {
						logger.debug("Subject ({}.{}) not found, insert...", pk.getStudyOID(), subject.getSubjectKey());
						subj = new Subject();
						subj.setSubjectKey(subject.getSubjectKey());
						subj.setStudy(study);
						if (subject.getSiteRef()!=null) {
							subj.setLocationOID(subject.getSiteRef().getLocationOID());
						}
						if (study.getSubjects()==null) {
							List<Subject> lst = new ArrayList<Subject>();
							study.setSubjects(lst);
						}
						study.getSubjects().add(subj);
						studyService.update(study);
					}
					subject.getStudyEventData();
					/*
					 * Study Event
					 */
					for (ODMcomplexTypeDefinitionStudyEventData studyEvent : subject.getStudyEventData()) {
						StudyEvent se = null;
						String logSeStr = subject.getSubjectKey() + "." + studyEvent.getStudyEventOID() + "." + studyEvent.getStudyEventRepeatKey();
						try {
							se = seService.getByPrimaryKey(pk,
									subject.getSubjectKey(),
									studyEvent.getStudyEventOID(),
									studyEvent.getStudyEventRepeatKey());
							logger.debug("StudyEvent ({}.{}) found...", pk.getStudyOID(), logSeStr);
						}
						catch (NoResultException e) {
							logger.debug("StudyEvent ({}.{}) not found, insert...", pk.getStudyOID(), logSeStr);
							se = new StudyEvent();
							se.setSubject(subj);
							se.setStudyEventOID(studyEvent.getStudyEventOID());
							se.setStudyEventRepeatKey(studyEvent.getStudyEventRepeatKey());
							if (subj.getStudyEvents()==null) {
								List<StudyEvent> lst = new ArrayList<StudyEvent>();
								subj.setStudyEvents(lst);
							}
							subj.getStudyEvents().add(se);
							subjService.update(subj);
						}
						/*
						 * Form Data
						 */
						for (ODMcomplexTypeDefinitionFormData formData : studyEvent.getFormData()) {
							FormData fm = null;
							String logFmStr = subject.getSubjectKey() + "."
									+ studyEvent.getStudyEventOID() + "."
									+ studyEvent.getStudyEventRepeatKey() + "."
									+ formData.getFormOID() + "."
									+ formData.getFormRepeatKey();
							try {
								fm = fmService.getByPrimaryKey(pk,
										subject.getSubjectKey(),
										studyEvent.getStudyEventOID(),
										studyEvent.getStudyEventRepeatKey(),
										formData.getFormOID(),
										formData.getFormRepeatKey());
								logger.debug("FormData ({}.{}) found...", pk.getStudyOID(), logFmStr);
							}
							catch (NoResultException e) {
								logger.debug("FormData ({}.{}) not found, insert...", pk.getStudyOID(), logFmStr);
								fm = new FormData();
								fm.setStudyEvent(se);
								fm.setFormOID(formData.getFormOID());
								fm.setFormRepeatKey(formData.getFormRepeatKey());
								if (se.getFormDataList()==null) {
									List<FormData> lst = new ArrayList<FormData>();
									se.setFormDataList(lst);
								}
								se.getFormDataList().add(fm);
								seService.update(se);
							}
							/*
							 * Item Group
							 */
							for (ODMcomplexTypeDefinitionItemGroupData itemGroup : formData.getItemGroupData()) {
								TransactionType tranType = null;
								if (startTime!=null) { // Incremental
									tranType = itemGroup.getTransactionType();
								}
								ItemGroup ig = null;
								String logIgStr = subject.getSubjectKey() + "."
										+ studyEvent.getStudyEventOID() + "."
										+ studyEvent.getStudyEventRepeatKey() + "."
										+ formData.getFormOID() + "."
										+ formData.getFormRepeatKey() + "."
										+ itemGroup.getItemGroupOID() + "."
										+ itemGroup.getItemGroupRepeatKey();
								try {
									ig = igService.getByPrimaryKey(pk,
													subject.getSubjectKey(),
													studyEvent.getStudyEventOID(),
													studyEvent.getStudyEventRepeatKey(),
													formData.getFormOID(),
													formData.getFormRepeatKey(),
													itemGroup.getItemGroupOID(),
													itemGroup.getItemGroupRepeatKey());
									logger.debug("ItemGroup ({}.{}) found...", pk.getStudyOID(), logIgStr);
									if (TransactionType.REMOVE.equals(tranType)) {
										igService.delete(ig); // XXX cascade delete
										continue;
									}
								}
								catch (NoResultException e) {
									logger.debug("ItemGroup ({}.{}) not found...", pk.getStudyOID(), logIgStr);
									if (TransactionType.REMOVE.equals(tranType)) {
										continue; // record to be removed does not exist locally
									}
									ig = new ItemGroup();
									ig.setFormData(fm);
									ig.setItemGroupOID(itemGroup.getItemGroupOID());
									ig.setItemGroupRepeatKey(itemGroup.getItemGroupRepeatKey());
									if (fm.getItemGroups()==null) {
										List<ItemGroup> lst = new ArrayList<ItemGroup>();
										fm.setItemGroups(lst);
									}
									fm.getItemGroups().add(ig);
									fmService.update(fm);
								}
								// get existing items from local database
								List<ItemData> ids = idService.getByAncestorKeys(pk,
										subject.getSubjectKey(),
										studyEvent.getStudyEventOID(),
										studyEvent.getStudyEventRepeatKey(),
										formData.getFormOID(),
										formData.getFormRepeatKey(),
										itemGroup.getItemGroupOID(),
										itemGroup.getItemGroupRepeatKey());
								logger.debug("Number of items ({}) found under key: ({})", ids.size(), logIgStr);
								/*
								 * Item Data
								 */
								// loop through items received from remote site
								for (ODMcomplexTypeDefinitionItemData itemData : itemGroup.getItemDataGroup()) {
									// does the item exist in local database?
									ItemData id = getByItemOid(itemData.getItemOID(), ids);
									if (id==null) { // no, insert
										logger.debug("Item OID ({}) not found, insert...", itemData.getItemOID());
										if (TransactionType.REMOVE.equals(itemData.getTransactionType())) {
											continue;
										}
										id = new ItemData();
										id.setItemGroup(ig);
										id.setItemOID(itemData.getItemOID());
										id.setItemValue(itemData.getValue());
										if (itemData.getIsNull()!=null) {
											id.setIsNull(itemData.getIsNull().value());
										}
										if (ig.getItemDataList()==null) {
											List<ItemData> lst = new ArrayList<ItemData>();
											ig.setItemDataList(lst);
										}
										ig.getItemDataList().add(id);
										igService.update(ig);
									}
									else { // yes, update
										logger.debug("Item OID ({}) found, update...", itemData.getItemOID());
										if (TransactionType.REMOVE.equals(itemData.getTransactionType())) {
											id.setRemove(true);
										}
										else {
											id.setItemValue(itemData.getValue());
											if (itemData.getIsNull()!=null) {
												id.setIsNull(itemData.getIsNull().value());
											}
											else {
												id.setIsNull(null);
											}
											id.setRemove(false);
											idService.update(id);
										}
									}
									if (isCaseNumber(itemData.getItemOID())) {
										// TODO
									}
								}
								// remove items not present in Rave
								for (ItemData id : ids) {
									if (id.isRemove()) {
										idService.delete(id);
									}
								}
							}
						}
					}
					logger.info("Committing changes to subject ({})...", subject.getSubjectKey());
				}
				if (printOutStudyStructure && tran_idx==odm.getClinicalData().size()) {
					logger.debug(StringUtil.prettyPrint(study));
				}
				commitTransaction();
			}
		}
		finally { 
			//
		}
 	}

	private boolean isCaseNumber(String itemOid) {
		return false;
	}

	private ItemData getByItemOid(String oid, List<ItemData> list) {
		for (ItemData id : list) {
			if (id.getItemOID().equals(oid)) {
				return id;
			}
		}
		return null;
	}

	private void setStudyLoadTimes(Study study, ODM odm, String formOid) {
		XMLGregorianCalendar cal = odm.getCreationDateTime();
		java.sql.Timestamp loadDt = new java.sql.Timestamp(cal.getMillisecond());
		if (FormOid.ADVERSE_1.getValue().equals(formOid)) {
			study.setAdverse1LoadDt(loadDt);
		}
		else if (FormOid.AESCR_1.getValue().equals(formOid)) {
			study.setAescr1LoadDt(loadDt);
		}
		else if (FormOid.SRF_1.getValue().equals(formOid)) {
			study.setSrf1LoadDt(loadDt);
		}
	}
}
