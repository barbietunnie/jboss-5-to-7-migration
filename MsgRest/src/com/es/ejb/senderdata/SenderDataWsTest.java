package com.es.ejb.senderdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.constant.Constants;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.ws.vo.SenderDataVo;

public class SenderDataWsTest {

	protected final static Logger logger = Logger.getLogger(SenderDataWsTest.class);
	
	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		Properties properties = new Properties();
        properties.setProperty("openejb.embedded.remotable", "true");
        //properties.setProperty("httpejbd.print", "true");
        //properties.setProperty("httpejbd.indent.xml", "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSenderDataWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/SenderData?wsdl"),
				new QName("http://com.es.ws.senderdata/wsdl", "SenderDataService"));
			assertNotNull(service);
			SenderDataWs senderDao = service.getPort(SenderDataWs.class);
			
			List<SenderDataVo> list = senderDao.getAll();
			assert(!list.isEmpty());
			for (SenderDataVo vo : list) {
				logger.info(StringUtil.prettyPrint(vo));
			}
			SenderDataVo vo = senderDao.getBySenderId(Constants.DEFAULT_SENDER_ID);
			assertNotNull(vo);
			try {
				senderDao.getBySenderId("FakeSender");
				fail();
			}
			catch (SOAPFaultException se) {
				// expected
			}
			
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			vo.setUpdtTime(updtTime);
			senderDao.update(vo);
			vo = senderDao.getBySenderId(vo.getSenderId());
			assert(updtTime.equals(vo.getUpdtTime()));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}
