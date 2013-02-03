package jpa.util;

import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.ClientData;
import jpa.service.ClientDataService;

public class ClientDataLoader {
	static final Logger logger = Logger.getLogger(ClientDataLoader.class);
	private static ClientDataService service;

	public static void main(String[] args) {
		ClientDataLoader loader = new ClientDataLoader();
		service = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("idtokens_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loader.loadClientData(true);
			loader.loadJBatchData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			txmgr.commit(status);
		}
	}

	void loadClientData(boolean loadTestData) {
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
		data.setIsSaveRawMsg(Constants.Code.YES_CODE.getValue()); // save raw stream
		if (loadTestData) {
			data.setContactEmail("sitemaster@emailsphere.com");
			data.setSecurityEmail("security@localhost");
			data.setCustcareEmail("custcare@localhost");
			data.setRmaDeptEmail("rma.dept@localhost");
			data.setSpamCntrlEmail("spam.ctrl@localhost");
			data.setChaRspHndlrEmail("challenge@localhost");
		}
		else { // release data
			data.setContactEmail("sitemaster@localhost");
			data.setSecurityEmail("security@localhost");
			data.setCustcareEmail("custcare@localhost");
			data.setRmaDeptEmail("rma.dept@localhost");
			data.setSpamCntrlEmail("spam.ctrl@localhost");
			data.setChaRspHndlrEmail("challenge@localhost");
		}
		data.setIsEmbedEmailId(Constants.Code.YES.getValue()); // Embed EmailId 
		data.setReturnPathLeft("support"); // return-path left
		data.setIsUseTestAddr(Constants.Code.YES.getValue()); // use testing address
		data.setTestFromAddr("testfrom@localhost");
		data.setTestToAddr("testto@localhost");
		data.setIsVerpEnabled(Constants.Code.YES.getValue()); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub-domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		Calendar cal = Calendar.getInstance();
		String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp(cal.getTime()));
		data.setSystemId(systemId);
		data.setSystemKey(ProductUtil.getProductKeyFromFile());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
	void loadJBatchData() throws SQLException {
		ClientData data = new ClientData();
		data.setClientId("JBatchCorp");
		data.setClientName("JBatch Corp. Site");
		data.setDomainName("jbatch.com"); // domain name
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId( "0000000000");
		data.setWebSiteUrl("http://www.jbatch.com");
		data.setIsSaveRawMsg(Constants.Code.YES_CODE.getValue()); // save raw stream
		data.setContactEmail("sitemaster@jbatch.com");
		data.setSecurityEmail("security@jbatch.com");
		data.setCustcareEmail("custcare@jbatch.com");
		data.setRmaDeptEmail("rma.dept@jbatch.com");
		data.setSpamCntrlEmail("spam.control@jbatch.com");
		data.setChaRspHndlrEmail("challenge@jbatch.com");
		data.setIsEmbedEmailId(Constants.Code.YES.getValue());
		data.setReturnPathLeft("support"); // return-path left
		data.setIsUseTestAddr(Constants.Code.NO.getValue()); // use testing address
		data.setTestFromAddr("testfrom@jbatch.com");
		data.setTestToAddr("testto@jbatch.com");
		data.setIsVerpEnabled(Constants.Code.NO.getValue()); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		data.setSystemId("");
		data.setSystemKey(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
	}
}

