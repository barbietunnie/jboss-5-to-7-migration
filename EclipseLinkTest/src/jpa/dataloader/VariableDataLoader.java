package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.model.ClientData;
import jpa.model.ClientVariable;
import jpa.model.GlobalVariable;
import jpa.service.ClientDataService;
import jpa.service.ClientVariableService;
import jpa.service.GlobalVariableService;
import jpa.util.SpringUtil;

public class VariableDataLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(VariableDataLoader.class);
	private ClientVariableService cvService;
	private GlobalVariableService gvService;
	private ClientDataService clientService;

	public static void main(String[] args) {
		VariableDataLoader loader = new VariableDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		cvService = (ClientVariableService) SpringUtil.getAppContext().getBean("clientVariableService");
		gvService = (GlobalVariableService) SpringUtil.getAppContext().getBean("globalVariableService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadClientVariables();
			loadGlobalVariables();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	private void loadClientVariables() throws SQLException {
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		ClientVariable in = new ClientVariable();

		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		in.setClientData(cd);
		in.setVariableName("CurrentDateTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);

		cvService.insert(in);

		in = new ClientVariable();
		in.setClientData(cd);
		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);

		cvService.insert(in);

		in = new ClientVariable();
		in.setClientData(cd);
		in.setVariableName("CurrentTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		cvService.insert(in);
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadGlobalVariables() throws SQLException {
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		GlobalVariable in = new GlobalVariable();
		
		in.setVariableName("CurrentDateTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd HH:mm:ss");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);

		in = new GlobalVariable();
		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride( CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);

		in = new GlobalVariable();
		in.setVariableName("CurrentTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);

		in = new GlobalVariable();
		in.setVariableName(XHeaderName.XHEADER_CLIENT_ID);
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);
		
		in = new GlobalVariable();
		in.setVariableName("PoweredBySignature");
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.POWERED_BY_HTML_TAG);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.NO_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);
		
		System.out.println("loadGlobalVariables() completed.\n"+in);
	}
}

