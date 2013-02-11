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
import jpa.model.ClientData;
import jpa.model.ClientVariable;
import jpa.model.ClientVariablePK;
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
		ClientData cd0 = new ClientData();
		cd0.setClientId(testClientId);
		ClientVariablePK pk0 = new ClientVariablePK(cd0, testVariableName, new Date(System.currentTimeMillis()));
		ClientVariable var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		System.out.println("ClientVariable: " + StringUtil.prettyPrint(var1));

		ClientVariablePK pk1 = var1.getClientVariablePK();
		ClientVariable var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));

		List<ClientVariable> list1 = service.getByVariableName(pk1.getVariableName());
		assertFalse(list1.isEmpty());
		
		List<ClientVariable> list2 = service.getCurrentByClientId(testClientId);
		assertFalse(list2.isEmpty());

		// test insert
		Date newTms = new Date(System.currentTimeMillis()+1);
		ClientVariable var3 = createNewInstance(var2);
		ClientVariablePK pk2 = var2.getClientVariablePK();
		ClientVariablePK pk3 = new ClientVariablePK(pk2.getClientData(), pk2.getVariableName(), newTms);
		var3.setClientVariablePK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(pk3));

		// test deleteByVariableName
		ClientVariable var4 = createNewInstance(var2);
		ClientVariablePK pk4 = new ClientVariablePK(pk2.getClientData(), pk2.getVariableName()+"_v4", pk2.getStartTime());
		var4.setClientVariablePK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(pk4.getVariableName()));
		assertNull(service.getByPrimaryKey(pk4));

		// test deleteByPrimaryKey
		ClientVariable var5 = createNewInstance(var2);
		ClientVariablePK pk5 = new ClientVariablePK(pk2.getClientData(), pk2.getVariableName()+"_v5", pk2.getStartTime());
		var5.setClientVariablePK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		assertNull(service.getByPrimaryKey(pk5));

		// test getCurrentByClientId
		List<ClientVariable> list3 = service.getCurrentByClientId(pk2.getClientData().getClientId());
		for (ClientVariable rec : list3) {
			logger.info(StringUtil.prettyPrint(rec));
		}

		// test update
		ClientVariable var6 = createNewInstance(var2);
		ClientVariablePK pk6 = new ClientVariablePK(pk2.getClientData(), pk2.getVariableName()+"_v6", pk2.getStartTime());
		var6.setClientVariablePK(pk6);
		service.insert(var6);
		assertNotNull(service.getByPrimaryKey(pk6));
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
