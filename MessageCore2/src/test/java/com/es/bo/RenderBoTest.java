package com.es.bo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.render.RenderRequest;
import com.es.bo.render.RenderResponse;
import com.es.bo.render.RenderVariable;
import com.es.bo.sender.RenderBo;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.VariableType;
import com.es.data.constant.XHeaderName;
import com.es.msgbean.BodypartBean;
import com.es.msgbean.BodypartUtil;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageNode;
import com.es.msgbean.MsgHeader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class RenderBoTest {
	static final Logger logger = Logger.getLogger(RenderBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	@Resource
	private RenderBo renderBo;

	@Test
	public void testRenderBo1() {
		try {
			RenderRequest req = new RenderRequest(
					"testMsgSource",
					Constants.DEFAULT_SENDER_ID,
					new Timestamp(new java.util.Date().getTime()),
					buildTestVariables()
					);
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			assertNotNull(rsp);
			logger.info("testRender1() - ####################" + LF + rsp);
			// verify rendered data
			verifyRenderedData(rsp);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testRenderBo2() {
		try {
			RenderRequest req = new RenderRequest(
					"WeekendDeals",
					Constants.DEFAULT_SENDER_ID,
					new Timestamp(new java.util.Date().getTime()),
					new HashMap<String, RenderVariable>()
					);
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			assertNotNull(rsp);
			logger.info("testRender2() - ####################" + LF + rsp);
			// verify rendered data
			MessageBean msgBean = rsp.getMessageBean();
			assertTrue("jsmith@test.com".equals(msgBean.getFromAsString()));
			assertTrue(CarrierCode.SMTPMAIL.equals(msgBean.getCarrierCode()));
			assertTrue(StringUtils.startsWith(msgBean.getSubject(), "Weekend Deals at MyBestDeals.com"));
			assertTrue(Constants.DEFAULT_SENDER_ID.equals(msgBean.getSenderId()));
			List<MsgHeader> headers = msgBean.getHeaders();
			boolean headerFound = false;
			for (MsgHeader header : headers) {
				if (XHeaderName.SENDER_ID.getValue().equals(header.getName())) {
					assertTrue(Constants.DEFAULT_SENDER_ID.equals(header.getValue()));
					headerFound = true;
					break;
				}
			}
			assertTrue(headerFound);
			assertTrue(StringUtils.contains(msgBean.getBody(), "Dear subscriber, here is a list"));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void verifyRenderedData(RenderResponse rsp) {
		MessageBean msgBean = rsp.getMessageBean();
		assertTrue("jsmith@test.com".equals(msgBean.getFromAsString()));
		assertTrue("testto@localhost".equals(msgBean.getToAsString()));
		assertTrue(CarrierCode.SMTPMAIL.equals(msgBean.getCarrierCode()));
		assertTrue("Test Template".equals(msgBean.getSubject()));
		assertTrue(Constants.DEFAULT_SENDER_ID.equals(msgBean.getSenderId()));
		List<MsgHeader> headers = msgBean.getHeaders();
		boolean headerFound = false;
		for (MsgHeader header : headers) {
			if (XHeaderName.SENDER_ID.getValue().equals(header.getName())) {
				assertTrue(Constants.DEFAULT_SENDER_ID.equals(header.getValue()));
				headerFound = true;
				break;
			}
		}
		assertTrue(headerFound);
		assertTrue(StringUtils.contains(msgBean.getBody(), "Recursive Variable Jack Wang End"));
		List<MessageNode> attachments = BodypartUtil.retrieveAttachments(msgBean);
		assertTrue(attachments.size()==2);
		MessageNode atc1 = attachments.get(0);
		BodypartBean bpt1 = atc1.getBodypartNode();
		assertTrue(StringUtils.startsWith(bpt1.getMimeType(), Constants.TEXT_PLAIN));
		assertTrue(StringUtils.equals(bpt1.getDescription(), "jndi.txt"));
		assertTrue(StringUtils.equals(bpt1.getDisposition(), "attachment"));
		String body1 = new String(bpt1.getValue());
		assertTrue(StringUtils.contains(body1, "# JBoss 7.1 jndi.properties"));
		MessageNode atc2 = attachments.get(1);
		BodypartBean bpt2 = atc2.getBodypartNode();
		assertTrue(StringUtils.startsWith(bpt2.getMimeType(), Constants.TEXT_PLAIN));
		assertTrue(StringUtils.equals(bpt2.getDescription(), "attachment1.txt"));
		assertTrue(StringUtils.equals(bpt2.getDisposition(), "attachment"));
		String body2 = new String(bpt2.getValue());
		assertTrue(StringUtils.contains(body2, "Attachment Text ===="));
	}

	private static Map<String, RenderVariable> buildTestVariables() {
		Map<String, RenderVariable> map=new HashMap<String, RenderVariable>();
		
		try {
			RenderVariable toAddr = new RenderVariable(
					EmailAddressType.TO_ADDR.getValue(), 
					new InternetAddress("testto@localhost"),
					null, 
					VariableType.ADDRESS, 
					"Y",
					Boolean.FALSE
				);
			map.put(toAddr.getVariableName(), toAddr);
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		
		RenderVariable req1 = new RenderVariable(
				"name1", 
				"Jack Wang", 
				null, 
				VariableType.TEXT, 
				"Y",
				Boolean.FALSE
			);
		RenderVariable req2 = new RenderVariable(
				"name2", 
				"Rendered User2", 
				null, 
				VariableType.TEXT, 
				"Y",
				Boolean.FALSE
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Rendered User3", 
				null, 
				VariableType.TEXT, 
				"Y",
				Boolean.FALSE
			);
		RenderVariable req4 = new RenderVariable(
				"name4", 
				"Recursive Variable ${name1} End", 
				null, 
				VariableType.TEXT, 
				"Y",
				Boolean.FALSE
			);
		RenderVariable req5 = new RenderVariable(
				"name5", 
				"Rendered User5", 
				null, 
				VariableType.TEXT, 
				"Y",
				Boolean.FALSE
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"attachment1.txt", 
				"Attachment Text ============================================", 
				Constants.TEXT_PLAIN, 
				VariableType.LOB, 
				"Y",
				Boolean.FALSE
			);
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		java.net.URL url = loader.getResource("samples/jndi.properties");
		try {
			//url = new java.net.URL("http://www.eos.ncsu.edu/soc/");
			Object content = url.getContent();
			if (isDebugEnabled)
				logger.debug("AttachmentValue DataType: "+content.getClass().getName());
			byte[] buffer = null;
			if (content instanceof BufferedInputStream) {
				BufferedInputStream bis = (BufferedInputStream)content;
				int len = bis.available();
				buffer = new byte[len];
				bis.read(buffer);
				bis.close();
			}
			else if (content instanceof InputStream) {
				BufferedInputStream bis = new BufferedInputStream((InputStream)content);
				int len = bis.available();
				buffer = new byte[len];
				bis.read(buffer);
				bis.close();
			}
			RenderVariable req6_2 = new RenderVariable(
					"jndi.txt",
					buffer,
					Constants.TEXT_PLAIN,
					VariableType.LOB, 
					"Y",
					Boolean.FALSE
				);
			map.put("attachment2", req6_2);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
		}
		
		// build a Collection for Table
//		RenderVariable req2_row1 = new RenderVariable(
//				"name2", 
//				"Rendered User2 - Row 1", 
//				null, 
//				VariableType.TEXT, 
//				"Y",
//				Boolean.FALSE
//			);
//		RenderVariable req2_row2 = new RenderVariable(
//				"name2", 
//				"Rendered User2 - Row 2", 
//				null, 
//				VariableType.TEXT, 
//				"Y",
//				Boolean.FALSE
//			);
//		ArrayList<Map<String, RenderVariable>> collection = new ArrayList<Map<String, RenderVariable>>();
//		Map<String, RenderVariable> row1 = new HashMap<String, RenderVariable>();	// a row
//		row1.put(req2.getVariableName(), req2_row1);
//		row1.put(req3.getVariableName(), req3);
//		collection.add(row1);
//		Map<String, RenderVariable> row2 = new HashMap<String, RenderVariable>();	// a row
//		row2.put(req2.getVariableName(), req2_row2);
//		row2.put(req3.getVariableName(), req3);
//		collection.add(row2);
		// end of Collection
		
		map.put("name1", req1);
		map.put("name2", req2);
		map.put("name3", req3);
		map.put("name4", req4);
		map.put("name5", req5);
		map.put("attachment1", req6_1);
		
		return map;
	}
	
}
