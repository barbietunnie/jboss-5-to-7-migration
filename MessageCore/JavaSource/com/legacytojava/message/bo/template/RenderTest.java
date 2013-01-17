package com.legacytojava.message.bo.template;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.VariableType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-jmsqueue_rmt-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class RenderTest {
	static final Logger logger = Logger.getLogger(RenderTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	@Resource
	private RenderBo util;
	@Test
	public void testRender1() throws Exception {
		try {
			RenderRequest req = new RenderRequest(
					"testMsgSource",
					Constants.DEFAULT_CLIENTID,
					new Timestamp(new java.util.Date().getTime()),
					buildTestVariables()
					);
			RenderResponse rsp = util.getRenderedEmail(req);
			assertNotNull(rsp);
			logger.info(rsp);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	@Test
	public void testRender2() throws Exception {
		try {
			RenderRequest req = new RenderRequest(
					"WeekendDeals",
					Constants.DEFAULT_CLIENTID,
					new Timestamp(new java.util.Date().getTime()),
					new HashMap<String, RenderVariable>()
					);
			RenderResponse rsp = util.getRenderedEmail(req);
			assertNotNull(rsp);
			logger.info(rsp);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private static HashMap<String, RenderVariable> buildTestVariables() {
		HashMap<String, RenderVariable> map=new HashMap<String, RenderVariable>();
		
		try {
			RenderVariable toAddr = new RenderVariable(
					EmailAddressType.TO_ADDR, 
					new InternetAddress("testto@localhost"),
					null, 
					VariableType.ADDRESS, 
					"Y",
					"N", 
					null
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
				"N", 
				null
			);
		RenderVariable req2 = new RenderVariable(
				"name2", 
				"Rendered User2", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Rendered User3", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req4 = new RenderVariable(
				"name4", 
				"Recursive Variable ${name1} End", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req5 = new RenderVariable(
				"name5", 
				"Rendered User5", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"attachment1.txt", 
				"Attachment Text ============================================", 
				"text/plain", 
				VariableType.LOB, 
				"Y",
				"N", 
				null
			);
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		java.net.URL url = loader.getResource("jndi.properties");
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
					"text/plain",
					VariableType.LOB, 
					"Y",
					"N", 
					null
				);
			map.put("attachment2", req6_2);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
		}
		
		// build a Collection for Table
		RenderVariable req2_row1 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 1", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req2_row2 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 2", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		ArrayList<HashMap<String, RenderVariable>> collection = new ArrayList<HashMap<String, RenderVariable>>();
		HashMap<String, RenderVariable> row1 = new HashMap<String, RenderVariable>();	// a row
		row1.put(req2.getVariableName(), req2_row1);
		row1.put(req3.getVariableName(), req3);
		collection.add(row1);
		HashMap<String, RenderVariable> row2 = new HashMap<String, RenderVariable>();	// a row
		row2.put(req2.getVariableName(), req2_row2);
		row2.put(req3.getVariableName(), req3);
		collection.add(row2);
		RenderVariable array = new RenderVariable(
				Renderer.TableVariableName, 
				collection, 
				null, 
				VariableType.COLLECTION, 
				"Y",
				"N", 
				null
			);
		// end of Collection
		
		map.put("name1", req1);
		map.put("name2", req2);
		map.put("name3", req3);
		map.put("name4", req4);
		map.put("name5", req5);
		map.put("attachment1", req6_1);
		map.put(Renderer.TableVariableName, array);
		
		return map;
	}
	
}
