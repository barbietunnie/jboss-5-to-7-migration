package jpa.test.message;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.model.ClientData;
import jpa.model.message.MessageRendered;
import jpa.model.message.MessageSource;
import jpa.service.ClientDataService;
import jpa.service.message.MessageRenderedService;
import jpa.service.message.MessageSourceService;
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
public class MessageRenderedTest {

	@BeforeClass
	public static void MessageRenderedPrepare() {
	}

	@Autowired
	MessageRenderedService service;
	@Autowired
	ClientDataService clientService;
	@Autowired
	MessageSourceService sourceService;

	@Test
	public void messageRenderedService() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		List<MessageSource> srcs = sourceService.getAll();
		assertFalse(srcs.isEmpty());
		MessageSource src1 = srcs.get(0);

		MessageRendered in1 = new MessageRendered();
		in1.setMessageSourceRowId(src1.getRowId());
		in1.setMessageTemplateRowId(src1.getTemplateData().getRowId());
		in1.setStartTime(updtTime);
		in1.setClientDataRowId(client.getRowId());
		in1.setCustomerDataRowId(null);
		in1.setPurgeAfter(null);
		service.insert(in1);
		
		MessageRendered msg1 = service.getByPrimaryKey(in1.getRowId());
		System.out.println(StringUtil.prettyPrint(msg1,2));
		
		MessageRendered in2 = new MessageRendered();
		try {
			BeanUtils.copyProperties(in2, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		service.insert(in2);
		
		MessageRendered msg2  =service.getLastRecord();
		System.out.println(StringUtil.prettyPrint(msg2,2));
		MessageRendered msg22  =service.getAllDataByPrimaryKey(msg2.getRowId());
		System.out.println(StringUtil.prettyPrint(msg22,2));
		
		try {
			service.getNextRecord(msg2);
			fail();
		}
		catch (NoResultException e) {}
		
		try {
			MessageRendered msg3  =service.getPrevoiusRecord(msg2);
			System.out.println(StringUtil.prettyPrint(msg3,1));
			
			assertTrue(msg1.equals(msg3));
	
			MessageRendered msg4  =service.getNextRecord(msg3);
			assertTrue(msg2.equals(msg4));
		}
		catch (NoResultException e) {
			assertTrue("MessageRendered table is empty", true);
		}
	}
}
