package com.pra.rave.ws;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;
import org.cdisc.ns.odm.v1.ODM;
import org.slf4j.Logger;

import com.pra.rave.xml.util.StringUtil;
import com.pra.rave.xml.util.XmlHelper;
import com.pra.util.logger.LoggerHelper;

public class RaveClinicalView {
	static Logger logger = LoggerHelper.getLogger();
	
	public static void main(String[] args) {
		RaveClinicalView client = new RaveClinicalView();
		try {
			//client.getClinicalViewByFormAsString("SRF_1");
			ODM odm = client.getClinicalViewByForm("SRF_1");
			logger.info(StringUtil.prettyPrint(odm));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}

	public ODM getClinicalViewByForm(String formOid) {
		WebClient client = getClinicalView(formOid);
		ODM odm = client.get(ODM.class);
		return odm;
	}

	public String getClinicalViewByFormAsString(String formOid) {
		WebClient client = getClinicalView(formOid);
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

	private WebClient getClinicalView(String formOid) {
		WebClient client = WebClient.create("https://hdcvcl09-018.mdsol.com");
		client.path("RaveWebServices/studies/ABP%20980–Lilac–20120283%20(IUAT)//datasets/regular/" + formOid);
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
