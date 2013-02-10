package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.model.ActionDetail;
import jpa.service.ActionDetailService;
import jpa.service.RuleDataValueService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ActionDetailTest {
	static Logger logger = Logger.getLogger(ActionDetailTest.class);
	
	final String testActionId = "testAction";
	final String testServiceName = "testService";
	
	@BeforeClass
	public static void ActionDetailPrepare() {
	}

	@Autowired
	ActionDetailService service;
	@Autowired
	RuleDataValueService propService;

	@Test
	public void actionDetailService1() {
		// test insert
		ActionDetail var1 = new ActionDetail();
		var1.setActionId(testActionId);
		var1.setServiceName(testServiceName);
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		ActionDetail var2 = service.getByActionId(testActionId);
		assertNotNull(var2);
		logger.info("ActionDetail: " + StringUtil.prettyPrint(var2));

		List<ActionDetail> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		// test insert
		ActionDetail var3 = createNewInstance(list1.get(0));
		var3.setActionId(var3.getActionId()+"_v2");
		service.insert(var3);
		assertNotNull(service.getByActionId(var3.getActionId()));
		// end of test insert
		// test update
		var3.setUpdtUserId("jpa test");
		service.update(var3);
		ActionDetail var5 = service.getByActionId(var3.getActionId());
		assertTrue("jpa test".equals(var5.getUpdtUserId()));
		
		service.delete(var3);
		try {
			service.getByRowId(var5.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		// test delete
		ActionDetail var4 = createNewInstance(var2);
		var4.setActionId(var2.getActionId() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByActionId(var4.getActionId()));
	}
	
	private ActionDetail createNewInstance(ActionDetail orig) {
		ActionDetail dest = new ActionDetail();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
