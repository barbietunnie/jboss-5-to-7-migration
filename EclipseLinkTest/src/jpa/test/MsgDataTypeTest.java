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
import jpa.constant.EmailAddrType;
import jpa.model.RuleDataValue;
import jpa.service.RuleDataValueService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MsgDataTypeTest {
	static Logger logger = Logger.getLogger(MsgDataTypeTest.class);
	
	final String testDataType = "emailCarbonCopyAddress";
	final String testDataValue = EmailAddrType.TO_ADDR.getValue();
	
	@BeforeClass
	public static void ActionPropertyPrepare() {
	}

	@Autowired
	RuleDataValueService service;

	@Test
	public void actionPropertyService1() {
		// test insert
		RuleDataValue var1 = new RuleDataValue();
		var1.setDataType(testDataType);
		var1.setDataValue(testDataValue);
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		RuleDataValue var2 = service.getByPrimaryKey(testDataType, testDataValue);
		assertNotNull(var2);
		logger.info("RuleDataValue: " + StringUtil.prettyPrint(var2));

		List<RuleDataValue> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		List<RuleDataValue> list2 = service.getByDataType(testDataType);
		assertFalse(list2.isEmpty());
		
		// test insert
		RuleDataValue var3 = createNewInstance(list2.get(0));
		var3.setDataValue(var3.getDataValue()+"_v2");
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(testDataType, var3.getDataValue()));
		// end of test insert
		// test update
		var3.setUpdtUserId("jpa test");
		service.update(var3);
		RuleDataValue var5 = service.getByPrimaryKey(testDataType, var3.getDataValue());
		assertTrue("jpa test".equals(var5.getUpdtUserId()));
		
		service.delete(var3);
		try {
			service.getByPrimaryKey(testDataType, var3.getDataValue());
			fail();
		}
		catch (NoResultException e) {}
		
		// test delete
		RuleDataValue var4 = createNewInstance(var2);
		var4.setDataType(var2.getDataType() + "_v4");
		var4.setDataValue(var2.getDataValue() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByDataType(var4.getDataType()));
	}
	
	private RuleDataValue createNewInstance(RuleDataValue orig) {
		RuleDataValue dest = new RuleDataValue();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
