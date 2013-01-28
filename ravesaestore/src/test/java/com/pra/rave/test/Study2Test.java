package com.pra.rave.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.util.SpringUtil;
import com.pra.rave.jpa.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

public class Study2Test {
	static Logger logger = LoggerHelper.getLogger();

	@BeforeClass
	public static void StudyPrepare() {
	}

	StudyService service;

	private String testStudyOid = "Test Study OID";

	@Test
	public void testStudyService() {
		service = (StudyService) SpringUtil.getAppContext().getBean("studyService");
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
		logger.debug(StringUtil.prettyPrint(list.get(0)));
		
		Study study3 = service.getById(study.getId());
		assertNotNull(study3);
		
		assertTrue(1==service.deleteById(study3.getId()));
	}
}
