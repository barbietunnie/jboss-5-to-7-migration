package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.data.preload.ClientVariableEnum;
import jpa.data.preload.GlobalVariableEnum;
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

		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		for (ClientVariableEnum variable : ClientVariableEnum.values()) {
			ClientVariable in = new ClientVariable();
			ClientVariablePK pk1 = new ClientVariablePK(cd, variable.name(), updtTime);
			in.setClientVariablePK(pk1);
			in.setVariableValue(variable.getDefaultValue());
			in.setVariableFormat(variable.getVariableFormat());
			in.setVariableType(variable.getVariableType().getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(variable.getAllowOverride().getValue());
			in.setRequired(false);
			cvService.insert(in);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadGlobalVariables() throws SQLException {
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		for (GlobalVariableEnum variable : GlobalVariableEnum.values()) {
			GlobalVariable in = new GlobalVariable();
			GlobalVariablePK pk1 = new GlobalVariablePK(variable.name(), updtTime);
			in.setGlobalVariablePK(pk1);
			in.setVariableValue(variable.getDefaultValue());
			in.setVariableFormat(variable.getVariableFormat());
			in.setVariableType(variable.getVariableType().getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(variable.getAllowOverride().getValue());
			in.setRequired(false);
			gvService.insert(in);
		}

		GlobalVariable in = new GlobalVariable();
		GlobalVariablePK pk1 = new GlobalVariablePK(XHeaderName.CLIENT_ID.getValue(), updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(Constants.DEFAULT_CLIENTID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);
		
		System.out.println("loadGlobalVariables() completed.\n"+in);
	}
}

