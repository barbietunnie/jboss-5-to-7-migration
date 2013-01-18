package jpa.test;

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

import jpa.constant.Constants;
import jpa.model.ClientVariable;
import jpa.service.ClientVariableService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ClientVariableTest {
	static Logger logger = Logger.getLogger(ClientVariableTest.class);
	
	final String testVariableName = "CurrentDate";
	final String testClientId = Constants.DEFAULT_CLIENTID;
	
	@BeforeClass
	public static void ClientVariablePrepare() {
	}

	@Autowired
	ClientVariableService service;

	@Test
	public void clientVariableService() {
		ClientVariable var1 = service.getByBestMatch(testClientId, testVariableName, new Date(System.currentTimeMillis()));
		assertNotNull(var1);
		System.out.println("ClientVariable: " + StringUtil.prettyPrint(var1));

		ClientVariable var2 = service.getByPrimaryKey(var1.getClientId(), var1.getVariableName(), var1.getStartTime());
		assertTrue(var1.equals(var2));

		List<ClientVariable> list1 = service.getByVariableName(var1.getVariableName());
		assertFalse(list1.isEmpty());
		
		List<ClientVariable> list2 = service.getCurrentByClientId(testClientId);
		assertFalse(list2.isEmpty());

		// test insert
		Date newTms = new Date(System.currentTimeMillis());
		ClientVariable var3 = createNewInstance(var2);
		var3.setStartTime(newTms);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(var3.getClientId(), var3.getVariableName(), newTms));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(var3.getClientId(), var3.getVariableName(), var3.getStartTime()));

		// test deleteByVariableName
		ClientVariable var4 = createNewInstance(var2);
		var4.setVariableName(var2.getVariableName() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(var4.getVariableName()));
		assertNull(service.getByPrimaryKey(var4.getClientId(), var4.getVariableName(), var4.getStartTime()));

		// test deleteByPrimaryKey
		ClientVariable var5 = createNewInstance(var2);
		var5.setVariableName(var2.getVariableName() + "_v5");
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(var5.getClientId(), var5.getVariableName(), var5.getStartTime()));
		assertNull(service.getByPrimaryKey(var5.getClientId(), var5.getVariableName(), var5.getStartTime()));

		// test getCurrentByClientId
		List<ClientVariable> list3 = service.getCurrentByClientId(var2.getClientId());
		for (ClientVariable rec : list3) {
			logger.info(StringUtil.prettyPrint(rec));
		}

		// test update
		ClientVariable var6 = createNewInstance(var2);
		var6.setVariableName(var2.getVariableName() + "_v6");
		service.insert(var6);
		assertNotNull(service.getByPrimaryKey(var6.getClientId(), var6.getVariableName(), var6.getStartTime()));
		var6.setVariableValue("new test value");
		service.update(var6);
		ClientVariable var_updt = service.getByRowId(var6.getRowId());
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

	private ClientVariable createNewInstance(ClientVariable orig) {
		ClientVariable dest = new ClientVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
