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

import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyEvent;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.service.StudyEventService;
import com.pra.rave.jpa.service.SubjectService;
import com.pra.rave.jpa.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:META-INF/spring-rave-config.xml"})
@TransactionConfiguration(transactionManager="raveTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class StudyEventTest {
	static Logger logger = LoggerHelper.getLogger();

	@Autowired
	StudyEventService service;
	@Autowired
	StudyService studyService;
	@Autowired
	SubjectService subjectService;
	
	@BeforeClass
	public static void StudyEventPrepare() {
	}

	private String testStudyOid = "Test Study OID";
	private String testSubjectKey = "Test Subject Key";
	private Study study = null;
	private Subject subj = null;

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
	}
	
	@After
	public void cleanupData() {
		//studyService.deleteByStudyPK(new StudyPK(testStudyOid));
	}
	
	private String testStudyEventOid = "Test StudyEvent Oid";
	private String testStudyEventRepeatKey = "01";

	@Test
	public void testStudyEventService() {
		// test insert
		StudyEvent se = new StudyEvent();
		se.setSubject(subj);
		se.setStudyEventOID(testStudyEventOid);
		se.setStudyEventRepeatKey(testStudyEventRepeatKey);
		if (subj.getStudyEvents()==null) {
			List<StudyEvent> lst = new ArrayList<StudyEvent>();
			subj.setStudyEvents(lst);
		}
		subj.getStudyEvents().add(se);
		subjectService.update(subj);

		StudyEvent se0 = service.getByPrimaryKey(study.getStudyPK(), subj.getSubjectKey(), testStudyEventOid, testStudyEventRepeatKey);
		assertNotNull(se0);
		assertNotNull(se0.getUpdtTime());
		java.sql.Timestamp before = se0.getUpdtTime();
		
		List<StudyEvent> list0 = service.getByAncestorKeys(study.getStudyPK(), subj.getSubjectKey());
		assertFalse(list0.isEmpty());
		logger.debug(StringUtil.prettyPrint(list0.get(0)));
		
		Study stdy = studyService.getByStudyPK(study.getStudyPK());
		assertFalse(stdy.getSubjects().isEmpty());
		assertFalse(stdy.getSubjects().get(0).getStudyEvents().isEmpty());
		logger.debug(StringUtil.prettyPrint(stdy));
		
		// test update
		String testRptKey2 = testStudyEventRepeatKey + "1";
		se0.setStudyEventRepeatKey(testRptKey2);
		service.update(se0);
		StudyEvent se1 = service.getById(se0.getId());
		assertTrue(testRptKey2.equals(se1.getStudyEventRepeatKey()));
		assertFalse(before.equals(se1.getUpdtTime()));
		assertNotNull(se1.getSubject());
		
		List<StudyEvent> list1 = service.getByStudyPK(study.getStudyPK());
		assertFalse(list1.isEmpty());
		
		StudyEvent se3 = service.getById(se.getId());
		assertNotNull(se3);
		
		assertTrue(1==service.deleteById(se3.getId()));
	}
}
