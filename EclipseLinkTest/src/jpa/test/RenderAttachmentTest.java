package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.List;

import javax.mail.Part;
import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.model.ClientData;
import jpa.model.message.MessageRendered;
import jpa.model.message.MessageSource;
import jpa.model.message.RenderAttachment;
import jpa.model.message.RenderAttachmentPK;
import jpa.service.ClientDataService;
import jpa.service.message.MessageRenderedService;
import jpa.service.message.MessageSourceService;
import jpa.service.message.RenderAttachmentService;
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
public class RenderAttachmentTest {

	@BeforeClass
	public static void RenderAttachmentPrepare() {
	}

	@Autowired
	RenderAttachmentService service;
	@Autowired
	MessageRenderedService renderedService;
	@Autowired
	ClientDataService clientService;
	@Autowired
	MessageSourceService sourceService;

	MessageRendered mrn1;
	MessageRendered mrn2;
	
	@Before
	public void prepare() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		List<MessageSource> srcs = sourceService.getAll();
		assertFalse(srcs.isEmpty());
		MessageSource src1 = srcs.get(0);

		mrn1 = new MessageRendered();
		mrn1.setMessageSourceRowId(src1.getRowId());
		mrn1.setMessageTemplateRowId(src1.getTemplateData().getRowId());
		mrn1.setStartTime(updtTime);
		mrn1.setClientDataRowId(client.getRowId());
		mrn1.setCustomerDataRowId(null);
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
	public void renderAttachmentService() {
		// test insert
		RenderAttachment in1 = new RenderAttachment();
		RenderAttachmentPK pk1 = new RenderAttachmentPK(mrn1,1);
		in1.setRenderAttachmentPK(pk1);
		in1.setAttachmentType(Part.INLINE);
		in1.setAttachmentName("jpa1.txt");
		in1.setAttachmentValue("jpa test attachment value 1".getBytes());
		service.insert(in1);
		
		RenderAttachment msg1 = service.getByPrimaryKey(in1.getRenderAttachmentPK());
		System.out.println(StringUtil.prettyPrint(msg1,2));
		
		RenderAttachment in2 = new RenderAttachment();
		try {
			BeanUtils.copyProperties(in2, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderAttachmentPK pk2 = new RenderAttachmentPK(mrn1,2);
		in2.setRenderAttachmentPK(pk2);
		service.insert(in2);
		
		RenderAttachment in3 = new RenderAttachment();
		try {
			BeanUtils.copyProperties(in3, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderAttachmentPK pk3 = new RenderAttachmentPK(mrn1,3);
		in3.setRenderAttachmentPK(pk3);
		service.insert(in3);

		RenderAttachment msg2  =service.getByRowId(in2.getRowId());
		System.out.println(StringUtil.prettyPrint(msg2,1));
		
		List<RenderAttachment> lst1 = service.getByRenderId(mrn1.getRowId());
		assertTrue(3==lst1.size());
		RenderAttachment rv1 = lst1.get(0);
		
		rv1.setUpdtUserId("jpa test");
		service.update(rv1);
		RenderAttachment rv2 = service.getByRowId(rv1.getRowId());
		assertTrue("jpa test".equals(rv2.getUpdtUserId()));
		
		service.delete(in2);
		try {
			service.getByPrimaryKey(in2.getRenderAttachmentPK());
			fail();
		}
		catch (NoResultException e) {}
		assertTrue(1==service.deleteByPrimaryKey(in1.getRenderAttachmentPK()));
		assertTrue(0==service.deleteByRowId(in1.getRowId()));
		assertTrue(1==service.deleteByRenderId(mrn1.getRowId()));
	}
}
