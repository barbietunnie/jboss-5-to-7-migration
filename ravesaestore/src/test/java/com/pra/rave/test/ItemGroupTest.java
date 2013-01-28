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
import com.pra.rave.jpa.model.ItemGroup;
import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyEvent;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.FormDataService;
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
public class ItemGroupTest {
	static Logger logger = LoggerHelper.getLogger();

	@Autowired
	ItemGroupService service;
	@Autowired
	StudyEventService seService;
	@Autowired
	StudyService studyService;
	@Autowired
	SubjectService subjectService;
	@Autowired
	FormDataService formService;
	
	@BeforeClass
	public static void ItemGroupPrepare() {
	}

	private String testStudyOid = "Test Study OID";
	private String testSubjectKey = "Test Subject Key";
	private String testStudyEventOid = "Test StudyEvent Oid";
	private String testStudyEventRepeatKey = "01";
	private String testFormOid = "Test Form Oid";
	private String testFormRepeatKey = "01";
	private Study study = null;
	private Subject subj = null;
	private StudyEvent se = null;
	private FormData fm = null;

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
		subj.setSubjectKey(testSubjectKey);
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
	}
	
	@After
	public void cleanupData() {
	}
	
	private String testItemGroupOid = "test Item Group Oid";
	private String testItemGroupRepeatKey = "03";

	@Test
	public void testItemGroupService() {
		// test insert
		ItemGroup ig = new ItemGroup();
		ig.setFormData(fm);
		ig.setItemGroupOID(testItemGroupOid);
		ig.setItemGroupRepeatKey(testItemGroupRepeatKey);
		if (fm.getItemGroups()==null) {
			List<ItemGroup> lst = new ArrayList<ItemGroup>();
			fm.setItemGroups(lst);
		}
		fm.getItemGroups().add(ig);
		formService.update(fm);

		ItemGroup ig0 = service.getByPrimaryKey(
				study.getStudyPK(), subj.getSubjectKey(),
				se.getStudyEventOID(), se.getStudyEventRepeatKey(), 
				fm.getFormOID(), fm.getFormRepeatKey(),
				testItemGroupOid, testItemGroupRepeatKey);
		assertNotNull(ig0);
		assertNotNull(ig0.getUpdtTime());
		assertTrue(testItemGroupOid.equals(ig0.getItemGroupOID()));
		assertTrue(testItemGroupRepeatKey.equals(ig0.getItemGroupRepeatKey()));
		assertNotNull(ig0.getFormData());
		java.sql.Timestamp before = ig0.getUpdtTime();
		
		
		List<ItemGroup> list0 = service.getByAncestorKeys(study.getStudyPK(), 
				subj.getSubjectKey(), se.getStudyEventOID(), se.getStudyEventRepeatKey(),
				fm.getFormOID(), fm.getFormRepeatKey());
		assertFalse(list0.isEmpty());
		logger.debug(StringUtil.prettyPrint(list0.get(0)));
		
		Study stdy = studyService.getByStudyPK(study.getStudyPK());
		assertFalse(stdy.getSubjects().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().get(0).getFormDataList().isEmpty());
		logger.debug(StringUtil.prettyPrint(stdy));

		// test update
		String testRptKey2 = testItemGroupRepeatKey + "1";
		ig0.setItemGroupRepeatKey(testRptKey2);
		service.update(ig0);
		ItemGroup ig1 = service.getById(ig0.getId());
		assertTrue(testRptKey2.equals(ig1.getItemGroupRepeatKey()));
		assertFalse(before.equals(ig1.getUpdtTime()));
		assertNotNull(ig1.getFormData());
		
		List<ItemGroup> list1 = service.getByStudyPK(study.getStudyPK());
		assertFalse(list1.isEmpty());
		
		ItemGroup ig3 = service.getById(ig.getId());
		assertNotNull(ig3);
		
		assertTrue(1==service.deleteById(ig3.getId()));
	}
}
