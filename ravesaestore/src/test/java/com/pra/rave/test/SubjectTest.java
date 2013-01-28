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
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.service.SubjectService;
import com.pra.rave.jpa.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:META-INF/spring-rave-config.xml"})
@TransactionConfiguration(transactionManager="raveTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class SubjectTest {
	static Logger logger = LoggerHelper.getLogger();

	@Autowired
	SubjectService service;
	@Autowired
	StudyService studyService;
	
	@BeforeClass
	public static void SubjectPrepare() {
	}

	private String testStudyOid = "Test Study OID";
	private Study study = null;

	@Before
	public void prepareData() {
		studyService.deleteByStudyPK(new StudyPK(testStudyOid));
		
		// test insert
		study = new Study();
		study.setStudyPK(new StudyPK(testStudyOid));
		study.setMetaDataVersionOID("01");
		studyService.insert(study);
	}
	
	@After
	public void cleanupData() {
		studyService.deleteByStudyPK(new StudyPK(testStudyOid));
	}
	
	private String testSubjectKey = "Test Subject Key";

	@Test
	public void testSubjectService() {
		// test insert
		Subject subj = new Subject();
		subj.setStudy(study);
		subj.setSubjectKey(testSubjectKey);
		subj.setLocationOID("Site01");
		//service.insert(subj);
		if (study.getSubjects()==null) {
			List<Subject> lst = new ArrayList<Subject>();
			study.setSubjects(lst);
		}
		study.getSubjects().add(subj);
		studyService.update(study);

		Subject subj0 = service.getByPrimaryKey(study.getStudyPK(), testSubjectKey);
		assertNotNull(subj0);
		assertNotNull(subj0.getUpdtTime());
		java.sql.Timestamp before = subj0.getUpdtTime();
		logger.debug(StringUtil.prettyPrint(subj0));
		
		assertFalse(service.getByStudyPK(study.getStudyPK()).isEmpty());
		
		Study stdy1 = studyService.getByStudyPK(study.getStudyPK());
		assertFalse(stdy1.getSubjects().isEmpty());
		logger.debug(StringUtil.prettyPrint(stdy1));
		
		// test update
		String testSiteId = "JpaTest";
		subj0.setLocationOID(testSiteId);
		service.update(subj0);
		Subject subj1 = service.getById(subj0.getId());
		assertTrue(testSiteId.equals(subj1.getLocationOID()));
		assertFalse(before.equals(subj1.getUpdtTime()));
		assertNotNull(subj1.getStudy());
		
		List<Subject> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		Subject subj3 = service.getById(subj.getId());
		assertNotNull(subj3);
		
		assertTrue(1==service.deleteById(subj3.getId()));
	}
}
