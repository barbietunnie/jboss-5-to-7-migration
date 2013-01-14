package com.legacytojava.message.ejb.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.CreateException;
import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.naming.NamingException;

import com.legacytojava.message.bo.template.RenderRequest;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.bo.template.RenderVariable;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.VariableName;
import com.legacytojava.message.constant.VariableType;
import com.legacytojava.message.ejb.sendmail.RenderRemote;
import com.legacytojava.message.ejb.sendmail.SendMailRemote;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.LookupUtil;

/**
 * this class tests both SendMail and Render EJB's
 */
public class SendMailClient {
	public static void main(String[] args){
		try {
			SendMailClient sendMailClient = new SendMailClient();
			sendMailClient.invokeEJBs();
			sendMailClient.invokeEJB2();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	void invokeEJBs() throws CreateException, DataValidationException, ParseException,
			MessagingException, JMSException, IOException {
		RenderRemote render = (RenderRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/Render!com.legacytojava.message.ejb.sendmail.RenderRemote");
		
		RenderRequest req = new RenderRequest(
				"testMsgSource",
				Constants.DEFAULT_CLIENTID,
				new Timestamp(new java.util.Date().getTime()),
				buildTestVariables()
				);
		RenderResponse rsp = render.getRenderedEmail(req);

		SendMailRemote sendMail = (SendMailRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/SendMail!com.legacytojava.message.ejb.sendmail.SendMailRemote");
		
		sendMail.saveRenderData(rsp);
		System.out.println("MessageBean returned: "+rsp.getMessageBean());
		sendMail.sendMail(rsp.getMessageBean());
	}
	
	void invokeEJB2() throws NamingException, CreateException, DataValidationException,
			MessagingException, JMSException, IOException {
		SendMailRemote sendMail = (SendMailRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/SendMail!com.legacytojava.message.ejb.sendmail.SendMailRemote");

		long emailsSent = sendMail.sendMail("testfrom@test.com", "test@test.com",
				"test subject from SendMailBean", "Test Body Text");
		System.out.println("Emails Sent: " + emailsSent);
	}

	private HashMap<String, RenderVariable> buildTestVariables() {
		String TableVariableName="Table";
		
		HashMap<String, RenderVariable> map=new HashMap<String, RenderVariable>();
		
		RenderVariable toAddr = new RenderVariable(
				EmailAddressType.TO_ADDR, 
				"testto@localhost",
				null, 
				VariableType.ADDRESS, 
				"Y",
				"N", 
				null
			);
		map.put(toAddr.getVariableName(), toAddr);
		
		RenderVariable customer = new RenderVariable(
				VariableName.CUSTOMER_ID, 
				"test",
				"maximum 16 characters", 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		map.put(customer.getVariableName(), customer);
		
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
				"Jim Black", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Paul Teslyuk", 
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
				"Roy Livingston", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"attachment1.txt", 
				"Attachment Text ============================================", 
				"text/plain; charset=\"iso-8859-1\"", 
				VariableType.LOB, 
				"Y",
				"N", 
				null
			);
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		java.net.URL url = loader.getResource("jndi.properties");
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
					"N", 
					null
				);
			map.put("attachment2", req6_2);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// build a Collection for Table
		RenderVariable req2_row1 = new RenderVariable(
				"name2", 
				"Jim Black - Row 1", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req2_row2 = new RenderVariable(
				"name2", 
				"Jim Black - Row 2", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		ArrayList<HashMap<String, RenderVariable>> collection = new ArrayList<HashMap<String, RenderVariable>>();
		HashMap<String, RenderVariable> row1 = new HashMap<String, RenderVariable>();
		row1.put(req2.getVariableName(), req2_row1);
		row1.put(req3.getVariableName(), req3);
		collection.add(row1);
		HashMap<String, RenderVariable> row2 = new HashMap<String, RenderVariable>();
		row2.put(req2.getVariableName(), req2_row2);
		row2.put(req3.getVariableName(), req3);
		collection.add(row2);
		RenderVariable array = new RenderVariable(
				TableVariableName, 
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
		map.put(TableVariableName, array);
		
		return map;
	}
}
