package com.es.jaxrs.client;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jpa.util.FileUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.log4j.Logger;

import com.es.tomee.util.JaxrsUtil;
import com.es.tomee.util.TomeeCtxUtil;

public class MailingListClient {
	protected final static Logger logger = Logger.getLogger(MailingListClient.class);
	
	public static void main(String[] args) {
		try {
			testMultipart("uploadpart");
			testMultipart("uploadfile");
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	static void testMultipart(String part) throws IOException {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		String uriStr = "http://localhost:" + port + "/MsgRest/msgapi/mailinglist/" + part;
		WebClient client = WebClient.create(uriStr);
		client.type("multipart/form-data").accept("multipart/mixed");
		List<Attachment> atts = new LinkedList<Attachment>();
		byte[] txtfile = FileUtil.loadFromFile("META-INF", "openejb.xml");
		atts.add(new Attachment("root", "text/xml", txtfile));
		if (StringUtils.contains(part, "part")) {
			byte[] txtfile2 = FileUtil.loadFromFile("META-INF", "ejb-jar.xml");
			atts.add(new Attachment("fileUpload", "text/xml", txtfile2));
		}
		Collection<?> attlist = client.postAndGetCollection(atts, Attachment.class);
		for (Object obj : attlist) {
			if (!(obj instanceof Attachment)) {
				logger.warn("Not an Attachment, skip.");
				continue;
			}
			Attachment att = (Attachment)obj;
			byte[] content = JaxrsUtil.getBytesFromDataHandler(att.getDataHandler());
			boolean isTextContent = StringUtils.contains(att.getDataHandler().getContentType().toString(), "text");
			logger.info("Content type: " + att.getContentType() + ", id: " + att.getContentId());
			if (isTextContent) {
				logger.info("     Content: " + new String(content));
			}
		}
	}

}
