package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.model.UserData;
import jpa.service.SessionUploadService;
import jpa.service.UserDataService;
import jpa.util.StringUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class SessionUploadTest {

	@BeforeClass
	public static void SessionUploadPrepare() {
	}

	@Autowired
	SessionUploadService service;
	@Autowired
	UserDataService userService;

	private String testSessionId = "jpa test session id";
	
	@Test
	public void userDataService() {
		List<UserData> lst0 = userService.getAll();
		assertFalse(lst0.isEmpty());
		UserData usr1 = lst0.get(0);
		
		List<SessionUpload> lst1 = service.getByUserId(usr1.getUserId());
		assertNotNull(lst1);
		
		// test insert
		SessionUploadPK pk1 = new SessionUploadPK(testSessionId,0);
		SessionUpload tkn1 = new SessionUpload();
		tkn1.setSessionUploadPK(pk1);
		tkn1.setFileName("jpatest1.txt");
		tkn1.setUserData(usr1);
		tkn1.setSessionValue("jpa test 1 content".getBytes());
		service.insert(tkn1);
		
		SessionUpload tkn2 = service.getByPrimaryKey(pk1);
		assertNotNull(tkn2);
		System.out.println(StringUtil.prettyPrint(tkn2,2));
		
		assertFalse(service.getBySessionId(pk1.getSessionId()).isEmpty());
		assertFalse(service.getByUserId(usr1.getUserId()).isEmpty());
		
		// test insert 2
		SessionUploadPK pk2 = new SessionUploadPK(testSessionId,0);
		SessionUpload tkn3 = new SessionUpload();
		tkn3.setSessionUploadPK(pk2);
		tkn3.setFileName("jpatest2.txt");
		tkn3.setUserData(usr1);
		tkn3.setSessionValue("jpa test 2 content".getBytes());
		service.insertLast(tkn3);
		
		SessionUpload tkn4 = service.getByPrimaryKey(pk2);
		assertNotNull(tkn4);
		assertFalse(tkn2.getFileName().equals(tkn4.getFileName()));

		assertTrue(0<=service.deleteExpired(1));

		// test update
		tkn2.setUpdtUserId("JpaTest");
		service.update(tkn2);
		
		SessionUpload tkn5 = service.getByRowId(tkn2.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test select with NoResultException
		service.delete(tkn5);
		try {
			service.getByRowId(tkn5.getRowId());
			fail();
		}
		catch (NoResultException e) {
		}
		
		assertTrue(0==service.deleteByPrimaryKey(tkn5.getSessionUploadPK()));
		assertTrue(0==service.deleteByRowId(tkn5.getRowId()));
	}
}
