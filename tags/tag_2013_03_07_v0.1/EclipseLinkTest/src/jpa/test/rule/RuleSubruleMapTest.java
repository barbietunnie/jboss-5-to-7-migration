package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import jpa.data.preload.RuleNameEnum;
import jpa.model.rule.RuleLogic;
import jpa.model.rule.RuleSubruleMap;
import jpa.model.rule.RuleSubruleMapPK;
import jpa.service.rule.RuleElementService;
import jpa.service.rule.RuleLogicService;
import jpa.service.rule.RuleSubruleMapService;
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
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RuleSubruleMapTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void RuleElementPrepare() {
	}

	@Autowired
	RuleLogicService logicService;
	@Autowired
	RuleElementService elementService;
	@Autowired
	RuleSubruleMapService service;
	
	@Test
	public void ruleSubruleMapService1() {
		List<RuleSubruleMap> lst1 = service.getByRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		assertFalse(lst1.isEmpty());
		
		List<RuleSubruleMap> lst2 = service.getByRuleName(RuleNameEnum.MAILBOX_FULL.getValue());
		assertFalse(lst2.isEmpty());

		RuleLogic rlg1 = logicService.getByRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		RuleLogic rlg2 = logicService.getByRuleName("MailboxFull_Body_Match");

		// test insert
		RuleSubruleMap map1 = new RuleSubruleMap();
		RuleSubruleMapPK pk1 = new RuleSubruleMapPK(rlg1, rlg2);
		map1.setRuleSubruleMapPK(pk1);
		map1.setSubruleSequence(0);
		service.insert(map1);
		
		RuleSubruleMap map2 = service.getByPrimaryKey(pk1);
		System.out.println(StringUtil.prettyPrint(map2));
		
		// test update
		map2.setUpdtUserId("JpaTest");
		service.update(map2);
		RuleSubruleMap map3 = service.getByRowId(map2.getRowId());
		assertTrue("JpaTest".equals(map3.getUpdtUserId()));
		
		// test insert
		RuleLogic rlg3 = logicService.getByRuleName(RuleNameEnum.MAILBOX_FULL.getValue());
		RuleLogic rlg4 = logicService.getByRuleName("HardBounce_Subj_Match");
		RuleSubruleMap map4 = new RuleSubruleMap();
		RuleSubruleMapPK pk2 = new RuleSubruleMapPK(rlg3, rlg4);
		map4.setRuleSubruleMapPK(pk2);
		map4.setSubruleSequence(2);
		service.insert(map4);
		
		RuleSubruleMap objs4 = service.getByPrimaryKey(pk2);
		assertNotNull(objs4);
		assertTrue(map3.getRowId()!=objs4.getRowId());
		service.delete(objs4);
		try {
			service.getByRowId(objs4.getRowId());
			fail();
		}
		catch (NoResultException e) {}

		// test delete
		int random = new Random().nextInt(3);
		if (random==0) {
			assertTrue(1==service.deleteByRowId(map3.getRowId()));
		}
		else if (random==1) {
			assertTrue(1==service.deleteByPrimaryKey(pk1));
		}
		else {
			assertTrue(1<service.deleteByRuleName(rlg1.getRuleName()));
		}
	}
}
