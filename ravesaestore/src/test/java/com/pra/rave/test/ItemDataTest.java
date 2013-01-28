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
import com.pra.rave.jpa.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:META-INF/spring-rave-config.xml"})
@TransactionConfiguration(transactionManager="raveTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ItemDataTest {
	static Logger logger = LoggerHelper.getLogger();

	@Autowired
	ItemDataService service;
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
	public static void ItemDataPrepare() {
	}

	private String testStudyOid = "Test Study OID";
	private String testSubjectKey = "Test Subject Key";
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
	
	private String testItemDataOid1 = "test Item Data Oid 1";
	private String testItemDataValue1 = "test value 1";
	private String testItemDataOid2 = "test Item Data Oid 2";
	private String testItemValue2 = "test value 2";
	
	@Test
	public void testItemGroupService() {
		// test insert
		ItemData id1 = new ItemData();
		id1.setItemGroup(ig);
		id1.setItemOID(testItemDataOid1);
		id1.setIsNull("No");
		id1.setItemValue(testItemDataValue1);

		ItemData id2 = new ItemData();
		id2.setItemGroup(ig);
		id2.setItemOID(testItemDataOid2);
		id2.setIsNull("Yes");
		
		if (ig.getItemDataList()==null) {
			List<ItemData> lst = new ArrayList<ItemData>();
			ig.setItemDataList(lst);
		}
		ig.getItemDataList().add(id1);
		ig.getItemDataList().add(id2);
		igService.update(ig);

		ItemData id0 = service.getByPrimaryKey(
				study.getStudyPK(), subj.getSubjectKey(),
				se.getStudyEventOID(), se.getStudyEventRepeatKey(), 
				fm.getFormOID(), fm.getFormRepeatKey(),
				ig.getItemGroupOID(), ig.getItemGroupRepeatKey(), testItemDataOid1);
		assertNotNull(id0);
		assertNotNull(id0.getUpdtTime());
		assertTrue(testItemDataOid1.equals(id0.getItemOID()));
		assertTrue(testItemDataValue1.equals(id0.getItemValue()));
		assertNotNull(id0.getItemGroup());
		assertTrue(testItemGroupOid.equals(id0.getItemGroup().getItemGroupOID()));
		java.sql.Timestamp before = id0.getUpdtTime();
		
		
		List<ItemData> list0 = service.getByAncestorKeys(study.getStudyPK(), 
				subj.getSubjectKey(), se.getStudyEventOID(), se.getStudyEventRepeatKey(),
				fm.getFormOID(), fm.getFormRepeatKey(), ig.getItemGroupOID(), ig.getItemGroupRepeatKey());
		assertTrue(list0.size()==2);
		logger.debug(StringUtil.prettyPrint(list0.get(0)));
		
		Study stdy = studyService.getByStudyPK(study.getStudyPK());
		assertFalse(stdy.getSubjects().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().get(0).getFormDataList().isEmpty());
		logger.debug(StringUtil.prettyPrint(stdy));

		// test update
		id0.setItemValue(testItemValue2);
		service.update(id0);
		ItemData id3 = service.getById(id0.getId());
		assertTrue(testItemValue2.equals(id3.getItemValue()));
		assertFalse(before.equals(id3.getUpdtTime()));
		assertNotNull(id3.getItemGroup());
		
		List<ItemData> list1 = service.getByStudyPK(study.getStudyPK());
		assertFalse(list1.isEmpty());
		
		ItemData id4 = service.getById(id1.getId());
		assertNotNull(id4);
		
		assertTrue(1==service.deleteById(id4.getId()));
	}
}
