package com.es.ejb.mailsender;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.VariableType;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.EmailAddress;
import jpa.model.message.MessageRendered;
import jpa.service.maillist.RenderRequest;
import jpa.service.message.MessageRenderedService;
import jpa.service.msgout.MsgOutboxBo;
import jpa.util.FileUtil;
import jpa.util.SpringUtil;
import jpa.variable.RenderVariableVo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.tomee.util.TomeeCtxUtil;

public class MailSenderTest {
	protected final static Logger logger = Logger.getLogger(MailSenderTest.class);
	
	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		ejbContainer = EJBContainer.createEJBContainer();
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testMailSenderRemote() {
		try {
			MailSenderRemote rmt = (MailSenderRemote) TomeeCtxUtil.getLocalContext().lookup(
					"java:global/MsgRest/MailSender!com.es.ejb.mailsender.MailSenderRemote");
			EmailAddress addr = rmt.findByAddress("test@test.com");
			assertNotNull(addr);
			
			try {
				EmailAddress ea = rmt.findByAddress("test@test.com");
				assertNotNull(ea);
			}
			catch (EJBException e) {
				//assertNotNull(e.getCause());
				//assert(e.getCause() instanceof NoResultException);
				fail();
			}
			
			try {
				rmt.send("testfrom@localhost", "testto@localhost", "Test from MailSender", "Message from MailSender EJB.");
			}
			catch (Exception e) {
				fail();
			}
		}
		catch (NamingException e) {
			fail();
		}
	}

	@Test
	public void testMailSenderLocal() {
		try {
			MailSenderLocal sender = (MailSenderLocal) TomeeCtxUtil.getLocalContext().lookup(
					"java:global/MsgRest/MailSender!com.es.ejb.mailsender.MailSenderLocal");
			EmailAddress addr = sender.findByAddress("test@test.com");
			assertNotNull(addr);
			
			String filePath = "bouncedmails";
			String fileName = "BouncedMail_1.txt";
			try {
				byte[] mailStream = FileUtil.loadFromFile(filePath, fileName);
				MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
				msgBean.setSenderId(Constants.DEFAULT_SENDER_ID);
				sender.send(msgBean);
			}
			catch (Exception te) {
				logger.error("Exception caught", te);
				fail();
			}
			
			try {
				sender.sendMailToSite("", "testfrom@test.com", "test subject from MailSender EJB", "Test message");
			}
			catch (Exception e) {
				fail();
			}
			
			// test render function
			assertNotNull(SpringUtil.getAppContext());
			MessageRenderedService renderedService = SpringUtil.getAppContext().getBean(MessageRenderedService.class);
			MsgOutboxBo outboxBo = SpringUtil.getAppContext().getBean(MsgOutboxBo.class);
			MessageRendered mr = renderedService.getFirstRecord();
			RenderRequest req = outboxBo.getRenderRequestByPK(mr.getRowId());
			assertTrue(StringUtils.isNotBlank(req.getMsgSourceId()));
			assertTrue(StringUtils.isNotBlank(req.getSenderId()));
			RenderVariableVo vo = new RenderVariableVo(
					EmailAddrType.TO_ADDR.getValue(),
					"testto@test.com",
					VariableType.ADDRESS);
			req.getVariableOverrides().put(vo.getVariableName(), vo);
			try {
				int renderId = sender.renderAndSend(req);
				assert(renderId>0);
				MessageBean msgBean = sender.getMessageByRenderId(renderId);
				assertNotNull(msgBean);
				assertNotNull(msgBean.getRenderId());
				assert(renderId==msgBean.getRenderId());
				assert(StringUtils.equals(msgBean.getToAsString(), (String)vo.getVariableValue()));
			}
			catch (Exception e) {
				fail();
			}
		}
		catch (NamingException e) {
			fail();
		}
	}

}
