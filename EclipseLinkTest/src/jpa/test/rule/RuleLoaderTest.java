package jpa.test.rule;

import javax.annotation.Resource;

import jpa.service.rule.RuleLoaderBo;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class RuleLoaderTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(RuleLoaderTest.class);
	
	@Resource
	private RuleLoaderBo loader;
	@BeforeClass
	public static void RuleLoaderPrepare() {
	}
	@Test
	public void testRuleMatch() throws Exception {
		loader.loadRules();
		loader.listRuleNames();
		// TODO add assertions
	}
	
}
