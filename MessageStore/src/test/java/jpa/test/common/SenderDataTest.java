package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SubscriberData;
import jpa.model.UserData;
import jpa.service.common.SenderDataService;

import org.apache.commons.beanutils.BeanUtils;
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
public class SenderDataTest {

	@BeforeClass
	public static void SenderDataPrepare() {
	}

	@Autowired
	SenderDataService service;

	@Test
	public void senderDataService() {
		List<SenderData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		SenderData tkn0 = service.getBySenderId(Constants.DEFAULT_SENDER_ID);
		assertNotNull(tkn0);
		
		assertTrue(tkn0.getSystemId().equals(service.getSystemId()));
		assertTrue(tkn0.getSystemKey().equals(service.getSystemKey()));
		assertNotNull(service.getByDomainName(tkn0.getDomainName()));

		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		SenderData tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		SenderData tkn2 = new SenderData();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setSenderId(Constants.DEFAULT_SENDER_ID + "_2");
		tkn2.setSenderVariables(new ArrayList<SenderVariable>());
		tkn2.setSubscribers(new ArrayList<SubscriberData>());
		tkn2.setUserDatas(new ArrayList<UserData>());
		service.insert(tkn2);
		
		SenderData tkn3 = service.getBySenderId(tkn2.getSenderId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with NoResultException
		service.delete(tkn3);
		try {
			service.getBySenderId(tkn2.getSenderId());
			fail();
		}
		catch (NoResultException e) {
		}
		
		assertTrue(0==service.deleteBySenderId(tkn3.getSenderId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
