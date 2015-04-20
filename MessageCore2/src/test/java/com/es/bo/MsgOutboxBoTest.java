package com.es.bo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.outbox.MsgOutboxBo;
import com.es.bo.render.RenderRequest;
import com.es.bo.render.RenderResponse;
import com.es.bo.render.RenderVariable;
import com.es.bo.sender.RenderBo;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddrType;
import com.es.data.constant.VariableName;
import com.es.data.constant.VariableType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgOutboxBoTest {
	static final Logger logger = Logger.getLogger(MsgOutboxBoTest.class);
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgOutboxBo msgOutboxBo;
	@Resource
	private RenderBo renderBo;
	@BeforeClass
	public static void  MsgOutboxBoPrepare() {
	}
	@Test
	@Rollback(false) // must commit MsgRendered record for MailSender
	public void testMsgOutboxBo() {
		try {
			RenderRequest req = new RenderRequest(
					"testMsgSource",
					Constants.DEFAULT_SENDER_ID,
					new Timestamp(System.currentTimeMillis()),
					buildTestVariables()
					);
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			assertNotNull(rsp);
			logger.info("Renderer Body: ####################" + LF + rsp.getMessageBean().getBody());
			
			long msgId = msgOutboxBo.saveRenderData(rsp);
			assertTrue(msgId>0);
			
			RenderRequest req2 = msgOutboxBo.getRenderRequestByPK(msgId);
			assertNotNull(req2);
			logger.info("RenderRequest2: ####################"+LF+req2);
			RenderResponse rsp2 = renderBo.getRenderedEmail(req2);
			assertNotNull(rsp2);
			logger.info("RenderResponse2: ####################"+LF+rsp2);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private static Map<String, RenderVariable> buildTestVariables() {
		Map<String, RenderVariable> map=new HashMap<String, RenderVariable>();
		
		RenderVariable toAddr = new RenderVariable(
				EmailAddrType.TO_ADDR.getValue(), 
				"testto@localhost",
				null, 
				VariableType.ADDRESS, 
				"Y",
				Boolean.FALSE
			);
		map.put(toAddr.getVariableName(), toAddr);
		
		RenderVariable customer = new RenderVariable(
				VariableName.SUBSCRIBER_ID.getValue(), 
				"test",
				"maximum 16 characters", 
				VariableType.TEXT, 
				"Y",
				Boolean.FALSE
			);
		map.put(customer.getVariableName(), customer);
		
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
				"text/plain; charset=\"iso-8859-1\"", 
				VariableType.LOB, 
				"Y",
				Boolean.FALSE
			);
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		java.net.URL url = loader.getResource("samples/jndi.properties");
		try {
			Object content = url.getContent();
			byte[] buffer = null;
			if (content instanceof BufferedInputStream) {
				BufferedInputStream bis = (BufferedInputStream)content;
				int len = bis.available();
				buffer = new byte[len];
				bis.read(buffer);
				bis.close();
			}
			RenderVariable req6_2 = new RenderVariable(
					"jndi.bin",
					buffer,
					"application/octet-stream",
					VariableType.LOB, 
					"Y",
					Boolean.FALSE
				);
			map.put("attachment2", req6_2);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
		}
		
		map.put("name1", req1);
		map.put("name2", req2);
		map.put("name3", req3);
		map.put("name4", req4);
		map.put("name5", req5);
		map.put("attachment1", req6_1);
		
		return map;
	}
}
