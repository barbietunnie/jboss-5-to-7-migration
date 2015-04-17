package jpa.test.common;

import static org.junit.Assert.*;

import java.util.Date;
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

import jpa.model.GlobalVariable;
import jpa.model.GlobalVariablePK;
import jpa.service.common.GlobalVariableService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableTest {
	static Logger logger = Logger.getLogger(GlobalVariableTest.class);
	
	final String testVariableName = "CurrentDate";
	
	@BeforeClass
	public static void GlobalVariablePrepare() {
	}

	@Autowired
	GlobalVariableService service;

	@Test
	public void globalVariableService1() {
		GlobalVariablePK pk0 = new GlobalVariablePK(testVariableName,new Date(System.currentTimeMillis()));
		GlobalVariable var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		logger.info("GlobalVariable: " + StringUtil.prettyPrint(var1));

		GlobalVariablePK pk1 = var1.getGlobalVariablePK();
		GlobalVariable var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));

		List<GlobalVariable> list1 = service.getCurrent();
		assertFalse(list1.isEmpty());
		
		List<GlobalVariable> list2 = service.getByVariableName(pk1.getVariableName());
		assertFalse(list2.isEmpty());
		
		// test insert
		Date newTms = new Date(System.currentTimeMillis());
		GlobalVariable var3 = createNewInstance(var2);
		GlobalVariablePK pk2 = var2.getGlobalVariablePK();
		GlobalVariablePK pk3 = new GlobalVariablePK(pk2.getVariableName(), newTms);
		var3.setGlobalVariablePK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		try {
			service.getByPrimaryKey(pk3);
			fail();
		}
		catch (NoResultException e) {}
		
		// test getByStatusid
		List<GlobalVariable> list3 = service.getByStatusId(var3.getStatusId());
		assertFalse(list3.isEmpty());
		for (GlobalVariable rec : list3) {
			logger.info(StringUtil.prettyPrint(rec));
		}
		
		// test deleteByVariableName
		GlobalVariable var4 = createNewInstance(var2);
		GlobalVariablePK pk4 = new GlobalVariablePK(pk2.getVariableName()+"_v4",pk2.getStartTime());
		var4.setGlobalVariablePK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(pk4.getVariableName()));
		try {
			service.getByPrimaryKey(pk4);
			fail();
		}
		catch (NoResultException e) {}

		// test deleteByPrimaryKey
		GlobalVariable var5 = createNewInstance(var2);
		GlobalVariablePK pk5 = new GlobalVariablePK(pk2.getVariableName()+"_v5",pk2.getStartTime());
		var5.setGlobalVariablePK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		try {
			service.getByPrimaryKey(pk5);
			fail();
		}
		catch (NoResultException e) {}

		// test update
		GlobalVariable var6 = createNewInstance(var2);
		GlobalVariablePK pk6 = new GlobalVariablePK(pk2.getVariableName()+"_v6",pk2.getStartTime());
		var6.setGlobalVariablePK(pk6);
		service.insert(var6);
		assertNotNull(service.getByPrimaryKey(pk6));
		var6.setVariableValue("new test value");
		service.update(var6);
		GlobalVariable var_updt = service.getByRowId(var6.getRowId());
		assertTrue("new test value".equals(var_updt.getVariableValue()));
		// end of test update
		
		service.delete(var6);
		try {
			service.getByRowId(var6.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}
	}
	
	private GlobalVariable createNewInstance(GlobalVariable orig) {
		GlobalVariable dest = new GlobalVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
