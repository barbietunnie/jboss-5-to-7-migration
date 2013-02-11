package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.model.ClientData;
import jpa.model.ClientVariable;
import jpa.model.ClientVariablePK;
import jpa.model.GlobalVariable;
import jpa.model.GlobalVariablePK;
import jpa.service.ClientDataService;
import jpa.service.ClientVariableService;
import jpa.service.GlobalVariableService;
import jpa.util.SpringUtil;

public class VariableDataLoader extends AbstractDataLoader {
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
		startTransaction();
		try {
			loadClientVariables();
			loadGlobalVariables();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadClientVariables() throws SQLException {
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		ClientVariable in = new ClientVariable();

		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		ClientVariablePK pk1;

		pk1 = new ClientVariablePK(cd, "CurrentDateTime", updtTime);
		in.setClientVariablePK(pk1);
		in.setVariableValue(null);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);

		cvService.insert(in);

		in = new ClientVariable();
		pk1 = new ClientVariablePK(cd, "CurrentDate", updtTime);
		in.setClientVariablePK(pk1);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);

		cvService.insert(in);

		in = new ClientVariable();
		pk1 = new ClientVariablePK(cd, "CurrentTime", updtTime);
		in.setClientVariablePK(pk1);
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
		GlobalVariablePK pk1;

		pk1 = new GlobalVariablePK("CurrentDateTime", updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd HH:mm:ss");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);

		in = new GlobalVariable();
		pk1 = new GlobalVariablePK("CurrentDate", updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(null);
		in.setVariableFormat("yyyy-MM-dd");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride( CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);

		in = new GlobalVariable();
		pk1 = new GlobalVariablePK("CurrentTime", updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(null);
		in.setVariableFormat("hh:mm:ss a");
		in.setVariableType(VariableType.DATETIME.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);

		in = new GlobalVariable();
		pk1 = new GlobalVariablePK(XHeaderName.CLIENT_ID.getValue(), updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);
		
		in = new GlobalVariable();
		pk1 = new GlobalVariablePK("PoweredBySignature", updtTime);
		in.setGlobalVariablePK(pk1);
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

