package jpa.test.rule;

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
import jpa.model.rule.RuleDataType;
import jpa.model.rule.RuleDataValue;
import jpa.model.rule.RuleDataValuePK;
import jpa.service.rule.RuleDataTypeService;
import jpa.service.rule.RuleDataValueService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataValueTest {
	static Logger logger = Logger.getLogger(RuleDataValueTest.class);
	
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
	public void ruleDataValueService() {
		// test insert
		RuleDataValue var1 = new RuleDataValue();
		RuleDataValuePK pk1 = new RuleDataValuePK(typ1,testDataValue);
		var1.setRuleDataValuePK(pk1);
		var1.setStatusId(StatusId.ACTIVE.getValue());
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		valueService.insert(var1);
		
		RuleDataValue var2 = valueService.getByPrimaryKey(pk1);
		assertNotNull(var2);
		logger.info("RuleDataValue: " + StringUtil.prettyPrint(var2));

		List<RuleDataValue> list1 = valueService.getAll();
		assertFalse(list1.isEmpty());
		
		List<RuleDataValue> list2 = valueService.getByDataType(testDataType1);
		assertFalse(list2.isEmpty());
		
		// test insert
		RuleDataValue var3 = createNewInstance(list2.get(0));
		RuleDataValuePK pk2 = list2.get(0).getRuleDataValuePK();
		RuleDataValuePK pk3 = new RuleDataValuePK(pk2.getRuleDataType(),pk2.getDataValue()+"_v3");
		var3.setRuleDataValuePK(pk3);
		var3.setStatusId(StatusId.ACTIVE.getValue());
		valueService.insert(var3);
		assertNotNull(valueService.getByPrimaryKey(pk3));
		// end of test insert

		// test update
		var3.setUpdtUserId("jpa test");
		valueService.update(var3);
		RuleDataValue var4 = valueService.getByPrimaryKey(pk3);
		assertTrue("jpa test".equals(var4.getUpdtUserId()));
		
		valueService.delete(var3);
		try {
			valueService.getByPrimaryKey(pk3);
			fail();
		}
		catch (NoResultException e) {}
		
		// test delete
		RuleDataValue var5 = createNewInstance(var2);
		RuleDataValuePK pk5 = new RuleDataValuePK(pk2.getRuleDataType(),pk2.getDataValue()+"_v5");
		var5.setRuleDataValuePK(pk5);
		var5.setStatusId(StatusId.ACTIVE.getValue());
		valueService.insert(var5);
		var5 = valueService.getByPrimaryKey(pk5);
		assertTrue(1==valueService.deleteByRowId(var5.getRowId()));
		// TODO the next two deletes caused INSERT error, revisit the issue.
		//valueService.deleteByPrimaryKey(pk5.getRuleDataType().getDataType(), pk5.getDataValue());
		//assertTrue(1==valueService.deleteByDataType(pk5.getRuleDataType().getDataType()));
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
