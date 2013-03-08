package jpa.test.msgout;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpa.message.MessageBean;
import jpa.model.message.MessageRendered;
import jpa.model.message.RenderVariable;
import jpa.service.message.MessageRenderedService;
import jpa.service.msgout.RenderBo;
import jpa.service.msgout.RenderRequest;
import jpa.service.msgout.RenderResponse;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;
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
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
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
			if (StringUtils.isNotBlank(bean.getSenderId())) {
				assertTrue(bean.getSenderId().equals(mr.getSenderData().getSenderId()));
			}
			assertTrue(bean.getFromAsString().equals(mr.getMessageSource().getFromAddress().getAddress()));
			assertTrue(bean.getCarrierCode().getValue().equals(mr.getMessageSource().getCarrierCode()));
			System.out.println("subject: " + bean.getSubject());
			System.out.println("body: " + bean.getBody());
			Renderer renderer = Renderer.getInstance();
			String bodyTmptl = mr.getMessageTemplate().getBodyTemplate();
			String subjTmptl = mr.getMessageTemplate().getSubjectTemplate();
			List<RenderVariable> varbles = mr.getRenderVariableList();
			Map<String, RenderVariableVo> map = RenderBo.renderVariablesToMap(varbles);
			Map<String, ErrorVariableVo> errors = new HashMap<String, ErrorVariableVo>();
			assertTrue(bean.getBody().equals(renderer.render(bodyTmptl, map, errors)));
			assertTrue(bean.getSubject().equals(renderer.render(subjTmptl, map, errors)));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		RenderRequest req = service.getRenderRequestByPK(mr.getRowId());
		assertNotNull(req);
		assertTrue(req.getMsgSourceId().equals(mr.getMessageSource().getMsgSourceId()));
		assertTrue(req.getSenderId().equals(mr.getSenderData().getSenderId()));
		assertTrue(req.getVariableOverrides().size()>=mr.getRenderVariableList().size());
		try {
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			int renderId = service.saveRenderData(rsp);
			MessageRendered mr2 = renderedService.getByPrimaryKey(renderId);
			assertTrue(mr2.getSenderDataRowId().equals(mr.getSenderDataRowId()));
			assertTrue(mr2.getMessageSourceRowId()==mr.getMessageSourceRowId());
			assertTrue(mr2.getMessageTemplateRowId()==mr.getMessageTemplateRowId());
			assertTrue(mr2.getRenderAttachmentList().size()==mr.getRenderAttachmentList().size());
			assertTrue(mr2.getRenderVariableList().size()>=mr.getRenderVariableList().size());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
