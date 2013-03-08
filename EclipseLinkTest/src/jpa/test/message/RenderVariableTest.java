package jpa.test.message;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.model.SenderData;
import jpa.model.message.MessageRendered;
import jpa.model.message.MessageSource;
import jpa.model.message.RenderVariable;
import jpa.model.message.RenderVariablePK;
import jpa.service.SenderDataService;
import jpa.service.message.MessageRenderedService;
import jpa.service.message.RenderVariableService;
import jpa.service.message.MessageSourceService;
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
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class RenderVariableTest {

	@BeforeClass
	public static void RenderVariablePrepare() {
	}

	@Autowired
	RenderVariableService service;
	@Autowired
	MessageRenderedService renderedService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	MessageSourceService sourceService;

	MessageRendered mrn1;
	MessageRendered mrn2;
	
	@Before
	public void prepare() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		List<MessageSource> srcs = sourceService.getAll();
		assertFalse(srcs.isEmpty());
		MessageSource src1 = srcs.get(0);

		mrn1 = new MessageRendered();
		mrn1.setMessageSourceRowId(src1.getRowId());
		mrn1.setMessageTemplateRowId(src1.getTemplateData().getRowId());
		mrn1.setStartTime(updtTime);
		mrn1.setSenderDataRowId(sender.getRowId());
		mrn1.setSubscriberDataRowId(null);
		mrn1.setPurgeAfter(null);
		renderedService.insert(mrn1);

		mrn2 = new MessageRendered();
		try {
			BeanUtils.copyProperties(mrn2, mrn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		renderedService.insert(mrn2);
	}
	
	@Test
	public void renderVariableService() {
		// test insert
		RenderVariable in1 = new RenderVariable();
		RenderVariablePK pk1 = new RenderVariablePK(mrn1,"jpa test variable 1");
		in1.setRenderVariablePK(pk1);
		in1.setVariableType(VariableType.TEXT.getValue());
		in1.setVariableValue("jpa test variable value 1");
		service.insert(in1);
		
		RenderVariable msg1 = service.getByPrimaryKey(in1.getRenderVariablePK());
		System.out.println(StringUtil.prettyPrint(msg1,2));
		
		RenderVariable in2 = new RenderVariable();
		try {
			BeanUtils.copyProperties(in2, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderVariablePK pk2 = new RenderVariablePK(mrn1,"jpa test variable 2");
		in2.setRenderVariablePK(pk2);
		service.insert(in2);
		
		RenderVariable in3 = new RenderVariable();
		try {
			BeanUtils.copyProperties(in3, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderVariablePK pk3 = new RenderVariablePK(mrn1,"jpa test variable 3");
		in3.setRenderVariablePK(pk3);
		service.insert(in3);

		RenderVariable msg2  =service.getByRowId(in2.getRowId());
		System.out.println(StringUtil.prettyPrint(msg2,1));
		
		List<RenderVariable> lst1 = service.getByRenderId(mrn1.getRowId());
		assertTrue(3==lst1.size());
		RenderVariable rv1 = lst1.get(0);
		
		rv1.setUpdtUserId("jpa test");
		service.update(rv1);
		RenderVariable rv2 = service.getByRowId(rv1.getRowId());
		assertTrue("jpa test".equals(rv2.getUpdtUserId()));
		
		service.delete(in2);
		try {
			service.getByPrimaryKey(in2.getRenderVariablePK());
			fail();
		}
		catch (NoResultException e) {}
		assertTrue(1==service.deleteByPrimaryKey(in1.getRenderVariablePK()));
		assertTrue(0==service.deleteByRowId(in1.getRowId()));
		assertTrue(1==service.deleteByRenderId(mrn1.getRowId()));
	}
}
