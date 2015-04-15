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
import jpa.constant.EmailVariableType;
import jpa.model.EmailVariable;
import jpa.service.common.EmailVariableService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class EmailVariableTest {
	static Logger logger = Logger.getLogger(EmailVariableTest.class);
	
	final String testVariableName = "jpa test variable name";
	
	@BeforeClass
	public static void EmailVariablePrepare() {
	}

	@Autowired
	EmailVariableService service;

	@Test
	public void globalVariableService1() {
		// test insert
		EmailVariable var1 = new EmailVariable();
		var1.setVariableName(testVariableName);
		var1.setVariableType(EmailVariableType.Custom.getValue());
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		EmailVariable var2 = service.getByVariableName(testVariableName);
		assertNotNull(var2);
		logger.info("EmailVariable: " + StringUtil.prettyPrint(var2));

		List<EmailVariable> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		List<EmailVariable> list2 = service.getAllCustomVariables();
		assertFalse(list2.isEmpty());
		
		// test insert
		EmailVariable var3 = createNewInstance(list2.get(0));
		var3.setVariableName(var3.getVariableName()+"_v2");
		service.insert(var3);
		assertNotNull(service.getByVariableName(var3.getVariableName()));
		// end of test insert
		
		service.delete(var3);
		try {
			service.getByVariableName(var3.getVariableName());
			fail();
		}
		catch (NoResultException e) {}
		
		// test deleteByVariableName
		EmailVariable var4 = createNewInstance(var2);
		var4.setVariableName(var2.getVariableName() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(var4.getVariableName()));
		try {
			service.getByVariableName(var4.getVariableName());
			fail();
		}
		catch (NoResultException e) {}

		// test deleteByPrimaryKey
		EmailVariable var5 = createNewInstance(var2);
		var5.setVariableName(var2.getVariableName() + "_v5");
		service.insert(var5);
		var5 = service.getByVariableName(var5.getVariableName());
		service.delete(var5);
		try {
			service.getByVariableName(var5.getVariableName());
			fail();
		}
		catch (NoResultException e) {}
		
		// test update
		EmailVariable var6 = createNewInstance(var2);
		var6.setVariableName(var2.getVariableName() + "_v6");
		service.insert(var6);
		assertNotNull(service.getByVariableName(var6.getVariableName()));
		var6.setDefaultValue("new test value");
		service.update(var6);
		EmailVariable var_updt = service.getByVariableName(var6.getVariableName());
		assertTrue("new test value".equals(var_updt.getDefaultValue()));
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
	
	private EmailVariable createNewInstance(EmailVariable orig) {
		EmailVariable dest = new EmailVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
