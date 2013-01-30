package com.pra.rave.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pra.rave.jpa.model.FormData;
import com.pra.rave.jpa.model.RaveIpadmin1;
import com.pra.rave.jpa.model.ItemGroup;
import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyEvent;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.FormDataService;
import com.pra.rave.jpa.service.RaveIpadmin1Service;
import com.pra.rave.jpa.service.ItemGroupService;
import com.pra.rave.jpa.service.StudyEventService;
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.service.SubjectService;
import com.pra.rave.jpa.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:META-INF/spring-rave-config.xml"})
@TransactionConfiguration(transactionManager="raveTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RaveIpadmin1Test {
	static Logger logger = LoggerHelper.getLogger();

	@Autowired
	RaveIpadmin1Service service;
	@Autowired
	ItemGroupService igService;
	@Autowired
	StudyEventService seService;
	@Autowired
	StudyService studyService;
	@Autowired
	SubjectService subjectService;
	@Autowired
	FormDataService formService;
	
	@BeforeClass
	public static void RaveIpadmin1Prepare() {
	}

	private String testStudyOid = "Test Study OID";
	private String testIpadmin1Key = "Test Ipadmin1 Key";
	private String testStudyEventOid = "Test StudyEvent Oid";
	private String testStudyEventRepeatKey = "01";
	private String testFormOid = "Test Form Oid";
	private String testFormRepeatKey = "01";
	private String testItemGroupOid = "test Item Group Oid";
	private String testItemGroupRepeatKey = "03";
	private Study study = null;
	private Subject subj = null;
	private StudyEvent se = null;
	private FormData fm = null;
	private ItemGroup ig = null;

	@Before
	public void prepareData() {
		studyService.deleteByStudyPK(new StudyPK(testStudyOid));
		// insert study
		study = new Study();
		study.setStudyPK(new StudyPK(testStudyOid));
		study.setMetaDataVersionOID("01");
		studyService.insert(study);
	
		subj = new Subject();
		subj.setStudy(study);
		subj.setSubjectKey(testIpadmin1Key);
		subj.setLocationOID("Site01");
		if (study.getSubjects()==null) {
			List<Subject> lst = new ArrayList<Subject>();
			study.setSubjects(lst);
		}
		study.getSubjects().add(subj);
		studyService.update(study);

		se = new StudyEvent();
		se.setSubject(subj);
		se.setStudyEventOID(testStudyEventOid);
		se.setStudyEventRepeatKey(testStudyEventRepeatKey);
		if (subj.getStudyEvents()==null) {
			List<StudyEvent> lst = new ArrayList<StudyEvent>();
			subj.setStudyEvents(lst);
		}
		subj.getStudyEvents().add(se);
		subjectService.update(subj);
		
		fm = new FormData();
		fm.setStudyEvent(se);
		fm.setFormOID(testFormOid);
		fm.setFormRepeatKey(testFormRepeatKey);
		if (se.getFormDataList()==null) {
			List<FormData> lst = new ArrayList<FormData>();
			se.setFormDataList(lst);
		}
		se.getFormDataList().add(fm);
		seService.update(se);

		ig = new ItemGroup();
		ig.setFormData(fm);
		ig.setItemGroupOID(testItemGroupOid);
		ig.setItemGroupRepeatKey(testItemGroupRepeatKey);
		if (fm.getItemGroups()==null) {
			List<ItemGroup> lst = new ArrayList<ItemGroup>();
			fm.setItemGroups(lst);
		}
		fm.getItemGroups().add(ig);
		formService.update(fm);
}
	
	@After
	public void cleanupData() {
	}
	
	private String testDoseIp1 = "test Dose Ip 1";
	private java.sql.Date testStartDt1 = new java.sql.Date(System.currentTimeMillis());
	private java.sql.Date testStartDt2 = null;
	private java.sql.Time testStartTm1 = new java.sql.Time(System.currentTimeMillis());
	
	@Test
	public void testItemGroupService() {
		// test insert
		RaveIpadmin1 id1 = new RaveIpadmin1();
		id1.setItemGroup(ig);
		id1.setDose_ip(testDoseIp1);
		id1.setStartdt(testStartDt1);
		id1.setStarttm(testStartTm1);

		if (ig.getRaveIpadmin1()==null) {
			ig.setRaveIpadmin1(id1);
		}
		igService.update(ig);

		RaveIpadmin1 ae0 = service.getByItemGroupId(ig.getId());
		assertNotNull(ae0);
		assertNotNull(ae0.getUpdtTime());
		assertTrue(testStartDt1.equals(ae0.getStartdt()));
		assertTrue(testStartTm1.equals(ae0.getStarttm()));
		assertNotNull(ae0.getItemGroup());
		assertTrue(testItemGroupOid.equals(ae0.getItemGroup().getItemGroupOID()));
		java.sql.Timestamp before = ae0.getUpdtTime();
		
		List<RaveIpadmin1> lst1 = service.getByAncestors(study.getStudyPK(), 
				subj.getSubjectKey(), se.getStudyEventOID(), se.getStudyEventRepeatKey(),
				fm.getFormOID(), fm.getFormRepeatKey(), ig.getItemGroupOID(), ig.getItemGroupRepeatKey());
		assertTrue(lst1.size()==1);
		logger.debug(StringUtil.prettyPrint(lst1.get(0)));
		
		Study stdy = studyService.getByStudyPK(study.getStudyPK());
		assertFalse(stdy.getSubjects().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().get(0).getFormDataList().isEmpty());
		logger.debug(StringUtil.prettyPrint(stdy));

		// test update
		testStartDt2 = new java.sql.Date(0);
		ae0.setStartdt(testStartDt2);
		service.update(ae0);
		RaveIpadmin1 id3 = service.getById(ae0.getId());
		assertTrue(testStartDt2.equals(id3.getStartdt()));
		assertFalse(before.equals(id3.getUpdtTime()));
		assertNotNull(id3.getItemGroup());
		
		List<RaveIpadmin1> lst3 = service.getByStudyPK(study.getStudyPK());
		assertFalse(lst3.isEmpty());
		
		RaveIpadmin1 id4 = service.getById(id1.getId());
		assertNotNull(id4);
		
		assertTrue(1==service.deleteById(id4.getId()));
	}
}
