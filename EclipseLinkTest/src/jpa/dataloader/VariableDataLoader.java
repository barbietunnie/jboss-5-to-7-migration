package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.data.preload.SenderVariableEnum;
import jpa.data.preload.GlobalVariableEnum;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SenderVariablePK;
import jpa.model.GlobalVariable;
import jpa.model.GlobalVariablePK;
import jpa.service.SenderDataService;
import jpa.service.SenderVariableService;
import jpa.service.GlobalVariableService;
import jpa.util.SpringUtil;

public class VariableDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(VariableDataLoader.class);
	private SenderVariableService cvService;
	private GlobalVariableService gvService;
	private SenderDataService senderService;

	public static void main(String[] args) {
		VariableDataLoader loader = new VariableDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		cvService = (SenderVariableService) SpringUtil.getAppContext().getBean("senderVariableService");
		gvService = (GlobalVariableService) SpringUtil.getAppContext().getBean("globalVariableService");
		senderService = (SenderDataService) SpringUtil.getAppContext().getBean("senderDataService");
		startTransaction();
		try {
			loadSenderVariables();
			loadGlobalVariables();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadSenderVariables() throws SQLException {
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);

		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		for (SenderVariableEnum variable : SenderVariableEnum.values()) {
			SenderVariable in = new SenderVariable();
			SenderVariablePK pk1 = new SenderVariablePK(cd, variable.name(), updtTime);
			in.setSenderVariablePK(pk1);
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
		GlobalVariablePK pk1 = new GlobalVariablePK(XHeaderName.SENDER_ID.getValue(), updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(Constants.DEFAULT_SENDER_ID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);
		
		System.out.println("loadGlobalVariables() completed.\n"+in);
	}
}

