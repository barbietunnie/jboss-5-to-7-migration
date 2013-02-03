package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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

public class TemplateDataLoader {
	static final Logger logger = Logger.getLogger(TemplateDataLoader.class);
	private static ClientVariableService cvService;
	private static GlobalVariableService gvService;
	private static ClientDataService clientService;

	public static void main(String[] args) {
		TemplateDataLoader loader = new TemplateDataLoader();
		cvService = (ClientVariableService) SpringUtil.getAppContext().getBean("clientVariableService");
		gvService = (GlobalVariableService) SpringUtil.getAppContext().getBean("globalVariableService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loader.loadClientVariables();
			loader.loadGlobalVariables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			txmgr.commit(status);
		}
	}

	void loadClientVariables() throws SQLException {
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
		in.setAllowOverride(Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());

		cvService.insert(in);

		in = new ClientVariable();
		in.setClientData(cd);
		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());

		cvService.insert(in);

		in = new ClientVariable();
		in.setClientData(cd);
		in.setVariableName("CurrentTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());
		cvService.insert(in);
		logger.info("EntityManager persisted the record.");
	}
	
	void loadGlobalVariables() throws SQLException {
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		GlobalVariable in = new GlobalVariable();
		
		in.setVariableName("CurrentDateTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd HH:mm:ss");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());
		gvService.insert(in);

		in = new GlobalVariable();
		in.setVariableName("CurrentDate");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride( Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());
		gvService.insert(in);

		in = new GlobalVariable();
		in.setVariableName("CurrentTime");
		in.setStartTime(updtTime);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());
		gvService.insert(in);

		in = new GlobalVariable();
		in.setVariableName(XHeaderName.XHEADER_CLIENT_ID);
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(Constants.Code.YES_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());
		gvService.insert(in);
		
		in = new GlobalVariable();
		in.setVariableName("PoweredBySignature");
		in.setStartTime(updtTime);
		in.setVariableValue(Constants.POWERED_BY_HTML_TAG);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.TEXT.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(Constants.Code.NO_CODE.getValue());
		in.setRequired(Constants.Code.NO_CODE.getValue());
		gvService.insert(in);
		
		System.out.println("loadGlobalVariables() completed.\n"+in);
	}
}

