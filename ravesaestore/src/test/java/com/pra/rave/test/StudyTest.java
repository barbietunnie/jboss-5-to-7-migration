package com.pra.rave.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:META-INF/spring-rave-config.xml"})
@TransactionConfiguration(transactionManager="raveTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
//@ActiveProfiles("dev")
public class StudyTest {
	static Logger logger = LoggerHelper.getLogger();

	@BeforeClass
	public static void StudyPrepare() {
	}

	@Autowired
	StudyService service;

	private String testStudyOid = "Test Study OID";

	@Test
	public void testStudyService() {
		service.deleteByStudyPK(new StudyPK(testStudyOid));
		
		// test insert
		Study study = new Study();
		study.setStudyPK(new StudyPK(testStudyOid));
		study.setMetaDataVersionOID("01");
		service.insert(study);

		Study study0 = service.getByStudyPK(new StudyPK(testStudyOid));
		assertNotNull(study0);
		assertNotNull(study0.getUpdtTime());
		java.sql.Timestamp before = study0.getUpdtTime();
		
		// test update
		String testStudyName = "JpaTest";
		study0.setStudyName(testStudyName);
		service.update(study0);
		Study study1 = service.getById(study0.getId());
		assertTrue(testStudyName.equals(study1.getStudyName()));
		assertFalse(before.equals(study1.getUpdtTime()));
		
		List<Study> list = service.getAll();
		assertFalse(list.isEmpty());
		
		Study study3 = service.getById(study.getId());
		assertNotNull(study3);
		logger.debug(StringUtil.prettyPrint(study3));
		
		assertTrue(1==service.deleteById(study3.getId()));
	}
}
