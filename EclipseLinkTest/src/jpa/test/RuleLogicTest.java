package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.model.RuleBase;
import jpa.model.RuleLogic;
import jpa.service.RuleLogicService;
import jpa.util.StringUtil;

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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RuleLogicTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void RuleLogicPrepare() {
	}

	@Autowired
	RuleLogicService service;
	
	@Test
	public void idTokensService1() {
		// test insert
		RuleLogic obj1 = new RuleLogic();
		obj1.setRuleName("testrule1");
		obj1.setEvalSequence(0);
		obj1.setRuleType(RuleBase.ALL_RULE);
		obj1.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
		obj1.setMailType(Constants.SMTP_MAIL);
		obj1.setRuleCategory(RuleBase.PRE_RULE);
		obj1.setSubrule(false);
		obj1.setBuiltinRule(false);
		obj1.setDescription("simply get rid of the messages from the mailbox.");
		service.insert(obj1);
		
		RuleLogic objs2 = service.getByRuleName("testrule1");
		assertNotNull(objs2);
		System.out.println(StringUtil.prettyPrint(objs2));
		
		// test update
		RuleLogic obj2 = objs2;
		obj2.setUpdtUserId("JpaTest");
		service.update(obj2);
		RuleLogic obj3 = service.getByRowId(obj2.getRowId());
		assertTrue("JpaTest".equals(obj3.getUpdtUserId()));
		
		try {
			service.getByRuleName("bad test rule name");
			fail();
		}
		catch (NoResultException e) {}

		// test insert
		RuleLogic obj4 = new RuleLogic();
		try {
			BeanUtils.copyProperties(obj4, obj3);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		obj4.setRuleName(obj3.getRuleName()+"_v2");
		service.insert(obj4);
		
		RuleLogic objs4 = service.getByRuleName(obj4.getRuleName());
		assertNotNull(objs4);
		assertTrue(obj3.getRowId()!=objs4.getRowId());
		
		assertTrue(1<service.getNextEvalSequence());
		List<RuleLogic> lst1 = service.getActiveRules();
		for (RuleLogic objs : lst1) {
			System.out.println(StringUtil.prettyPrint(objs,1));
		}
		System.out.println("Number of active rules: " + lst1.size());
		
		List<RuleLogic> lst2 = service.getSubRules(false);
		assertFalse(lst2.isEmpty());
		List<RuleLogic> lst3 = service.getSubRules(true);
		assertTrue(lst2.size()>lst3.size());

		// test delete
		assertTrue(1==service.deleteByRuleName(obj3.getRuleName()));
	}
}
