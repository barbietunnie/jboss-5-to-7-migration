package com.pra.rave.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;
import org.cdisc.ns.odm.v1.ODM;
import org.slf4j.Logger;

import com.pra.rave.xml.util.XmlHelper;
import com.pra.util.Util;
import com.pra.util.logger.LoggerHelper;

public class RaveClinicalView {
	static Logger logger = LoggerHelper.getLogger();
	private Properties props = null;
	
	public static void main(String[] args) {
		RaveClinicalView client = new RaveClinicalView();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			java.util.Date dt = sdf.parse("2013-01-20T01:01:00");
			client.getClinicalViewByFormAsString("ADVERSE_1", new java.sql.Timestamp(dt.getTime()));
			//ODM odm = client.getClinicalViewByForm("SRF_1");
			//logger.info(StringUtil.prettyPrint(odm));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}

	private Properties loadProperties() {
		String resourceName = "META-INF/config/ravewsclient." + Util.getEnv() + ".properties";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream(resourceName);
		if (is == null) {
			throw new RuntimeException("Resource (" + resourceName + ") missing, contact programming!");
		}
		Properties prop = new Properties();
		try {
			// XXX use InputStreamReader to load UTF-8 characters
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			prop.load(isr);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
			throw new RuntimeException("Failed to load resource (" + resourceName + "), contact programming!");
		}
		return prop;
	}

	public ODM getClinicalViewByForm(String formOid, java.sql.Timestamp startTime) {
		WebClient client = getClinicalView(formOid, startTime);
		ODM odm = client.get(ODM.class);
		return odm;
	}

	public String getClinicalViewByFormAsString(String formOid, java.sql.Timestamp startTime) {
		WebClient client = getClinicalView(formOid, startTime);
		String xml = client.get(String.class);
		String xmlStr = XmlHelper.normalizeXml(xml);
		try {
			logger.info(XmlHelper.printXml(xmlStr));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			logger.info(xmlStr);
		}
		return xml;
	}

	private WebClient getClinicalView(String formOid, java.sql.Timestamp startTime) {
		if (props == null) {
			props = loadProperties();
			props.list(System.out);
		}
		WebClient client = WebClient.create(props.getProperty("rave.rws.url"));
		//String path = "/RaveWebServices/studies/ABP%20980–Lilac–20120283%20(IUAT)/datasets/regular/" + formOid;
		String path = props.getProperty("rave.studies.endpoint");
		path += "/" + props.getProperty("rave.study.name") + "/datasets/regular/" + formOid;
		try { // encode URI (change space to %20)
			java.net.URI uri = new java.net.URI(
					props.getProperty("rave.rws.scheme"),
					props.getProperty("rave.rws.host"), path, null);
			path = uri.getRawPath();
		}
		catch (URISyntaxException e) { // should never happen
			logger.error("URISyntaxException caught", e);
			throw new RuntimeException("URISyntaxException caught: " + e);
		} 
		logger.info("Path: {}", path);

		client.path(path);
		if (startTime != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			client.query("start",  sdf.format(startTime));
		}
		String authorizationHeader = "Basic " + Base64Utility.encode("pra_rws:Sn0f@11".getBytes());
		client.header("Authorization", authorizationHeader);
		client.type("text/xml").accept("text/xml");
		return client;
	}
	
	/*
	 * XXX require glassfish jersey jar files.
	 */
	void rsClient() {
		Client client = ClientFactory.newClient();
		client.configuration().register(ODM.class);
		WebTarget target = client.target("https://hdcvcl09-018.mdsol.com/RaveWebServices/studies/ABP%20980–Lilac–20120283%20(IUAT)//datasets/regular/SRF_1");
		String authorizationHeader = "Basic " 
			    + Base64Utility.encode("pra_rws:Sn0f@11".getBytes());
		client.configuration().setProperty("Authorization",authorizationHeader);
		Builder builder = target.request("text/xml");
		logger.info(builder.get(String.class));
		//builder.get(ODM.class);
	}

}
