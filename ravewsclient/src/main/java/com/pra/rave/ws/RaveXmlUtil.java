package com.pra.rave.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.cdisc.ns.odm.v1.ODM;
import org.slf4j.Logger;
import org.xml.sax.InputSource;

import com.pra.rave.xml.util.JaxbUtil;
import com.pra.rave.xml.util.StringUtil;
import com.pra.rave.xml.util.XmlHelper;
import com.pra.util.logger.LoggerHelper;

public class RaveXmlUtil {
	static Logger logger = LoggerHelper.getLogger();
	
	public static void main(String[] args) {
		RaveXmlUtil client = new RaveXmlUtil();
		try {
			ODM odm = client.unmarshall("xml/SRFDataset.xml");
			logger.info(StringUtil.prettyPrint(odm));
			String xml = client.marshall(odm);
			logger.info(XmlHelper.printXml(xml));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	
	ODM unmarshall(String filePath) throws JAXBException, IOException {
		String pkgName = "org.cdisc.ns.odm.v1";
		JAXBContext ctx = JaxbUtil.getJAXBContext(pkgName);
		Unmarshaller unmar = ctx.createUnmarshaller();
		InputSource source = XmlHelper.loadInputSourceFromFilePath(filePath);
		String xml = normalize(source);
		ByteArrayInputStream payload = new ByteArrayInputStream(xml.getBytes());
		ODM obj = (ODM) unmar.unmarshal(payload);
		return obj;
	}
	
	String marshall(ODM obj) throws JAXBException {
		String pkgName = "org.cdisc.ns.odm.v1";
		JAXBContext ctx = JaxbUtil.getJAXBContext(pkgName);
		Marshaller marsh = ctx.createMarshaller();
		StringWriter writer = new StringWriter();
		marsh.marshal(obj, writer);
		StringBuffer sb = writer.getBuffer();
		return sb.toString();
	}

	private String normalize(InputSource source) throws IOException {
		String xmlStr = sourceToString(source);
		return XmlHelper.normalizeXml(xmlStr);
	}

	private String sourceToString(InputSource source) throws IOException {
		/*
		 * XXX decode input stream using "UTF-8" charset will address this error:
		 * "Invalid byte 1 of 1-byte UTF-8 sequence".
		 */
		InputStreamReader isr = new InputStreamReader(source.getByteStream(),"UTF-8");
		StringBuffer sb = new StringBuffer();
		char[] buf = new char[1024];
		int len = 0;
		while ((len=isr.read(buf))>0) {
			sb.append(new String(buf,0,len));
		}
		return sb.toString();
	}

}
