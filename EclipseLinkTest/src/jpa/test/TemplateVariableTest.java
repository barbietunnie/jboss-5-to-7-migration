package jpa.test;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import jpa.constant.VariableType;
import jpa.model.ClientData;
import jpa.model.ClientVariable;
import jpa.model.CustomerData;
import jpa.model.TemplateData;
import jpa.model.TemplateDataPK;
import jpa.model.TemplateVariable;
import jpa.model.TemplateVariablePK;
import jpa.model.UserData;
import jpa.service.ClientDataService;
import jpa.service.TemplateDataService;
import jpa.service.TemplateVariableService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class TemplateVariableTest {
	static Logger logger = Logger.getLogger(TemplateVariableTest.class);
	
	final String testTemplateId = "jpa test template id";
	final String testClientId = Constants.DEFAULT_CLIENTID;
	final String testVariableId = "jpa test variable id";
	final String testVariableName = "jpa test variable name";
	
	@BeforeClass
	public static void TemplateVariablePrepare() {
	}

	@Autowired
	TemplateVariableService service;
	@Autowired
	TemplateDataService templateService;
	@Autowired
	ClientDataService clientService;
	
	private TemplateData tmp0;
	private ClientData cd0;
	
	@Before
	public void prepare() {
		ClientData client = clientService.getByClientId(testClientId);
		
		cd0 = new ClientData();
		try {
			BeanUtils.copyProperties(cd0, client);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		cd0.setClientId(Constants.DEFAULT_CLIENTID + "_v2");
		cd0.setClientVariables(new ArrayList<ClientVariable>());
		cd0.setCustomers(new ArrayList<CustomerData>());
		cd0.setUserDatas(new ArrayList<UserData>());
		clientService.insert(cd0);

		TemplateDataPK pk0 = new TemplateDataPK(cd0, testTemplateId, new Timestamp(System.currentTimeMillis()));
		tmp0 = new TemplateData();
		tmp0.setTemplateDataPK(pk0);
		tmp0.setContentType("text/plain");
		tmp0.setBodyTemplate("jpa test template value");
		tmp0.setSubjectTemplate("jpa test subject");
		templateService.insert(tmp0);
	}

	@After
	public void teardown() {
		templateService.delete(tmp0);
	}

	@Test
	public void templateDataService() {
		Timestamp tms = new Timestamp(System.currentTimeMillis());
		
		TemplateVariablePK pk0 = new TemplateVariablePK(cd0, testVariableId, testVariableName, tms);
		TemplateVariable rcd1 = new TemplateVariable();
		rcd1.setTemplateVariablePK(pk0);
		rcd1.setVariableType(VariableType.TEXT.getValue());
		rcd1.setVariableValue("jpa test variable value");
		service.insert(rcd1);
		
		TemplateVariable var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		System.out.println("TemplateVariable: " + StringUtil.prettyPrint(var1,2));

		TemplateVariablePK pk1 = var1.getTemplateVariablePK();
		TemplateVariable var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));

		List<TemplateVariable> list1 = service.getByVariableId(pk1.getVariableId());
		assertFalse(list1.isEmpty());
		
		list1 = service.getCurrentByVariableId(pk1.getVariableId());
		assertFalse(list1.isEmpty());
		
		List<TemplateVariable> list2 = service.getCurrentByClientId(testClientId);
		assertFalse(list2.isEmpty());
		for (TemplateVariable rec : list2) {
			logger.info(StringUtil.prettyPrint(rec,2));
		}

		// test insert
		Timestamp newTms = new Timestamp(System.currentTimeMillis()+1000);
		TemplateVariable var3 = createNewInstance(var2);
		TemplateVariablePK pk2 = var2.getTemplateVariablePK();
		TemplateVariablePK pk3 = new TemplateVariablePK(pk2.getClientData(), pk2.getVariableId(), pk2.getVariableName(), newTms);
		var3.setTemplateVariablePK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		try {
			service.getByPrimaryKey(pk3);
			fail();
		}
		catch (NoResultException e) {}

		// test deleteByVariableName
		TemplateVariable var4 = createNewInstance(var2);
		TemplateVariablePK pk4 = new TemplateVariablePK(pk2.getClientData(), pk2.getVariableId(), pk2.getVariableName()+"_v4", pk2.getStartTime());
		var4.setTemplateVariablePK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(pk4.getVariableName()));
		try {
			service.getByPrimaryKey(pk4);
			fail();
		}
		catch (NoResultException e) {}

		// test deleteByPrimaryKey
		TemplateVariable var5 = createNewInstance(var2);
		TemplateVariablePK pk5 = new TemplateVariablePK(pk2.getClientData(), pk2.getVariableId(), pk2.getVariableName()+"_v5", pk2.getStartTime());
		var5.setTemplateVariablePK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		try {
			service.getByPrimaryKey(pk5);
			fail();
		}
		catch (NoResultException e) {}

		// test deleteVariableId
		TemplateVariable var6 = createNewInstance(var2);
		TemplateVariablePK pk6 = new TemplateVariablePK(pk2.getClientData(), pk2.getVariableId(), pk2.getVariableName()+"_v6", pk2.getStartTime());
		var6.setTemplateVariablePK(pk6);
		service.insert(var6);
		int rowsDeleted = service.deleteByVariableId(pk6.getVariableId());
		assertTrue(1<=rowsDeleted);
		try {
			service.getByPrimaryKey(pk6);
			fail();
		}
		catch (NoResultException e) {}
		
		// test deleteClientId
		TemplateVariable var7 = createNewInstance(var2);
		TemplateVariablePK pk7 = new TemplateVariablePK(pk2.getClientData(), pk2.getVariableId(), pk2.getVariableName()+"_v7", pk2.getStartTime());
		var7.setTemplateVariablePK(pk7);
		service.insert(var7);
		rowsDeleted = service.deleteByClientId(pk7.getClientData().getClientId());
		assertTrue(1<=rowsDeleted);
		try {
			service.getByPrimaryKey(pk6);
			fail();
		}
		catch (NoResultException e) {}
		
		// test update
		TemplateVariable var9 = createNewInstance(var2);
		TemplateVariablePK pk9 = new TemplateVariablePK(pk2.getClientData(), pk2.getVariableId(), pk2.getVariableName()+"_v6", pk2.getStartTime());
		var9.setTemplateVariablePK(pk9);
		service.insert(var9);
		assertNotNull(service.getByPrimaryKey(pk9));
		var9.setVariableValue("new test value");
		service.update(var9);
		TemplateVariable var_updt = service.getByRowId(var9.getRowId());
		assertTrue("new test value".equals(var_updt.getVariableValue()));
		// end of test update
		
		service.delete(var9);
		try {
			service.getByRowId(var9.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}
	}

	private TemplateVariable createNewInstance(TemplateVariable orig) {
		TemplateVariable dest = new TemplateVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
