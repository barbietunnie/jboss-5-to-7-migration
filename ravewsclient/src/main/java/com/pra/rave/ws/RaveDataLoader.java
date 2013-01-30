package com.pra.rave.ws;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.cdisc.ns.odm.v1.YesOnly;
import org.slf4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.pra.rave.jpa.model.FormData;
import com.pra.rave.jpa.model.ItemData;
import com.pra.rave.jpa.model.ItemGroup;
import com.pra.rave.jpa.model.RaveAdverse1;
import com.pra.rave.jpa.model.RaveAescr1;
import com.pra.rave.jpa.model.RaveSrf1;
import com.pra.rave.jpa.model.RaveSubject;
import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyEvent;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.FormDataService;
import com.pra.rave.jpa.service.ItemDataService;
import com.pra.rave.jpa.service.ItemGroupService;
import com.pra.rave.jpa.service.RaveAdverse1Service;
import com.pra.rave.jpa.service.RaveAescr1Service;
import com.pra.rave.jpa.service.RaveIpadmin1Service;
import com.pra.rave.jpa.service.RaveSrf1Service;
import com.pra.rave.jpa.service.RaveSubjectService;
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
			loader.persistForm("SUBJECT", client);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}

	public RaveDataLoader() {}
	
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
	private RaveAdverse1Service adverse1Service = null;
	private RaveAescr1Service aescr1Service = null;
	private RaveSrf1Service srf1Service = null;
	private RaveSubjectService raveSubjService = null;
	private RaveIpadmin1Service ipadmin1Service = null;
	
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
			adverse1Service = (RaveAdverse1Service) SpringUtil.getAppContext().getBean("raveAdverse1Service");
			aescr1Service = (RaveAescr1Service) SpringUtil.getAppContext().getBean("raveAescr1Service");
			srf1Service = (RaveSrf1Service) SpringUtil.getAppContext().getBean("raveSrf1Service");
			raveSubjService = (RaveSubjectService) SpringUtil.getAppContext().getBean("raveSubjectService");
			ipadmin1Service = (RaveIpadmin1Service) SpringUtil.getAppContext().getBean("raveIpadmin1Service");
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
								}
								// remove itemOid's not present in Rave
								if (TransactionType.INSERT.equals(itemGroup.getTransactionType())
										|| TransactionType.UPSERT.equals(itemGroup.getTransactionType())) {
									for (ItemData id : ids) {
										if (id.isRemove()) {
											idService.delete(id);
										}
									}
								}
								/*
								 *  ADVERSE_1
								 */
								if (FormOid.ADVERSE_1.getValue().equals(formData.getFormOID())) {
									RaveAdverse1 ae1 = null;
									try {
										ae1 = adverse1Service.getByItemGroupId(ig.getId());
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getAecasnum()!=null && ae1.getAecasnum().length()>0) {
											adverse1Service.update(ae1);
										}
										else {
											adverse1Service.delete(ae1);
										}
									}
									catch (NoResultException e) {
										ae1 = new RaveAdverse1();
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getAecasnum()!=null && ae1.getAecasnum().length()>0) {
											ae1.setItemGroup(ig);
											adverse1Service.insert(ae1);
										}
									}
									logger.info(StringUtil.prettyPrint(ae1));
								}
								/*
								 *  AESCR_1
								 */
								else if (FormOid.AESCR_1.getValue().equals(formData.getFormOID())) {
									RaveAescr1 ae1 = null;
									try {
										ae1 = aescr1Service.getByItemGroupId(ig.getId());
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getAecasnum()!=null && ae1.getAecasnum().length()>0) {
											aescr1Service.update(ae1);
										}
										else {
											aescr1Service.delete(ae1);
										}
									}
									catch (NoResultException e) {
										ae1 = new RaveAescr1();
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getAecasnum()!=null && ae1.getAecasnum().length()>0) {
											ae1.setItemGroup(ig);
											aescr1Service.insert(ae1);
										}
									}
									logger.info(StringUtil.prettyPrint(ae1));
								}
								/*
								 *  SRF_1
								 */
								else if (FormOid.SRF_1.getValue().equals(formData.getFormOID())) {
									RaveSrf1 ae1 = null;
									try {
										ae1 = srf1Service.getByItemGroupId(ig.getId());
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getSrcasnum()!=null && ae1.getSrcasnum().length()>0) {
											srf1Service.update(ae1);
										}
										else {
											srf1Service.delete(ae1);
										}
									}
									catch (NoResultException e) {
										ae1 = new RaveSrf1();
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getSrcasnum()!=null && ae1.getSrcasnum().length()>0) {
											ae1.setItemGroup(ig);
											srf1Service.insert(ae1);
										}
									}
									logger.info(StringUtil.prettyPrint(ae1));
								}
								/*
								 *  SUBJECT
								 */
								else if (FormOid.SUBJECT.getValue().equals(formData.getFormOID())) {
									RaveSubject ae1 = null;
									try {
										ae1 = raveSubjService.getByItemGroupId(ig.getId());
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getPt_id()!=null && ae1.getPt_id().length()>0) {
											raveSubjService.update(ae1);
										}
										else {
											raveSubjService.delete(ae1);
										}
									}
									catch (NoResultException e) {
										ae1 = new RaveSubject();
										copyDataFromRave(itemGroup.getItemDataGroup(), ae1);
										if (ae1.getPt_id()!=null && ae1.getPt_id().length()>0) {
											ae1.setItemGroup(ig);
											raveSubjService.insert(ae1);
										}
									}
									logger.info(StringUtil.prettyPrint(ae1));
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

	private void copyDataFromRave(List<ODMcomplexTypeDefinitionItemData> itemDataList, Object obj) {
		HashMap<String, Method> methodMap = new HashMap<String, Method>();
		List<String> methodNamelist = new ArrayList<String>();
		Method methods[] = obj.getClass().getDeclaredMethods();
		for (int i = 0; i< methods.length; i++) {
			if (methods[i].getName().startsWith("set") || methods[i].getName().startsWith("get")) {
				methodMap.put(methods[i].getName(), methods[i]);
				methodNamelist.add(methods[i].getName());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("getters and setters: {}", methodNamelist);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		for (ODMcomplexTypeDefinitionItemData id : itemDataList) {
			String fldName = getFieldName(id.getItemOID());
			if (!methodMap.containsKey("get" + fldName)) {
				continue; // getter not found
			}
			Method getter = methodMap.get("get" + fldName);
			Class<?>[] getterParmTypes = getter.getParameterTypes();
			if (getterParmTypes.length != 0) {
				continue; // getter with input parameter(s)
			}
			int mod = getter.getModifiers();
			if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isStatic(mod)) {
				continue; // non-public or abstract or static
			}
			if (!methodMap.containsKey("set" + fldName)) {
				continue; // setter not found
			}
			// invoke getter to get current value
			try {
				// get current value
				Object value = getter.invoke(obj, (Object[])getterParmTypes);
				if (logger.isDebugEnabled()) {
					logger.debug("Call to method ({}) returned: {}", getter.getName(), value);
				}
			}
			catch (Exception e) {
				logger.warn("Exception caught invoking method ({}), ignore.", getter.getName());
			}
			// invoke setter to set new value
			Method setter = methodMap.get("set" + fldName);
			Class<?>[] setterParmTypes = setter.getParameterTypes();
			try {
				if ("java.lang.String".equals(setterParmTypes[0].getName())) {
					Class<?> setParms[] = {new String().getClass()};
					try {
						Method setMethod = obj.getClass().getMethod(setter.getName(), setParms);
						String[] strParms = new String[1];
						if (YesOnly.YES.value().equals(id.getIsNull())) {
							strParms[0] = null;
						}
						else {
							strParms[0] = id.getValue();
						}
						setMethod.invoke(obj, (Object[])strParms);
					}
					catch (NoSuchMethodException e) {
						logger.warn("NoSuchMethodException caught getting method ({}), ignore.", setter.getName());
					}
				}
				else if ("java.sql.Date".equals(setterParmTypes[0].getName())) {
					Class<?> setParms[] = {new java.sql.Date(0).getClass()};
					try {
						Method setMethod = obj.getClass().getMethod(setter.getName(), setParms);
						java.sql.Date[] datParms = new java.sql.Date[1];
						datParms[0] = null;
						String dtStr = id.getValue();
						if (dtStr != null) {
							try {
								java.util.Date dt = sdf.parse(dtStr);
								datParms[0] = new java.sql.Date(dt.getTime());
							} catch (ParseException e) {
								logger.error("Malformed date received ({}) from ItemOid ({}), ignore.", dtStr, id.getItemOID());
								// TODO email to developer
							}
						}
						setMethod.invoke(obj, (Object[])datParms);
					} catch (NoSuchMethodException e) {
						logger.warn("NoSuchMethodException caught getting method ({}), ignore.", setter.getName());
					}
				}
			}
			catch (SecurityException e) {
				logger.warn("SecurityException caught invoking method ({}), ignore.", setter.getName());
			}
			catch (InvocationTargetException e) {
				logger.warn("InvocationTargetException caught invoking method ({}), ignore.", setter.getName());
			}
			catch (IllegalAccessException e) {
				logger.warn("IllegalAccessException caught invoking method ({}), ignore.", setter.getName());
			}
		}
	}

	private String getFieldName(String itemOid) {
		if (itemOid == null || itemOid.length()<1) {
			return itemOid;
		}
		int pos = itemOid.indexOf(".");
		if (pos>0 && itemOid.length()>(pos+1)) {
			itemOid = itemOid.substring(pos+1);
		}
		itemOid = itemOid.substring(0,1).toUpperCase() + itemOid.substring(1).toLowerCase();
		return itemOid;
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
		else if (FormOid.SUBJECT.getValue().equals(formOid)) {
			study.setSubjectLoadDt(loadDt);
		}
		else if (FormOid.IPADMIN_1.getValue().equals(formOid)) {
			study.setIpadmin1LoadDt(loadDt);
		}
	}
}
