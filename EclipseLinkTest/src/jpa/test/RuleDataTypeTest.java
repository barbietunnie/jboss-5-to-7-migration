package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
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
import jpa.constant.StatusId;
import jpa.model.RuleDataType;
import jpa.service.RuleDataTypeService;
import jpa.service.RuleDataValueService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataTypeTest {
	static Logger logger = Logger.getLogger(RuleDataTypeTest.class);
	
	final String testDataType1 = "CarbonCopyAddress";
	final String testDataType2 = "CarbonCopyAddress2";
	final String testDataValue = EmailAddrType.TO_ADDR.getValue();
	
	@BeforeClass
	public static void ActionPropertyPrepare() {
	}

	@Autowired
	RuleDataTypeService typeService;
	@Autowired
	RuleDataValueService valueService;

	RuleDataType typ1= null;
	RuleDataType typ2= null;
	
	@Before
	public void prepare() {
		// test insert
		typ1 = new RuleDataType();
		typ1.setDataType(testDataType1);
		typ1.setStatusId(StatusId.ACTIVE.getValue());
		typ1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		typeService.insert(typ1);

		typ2 = new RuleDataType();
		typ2.setDataType(testDataType2);
		typ2.setStatusId(StatusId.ACTIVE.getValue());
		typ2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		typeService.insert(typ2);
	}
	
	@After
	public void tearDown() {
		if (typ1!=null) {
			typeService.delete(typ1);
		}
		if (typ2!=null) {
			typeService.delete(typ2);
		}
	}

	@Test
	public void ruleDataTypeService() {
		RuleDataType var2 = typeService.getByDataType(typ1.getDataType());
		assertNotNull(var2);
		logger.info("RuleDataType: " + StringUtil.prettyPrint(var2));

		List<RuleDataType> list1 = typeService.getAll();
		assertFalse(list1.isEmpty());
		
		// test insert
		RuleDataType var3 = createNewInstance(list1.get(0));
		var3.setDataType(var3.getDataType()+"_v2");
		typeService.insert(var3);
		assertNotNull(typeService.getByDataType(var3.getDataType()));
		// end of test insert
		// test update
		var3.setUpdtUserId("jpa test");
		typeService.update(var3);
		RuleDataType var5 = typeService.getByDataType(var3.getDataType());
		assertTrue("jpa test".equals(var5.getUpdtUserId()));
		
		typeService.delete(var3);
		try {
			typeService.getByRowId(var5.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		// test delete
		RuleDataType var4 = createNewInstance(var2);
		var4.setDataType(var2.getDataType() + "_v4");
		typeService.insert(var4);
		assertTrue(1==typeService.deleteByDataType(var4.getDataType()));
	}

	private RuleDataType createNewInstance(RuleDataType orig) {
		RuleDataType dest = new RuleDataType();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}

}
