package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.msgui.vo.PagingSubscriberData;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriberDataService;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.commons.lang3.StringUtils;
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
public class SubscriberDataTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void SubscriberDataPrepare() {
	}

	@Autowired
	SubscriberDataService service;
	
	@Autowired
	SenderDataService cdService;
	@Autowired
	EmailAddressService emailService;

	@Test
	public void subscriberDataService() {
		List<SubscriberData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		SubscriberData rcd0 = service.getBySubscriberId(list.get(0).getSubscriberId());
		assertNotNull(rcd0);
		
		service.getByEmailAddress(rcd0.getEmailAddr().getAddress());
		
		// test paging for UI application
		PagingSubscriberData vo = new PagingSubscriberData();
		vo.setSenderId(rcd0.getSenderData().getSenderId());
		vo.setEmailAddr(rcd0.getEmailAddr().getAddress());
		if (StringUtils.isNotBlank(rcd0.getSsnNumber())) {
			vo.setSsnNumber(rcd0.getSsnNumber());
		}
		if (StringUtils.isNotBlank(rcd0.getDayPhone())) {
			vo.setDayPhone(rcd0.getDayPhone());
		}
		if (StringUtils.isNotBlank(vo.getFirstName())) {
			vo.setFirstName(rcd0.getFirstName());
		}
		if (StringUtils.isNotBlank(rcd0.getLastName())) {
			vo.setLastName(rcd0.getLastName());
		}
		List<SubscriberData> listPg = service.getSubscribersWithPaging(vo);
		assertTrue(listPg.size()>0);
		System.out.println(StringUtil.prettyPrint(listPg.get(0)));
		int count = service.getSubscriberCount(vo);
		assertTrue(count==listPg.size());

		// test update
		rcd0.setUpdtUserId("JpaTest");
		service.update(rcd0);
		SubscriberData rcd1 = service.getByRowId(rcd0.getRowId());
		assertTrue("JpaTest".equals(rcd1.getUpdtUserId()));
		
		// test insert
		SenderData cd2 = cdService.getBySenderId(rcd0.getSenderData().getSenderId());
		SubscriberData rcd2 = new SubscriberData();
		try {
			// allow null sql timestamp to be copied
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			// allow null util date to be copied
			DateConverter converter2 = new DateConverter(null);
			ConvertUtils.register(converter2, java.util.Date.class);
			// copy properties from rcd1 to rcd2
			BeanUtils.copyProperties(rcd2, rcd1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd2.setSenderData(cd2);
		rcd2.setSubscriberId(rcd1.getSubscriberId()+"_2");
		rcd2.setEmailAddr(emailService.findSertAddress(rcd2.getSubscriberId()+"@localhost"));
		service.insert(rcd2);
		
		SubscriberData rcd3 = service.getBySubscriberId(rcd1.getSubscriberId()+"_2");
		assertNotNull(rcd3);
		assertTrue(rcd1.getRowId()!=rcd3.getRowId());
		
		assertTrue(1==service.deleteBySubscriberId(rcd3.getSubscriberId()));
	}
}
