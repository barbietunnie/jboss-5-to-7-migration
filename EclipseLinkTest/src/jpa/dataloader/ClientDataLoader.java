package jpa.dataloader;

import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.ClientData;
import jpa.service.ClientDataService;
import jpa.util.ProductUtil;
import jpa.util.SpringUtil;
import jpa.util.TimestampUtil;

public class ClientDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(ClientDataLoader.class);
	private ClientDataService service;

	public static void main(String[] args) {
		ClientDataLoader loader = new ClientDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		startTransaction();
		try {
			loadClientData(true);
			loadJBatchData();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadClientData(boolean loadTestData) {
		ClientData data = new ClientData();
		data.setClientId(Constants.DEFAULT_CLIENTID);
		data.setClientName("Emailsphere Demo");
		if (loadTestData)
			data.setDomainName("localhost"); // domain name
		else
			data.setDomainName("espheredemo.com"); // domain name
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId("0000000000");
		data.setWebSiteUrl("http://localhost:8080/MsgUI/publicsite");
		data.setSaveRawMsg(true); // save raw stream
		if (loadTestData) {
			data.setVirusCntrlEmail("sitemaster@emailsphere.com");
			data.setSecurityEmail("security@localhost");
			data.setCustcareEmail("custcare@localhost");
			data.setRmaDeptEmail("rma.dept@localhost");
			data.setSpamCntrlEmail("spam.ctrl@localhost");
			data.setChaRspHndlrEmail("challenge@localhost");
		}
		else { // release data
			data.setVirusCntrlEmail("sitemaster@localhost");
			data.setSecurityEmail("security@localhost");
			data.setCustcareEmail("custcare@localhost");
			data.setRmaDeptEmail("rma.dept@localhost");
			data.setSpamCntrlEmail("spam.ctrl@localhost");
			data.setChaRspHndlrEmail("challenge@localhost");
		}
		data.setEmbedEmailId(true); // Embed EmailId 
		data.setReturnPathLeft("support"); // return-path left
		data.setUseTestAddr(true); // use testing address
		data.setTestFromAddr("testfrom@localhost");
		data.setTestToAddr("testto@localhost");
		data.setVerpEnabled(true); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub-domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		Calendar cal = Calendar.getInstance();
		String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.getDb2Timestamp(cal.getTime()));
		data.setSystemId(systemId);
		data.setSystemKey(ProductUtil.getProductKeyFromFile());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadJBatchData() throws SQLException {
		ClientData data = new ClientData();
		data.setClientId("JBatchCorp");
		data.setClientName("JBatch Corp. Site");
		data.setDomainName("jbatch.com"); // domain name
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId( "0000000000");
		data.setWebSiteUrl("http://www.jbatch.com");
		data.setSaveRawMsg(true); // save raw stream
		data.setVirusCntrlEmail("sitemaster@jbatch.com");
		data.setSecurityEmail("security@jbatch.com");
		data.setCustcareEmail("custcare@jbatch.com");
		data.setRmaDeptEmail("rma.dept@jbatch.com");
		data.setSpamCntrlEmail("spam.control@jbatch.com");
		data.setChaRspHndlrEmail("challenge@jbatch.com");
		data.setEmbedEmailId(true);
		data.setReturnPathLeft("support"); // return-path left
		data.setUseTestAddr(false); // use testing address
		data.setTestFromAddr("testfrom@jbatch.com");
		data.setTestToAddr("testto@jbatch.com");
		data.setVerpEnabled(false); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		data.setSystemId("");
		data.setSystemKey(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
}

