package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.model.ClientData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageSource;
import jpa.model.message.TemplateData;
import jpa.model.message.TemplateDataPK;
import jpa.model.message.TemplateVariable;
import jpa.model.message.TemplateVariablePK;
import jpa.service.ClientDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageSourceService;
import jpa.service.message.TemplateDataService;
import jpa.service.message.TemplateVariableService;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageSourceTest {

	@BeforeClass
	public static void MessageSourcePrepare() {
	}

	@Autowired
	MessageSourceService service;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	TemplateDataService templateService;
	@Autowired
	TemplateVariableService variableService;
	@Autowired
	ClientDataService clientService;

	private TemplateData tmp1;
	private TemplateVariable var1;
	
	@Before
	public void prepare() {
		Timestamp tms = new Timestamp(System.currentTimeMillis());
		String testTemplateId = "jpa test template id";
		String testClientId = Constants.DEFAULT_CLIENTID;
		ClientData cd0 = clientService.getByClientId(testClientId);
		TemplateDataPK tpk0 = new TemplateDataPK(cd0, testTemplateId, tms);
		tmp1 = new TemplateData();
		tmp1.setTemplateDataPK(tpk0);
		tmp1.setContentType("text/plain");
		tmp1.setBodyTemplate("jpa test template value");
		tmp1.setSubjectTemplate("jpa test subject");
		templateService.insert(tmp1);

		String testVariableId = "jpa test variable id";
		String testVariableName = "jpa test variable name";
		TemplateVariablePK vpk0 = new TemplateVariablePK(cd0, testVariableId, testVariableName, tms);
		var1 = new TemplateVariable();
		var1.setTemplateVariablePK(vpk0);
		var1.setVariableType(VariableType.TEXT.getValue());
		var1.setVariableValue("jpa test variable value");
		variableService.insert(var1);
	}
	
	private String testMsgSourceId = "test jpa msgsource id";
	
	@Test
	public void messageSourceService() {
		EmailAddress adr1 = addrService.findSertAddress("jsmith@test.com");
		MessageSource src1 = new MessageSource();
		src1.setMsgSourceId(testMsgSourceId);
		src1.setTemplateData(tmp1);
		src1.getTemplateVariableList().add(var1);
		src1.setFromAddress(adr1);
		service.insert(src1);
		
		List<MessageSource> list = service.getAll();
		assertFalse(list.isEmpty());
		
		MessageSource tkn0 = service.getByMsgSourceId(testMsgSourceId);
		assertNotNull(tkn0);
		assertFalse(tkn0.getTemplateVariableList().isEmpty());
		System.out.println(StringUtil.prettyPrint(tkn0,2));
		
		List<MessageSource> lst2 = service.getByFromAddress(adr1.getAddress());
		assertFalse(lst2.isEmpty());

		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		MessageSource tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		MessageSource tkn2 = new MessageSource();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setMsgSourceId(tkn1.getMsgSourceId()+"_v2");
		// to prevent "found shared references to a collection" error from Hibernate
		tkn2.setTemplateVariableList(null);
		service.insert(tkn2);
		
		MessageSource tkn3 = service.getByMsgSourceId(tkn2.getMsgSourceId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with NoResultException
		service.delete(tkn3);
		try {
			service.getByMsgSourceId(tkn2.getMsgSourceId());
			fail();
		}
		catch (NoResultException e) {
		}
		
		assertTrue(0==service.deleteByMsgSourceId(tkn3.getMsgSourceId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
