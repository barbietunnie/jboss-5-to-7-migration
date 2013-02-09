package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.EmailIdToken;
import jpa.model.ClientData;
import jpa.model.IdTokens;
import jpa.service.ClientDataService;
import jpa.service.IdTokensService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class IdTokensDataLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(IdTokensDataLoader.class);
	private IdTokensService itService;
	private ClientDataService clientService;

	public static void main(String[] args) {
		IdTokensDataLoader loader = new IdTokensDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		itService = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadIdTokens();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	void loadIdTokens() throws SQLException {
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		IdTokens in = new IdTokens();

		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		in.setClientData(cd);
		in.setDescription("Default SenderId");
		in.setBodyBeginToken(EmailIdToken.BODY_BEGIN);
		in.setBodyEndToken(EmailIdToken.BODY_END);
		in.setXheaderName(EmailIdToken.XHEADER_NAME);
		in.setXhdrBeginToken(EmailIdToken.XHDR_BEGIN);
		in.setXhdrEndToken(EmailIdToken.XHDR_END);
		in.setMaxLength(EmailIdToken.MAXIMUM_LENGTH);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId("SysAdmin");
		itService.insert(in);
		logger.info("EntityManager persisted the record.");
	}
	
}

