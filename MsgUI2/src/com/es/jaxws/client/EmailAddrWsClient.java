package com.es.jaxws.client;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import jpa.util.StringUtil;

import org.apache.log4j.Logger;

import com.es.ejb.emailaddr.EmailAddrVo;
import com.es.ejb.emailaddr.EmailAddrWs;

public class EmailAddrWsClient {
	protected final static Logger logger = Logger.getLogger(EmailAddrWsClient.class);
	
	public static void main(String[] args) {
		testEmailAddrWs();
	}
	
	static void testEmailAddrWs() {
		try {
			Service service = Service.create(new URL("http://localhost:8181/MsgUI2/webservices/EmailAddr?wsdl"),
				new QName("http://com.es.ws.emailaddr/wsdl", "EmailAddrService"));
			assertNotNull(service);
			EmailAddrWs addr = service.getPort(EmailAddrWs.class);
			EmailAddrVo vo = addr.findByAddress("test@test.com");
			assertNotNull(vo);
			logger.info(StringUtil.prettyPrint(vo));
			vo = addr.findByAddress("emailaddr@soapws.test");
			assertNotNull(vo);
			int rows = addr.deleteByAddress(vo.getAddress());
			assert(rows > 0);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
