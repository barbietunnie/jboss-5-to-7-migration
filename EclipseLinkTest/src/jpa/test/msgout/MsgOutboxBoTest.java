package jpa.test.msgout;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.message.MessageRendered;
import jpa.service.message.MessageRenderedService;
import jpa.service.msgout.RenderBo;
import jpa.service.msgout.RenderRequest;
import jpa.service.msgout.RenderResponse;
import jpa.variable.Renderer;

import org.apache.commons.lang3.StringUtils;
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
public class MsgOutboxBoTest {

	@BeforeClass
	public static void MsgOutboxBoPrepare() {
	}

	@Autowired
	jpa.service.msgout.MsgOutboxBo service;
	@Autowired
	RenderBo renderBo;
	@Autowired
	MessageRenderedService renderedService;

	@Test
	public void msgOutboxBoService() {
		MessageRendered mr = renderedService.getFirstRecord();
		mr = renderedService.getAllDataByPrimaryKey(mr.getRowId());
		
		try {
			MessageBean bean = service.getMessageByPK(mr.getRowId());
			System.out.println("MessageBean retrieved:\n" + bean);
			assertTrue(bean.getRenderId()!=null && mr.getRowId()==bean.getRenderId());
			if (StringUtils.isNotBlank(bean.getClientId())) {
				assertTrue(bean.getClientId().equals(mr.getClientData().getClientId()));
			}
			assertTrue(bean.getFromAsString().equals(mr.getMessageSource().getFromAddress().getAddress()));
			assertTrue(bean.getCarrierCode().getValue().equals(mr.getMessageSource().getCarrierCode()));
			System.out.println("subject: " + bean.getSubject());
			System.out.println("body: " + bean.getBody());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		RenderRequest renderRequest = service.getRenderRequestByPK(mr.getRowId());
		if (renderRequest == null) { // should never happen
			throw new DataValidationException("RenderRequest is null for RenderId: " + mr.getRowId());
		}
		try {
			RenderResponse rsp = renderBo.getRenderedEmail(renderRequest);
			service.saveRenderData(rsp);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
