package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import jpa.constant.RuleNameType;
import jpa.model.RuleBase;
import jpa.model.RuleElement;
import jpa.model.RuleLogic;
import jpa.service.RuleElementService;
import jpa.service.RuleLogicService;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RuleElementTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void RuleElementPrepare() {
	}

	@Autowired
	RuleElementService service;
	@Autowired
	RuleLogicService logicService;
	
	@Test
	public void ruleElementService1() {
		RuleLogic logic = logicService.getByRuleName(RuleNameType.HARD_BOUNCE.getValue());
		assertNotNull(logic.getRuleElements());
		assertFalse(logic.getRuleElements().isEmpty());

		// test insert
		int size = logic.getRuleElements().size();
		RuleElement obj1 = new RuleElement();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(obj1, logic.getRuleElements().get(size-1));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		obj1.setRuleLogic(logic);
		obj1.setElementSequence(logic.getRuleElements().get(size-1).getElementSequence()+1);
		obj1.setDataName(RuleBase.BODY);
		obj1.setCriteria(RuleBase.CONTAINS);
		obj1.setTargetText("Mail delivery failed.");
		service.insert(obj1);
		
		RuleElement elem = service.getByPrimaryKey(logic.getRuleName(), obj1.getElementSequence());
		System.out.println(StringUtil.prettyPrint(elem));
		
		// test update
		RuleElement obj2 = logic.getRuleElements().get(size-1);
		obj2.setUpdtUserId("JpaTest");
		service.update(obj2);
		RuleElement obj3 = service.getByRowId(obj2.getRowId());
		assertTrue("JpaTest".equals(obj3.getUpdtUserId()));
		
		// test insert
		RuleElement obj4 = new RuleElement();
		try {
			BeanUtils.copyProperties(obj4, obj3);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		obj4.setElementSequence(obj3.getElementSequence()+2);
		service.insert(obj4);
		
		RuleElement objs4 = service.getByPrimaryKey(obj4.getRuleLogic().getRuleName(),obj4.getElementSequence());
		assertNotNull(objs4);
		assertTrue(obj3.getRowId()!=objs4.getRowId());
		service.delete(objs4);
		try {
			service.getByPrimaryKey(obj4.getRuleLogic().getRuleName(),obj4.getElementSequence());
			fail();
		}
		catch (NoResultException e) {}

		// test delete
		int random = new Random().nextInt(3);
		if (random==0) {
			assertTrue(1==service.deleteByRowId(elem.getRowId()));
		}
		else if (random==1) {
			assertTrue(1==service.deleteByPrimaryKey(obj3.getRuleLogic().getRuleName(),obj3.getElementSequence()));
		}
		else {
			assertTrue(1<service.deleteByRuleName(obj3.getRuleLogic().getRuleName()));
		}
	}
}
