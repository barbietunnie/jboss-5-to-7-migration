package com.es.data.loader;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.sender.GlobalVariableDao;
import com.es.dao.sender.SenderDataDao;
import com.es.dao.template.MsgSourceDao;
import com.es.dao.template.TemplateDataDao;
import com.es.dao.template.TemplateVariableDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.data.constant.VariableName;
import com.es.data.constant.VariableType;
import com.es.data.constant.XHeaderName;
import com.es.data.preload.GlobalVariableEnum;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.SenderDataVo;
import com.es.vo.template.GlobalVariableVo;
import com.es.vo.template.MsgSourceVo;
import com.es.vo.template.TemplateDataVo;
import com.es.vo.template.TemplateVariableVo;

public class TemplateTables extends AbstractTableBase {

	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_SOURCE");
			System.out.println("Dropped MSG_SOURCE Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE TEMPLATE_VARIABLE");
			System.out.println("Dropped TEMPLATE_VARIABLE Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE SENDER_VARIABLE");
			System.out.println("Dropped SENDER_VARIABLE Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE GLOBAL_VARIABLE");
			System.out.println("Dropped GLOBAL_VARIABLE Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE TEMPLATE_DATA");
			System.out.println("Dropped TEMPLATE_DATA Table...");
		}
		catch (DataAccessException e) {
		}
	}

	public void createTables() throws DataAccessException {
		createBODYTEMPLATETable();
		createGLOBALVARIABLETable();
		createSENDERVARIABLETable();
		createTEMPLATEVARIABLETable();
		createMSGSOURCETable();
	}
	
	public void loadTestData() throws DataAccessException {
		loadGlobalVariables();
		loadTemplateData();
	}
	
	void createBODYTEMPLATETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE TEMPLATE_DATA ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "SenderId varchar(16), "
				+ "StartTime timestamp NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', "
				+ "ContentType varchar(100) NOT NULL, " // content mime type
				+ "BodyTemplate text, "
				+ "SubjTemplate varchar(255), "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (SenderId) REFERENCES Sender_Data (SenderId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (SenderId), "
				+ "UNIQUE INDEX (TemplateId,SenderId,StartTime)) ENGINE=InnoDB");
			System.out.println("Created BODY_TEMPLATE Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createGLOBALVARIABLETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE GLOBAL_VARIABLE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp NOT NULL, "
				+ "VariableFormat varchar(50), " 
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', "
				// A - Active, I - Inactive
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + CodeType.YES_CODE.getValue() + "', "
				// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
				+ "Required char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
				// required to be present in body template
				+ "VariableValue varchar(255), "
				+ "PRIMARY KEY (RowId), "
				+ "INDEX (VariableName), "
				+ "UNIQUE INDEX (VariableName,StartTime)) ENGINE=InnoDB");
			System.out.println("Created GLOBAL_VARIABLE Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSENDERVARIABLETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE SENDER_VARIABLE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "SenderId varchar(16) NOT NULL, "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp NOT NULL, "
				+ "VariableFormat varchar(50), "
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', "
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + CodeType.YES_CODE.getValue() + "', "
				// allow override value to be supplied at runtime
				+ "Required char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
				// required to present in body template
				+ "VariableValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (SenderId) REFERENCES Sender_Data (SenderId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (SenderId), "
				+ "INDEX (VariableName), "
				+ "UNIQUE INDEX (SenderId,VariableName,StartTime)) ENGINE=InnoDB");
			System.out.println("Created SENDER_VARIABLE Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createTEMPLATEVARIABLETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE TEMPLATE_VARIABLE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "SenderId varchar(16), "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp NOT NULL, "
				+ "VariableFormat varchar(50), "
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - X header, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', "
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + CodeType.YES_CODE.getValue() + "', "
				// allow override value to be supplied at runtime
				+ "Required char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
				// required to present in body template
				+ "VariableValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "INDEX (VariableName), "
				+ "FOREIGN KEY (SenderId) REFERENCES Sender_Data (SenderId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (SenderId), "
				+ "UNIQUE INDEX (TemplateId,SenderId,VariableName,StartTime)"
				+ ") ENGINE=InnoDB");
			System.out.println("Created TEMPLATE_VARIABLE Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGSOURCETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_SOURCE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "MsgSourceId varchar(16) NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', "
				+ "FromAddrId bigint NOT NULL, "
				+ "ReplyToAddrId bigint, "
				+ "TemplateDataId varchar(16) NOT NULL, "
				+ "TemplateVariableId varchar(16), "
				+ "ExcludingIdToken char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
				// Y - No email id will be embedded into message
				+ "CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.getValue() + "', "
				// Internet, WebMail, Internal Routing, ...
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + CodeType.YES_CODE.getValue() + "', "
				// allow override templates, addrs to be supplied at runtime
				+ "SaveMsgStream char(1) NOT NULL DEFAULT '" + CodeType.YES_CODE.getValue() + "', "
				// Y - save rendered smtp message stream to MSGSTREAM
				+ "ArchiveInd char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
				// Y - archive the rendered messages
				+ "PurgeAfter int, " // in month
				+ "UpdtTime datetime NOT NULL, "
				+ "UpdtUserId varchar(10) NOT NULL, "
				+ "PRIMARY KEY (RowId), "
				+ "UNIQUE INDEX (MsgSourceId), "
				+ "FOREIGN KEY (FromAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (FromAddrId), "
				+ "FOREIGN KEY (ReplyToAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE SET NULL ON UPDATE CASCADE, "
				+ "INDEX (ReplyToAddrId), "
				+ "FOREIGN KEY (TemplateVariableId) REFERENCES TEMPLATE_VARIABLE (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (TemplateVariableId), "
				+ "FOREIGN KEY (TemplateDataId) REFERENCES TEMPLATE_DATA (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (TemplateDataId) "
				+ ") ENGINE=InnoDB");
			System.out.println("Created MSG_SOURCE Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void loadTemplateData() {
		SenderDataDao senderService = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		TemplateDataDao templateService = SpringUtil.getAppContext().getBean(TemplateDataDao.class);
		TemplateVariableDao variableService = SpringUtil.getAppContext().getBean(TemplateVariableDao.class);
		EmailAddressDao addrService = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		MsgSourceDao sourceService = SpringUtil.getAppContext().getBean(MsgSourceDao.class);

		SenderDataVo sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		Timestamp tms =  new Timestamp(System.currentTimeMillis());

		TemplateDataVo data = new TemplateDataVo();
		data.setSenderId(sender.getSenderId());
		data.setTemplateId("WeekendDeals");
		data.setStartTime(tms);
		data.setContentType("text/plain");
		data.setBodyTemplate("Dear subscriber, here is a list of great deals on gardening tools provided to you by mydot.com.\n" +
				"Available by ${CurrentDate}. Sponsor (${SenderId}).");
		data.setSubjTemplate("Weekend Deals at MyBestDeals.com - ${CurrentDate}");
		templateService.insert(data);

		TemplateDataVo tmp2 = templateService.getByPrimaryKey(data.getTemplateId(), data.getSenderId(), data.getStartTime());
		String varTmpltId = "WeekendDeals";

		TemplateVariableVo var1 = new TemplateVariableVo();
		var1.setSenderId(sender.getSenderId());
		var1.setTemplateId(varTmpltId);
		var1.setVariableName("CurrentDateTime");
		var1.setStartTime(tms);
		
		var1.setVariableType(VariableType.DATETIME.getValue());
		var1.setAllowOverride(CodeType.YES_CODE.getValue());
		var1.setRequired(CodeType.NO_CODE.getValue());
		variableService.insert(var1);
		
		TemplateVariableVo var2 = new TemplateVariableVo();
		var2.setSenderId(sender.getSenderId());
		var2.setTemplateId(varTmpltId);
		var2.setVariableName("CurrentDate");
		var2.setStartTime(tms);
		var2.setVariableFormat("yyyy-MM-dd");
		var2.setVariableType(VariableType.DATETIME.getValue());
		var2.setAllowOverride(CodeType.YES_CODE.getValue());
		var2.setRequired(CodeType.NO_CODE.getValue());
		variableService.insert(var2);
		
		TemplateVariableVo var3 = new TemplateVariableVo();
		var3.setSenderId(sender.getSenderId());
		var3.setTemplateId(varTmpltId);
		var3.setVariableName(VariableName.SUBSCRIBER_ID.getValue());
		var3.setStartTime(tms);
		var3.setVariableType(VariableType.TEXT.getValue());
		var3.setAllowOverride(CodeType.YES_CODE.getValue());
		var3.setRequired(CodeType.YES_CODE.getValue());
		variableService.insert(var3);
		
		EmailAddressVo adr1 = addrService.findSertAddress("jsmith@test.com");
		MsgSourceVo src1 = new MsgSourceVo();
		src1.setMsgSourceId("WeekendDeals");
		src1.setDescription("Default Message Source");
		src1.setTemplateDataId(tmp2.getTemplateId());
		src1.setTemplateVariableId(varTmpltId);
		src1.setFromAddrId(adr1.getEmailAddrId());
		src1.setExcludingIdToken(CodeType.NO_CODE.getValue());
		src1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		src1.setAllowOverride(CodeType.YES_CODE.getValue());
		src1.setSaveMsgStream(CodeType.YES_CODE.getValue());
		src1.setArchiveInd(CodeType.NO_CODE.getValue());
		src1.setUpdtTime(tms);
		src1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		sourceService.insert(src1);

		// test template
		TemplateDataVo tmp3 = new TemplateDataVo();
		tmp3.setSenderId(sender.getSenderId());
		tmp3.setTemplateId("testTemplate");
		tmp3.setStartTime(tms);
		
		tmp3.setContentType("text/html");
		tmp3.setBodyTemplate("BeginTemplate\n"
				+ "Current DateTime: ${CurrentDate}<br>\n"
				+ "${name1}${name2} Some Text ${name3}More Text<br>\n"
				+ "${TABLE_SECTION_BEGIN}TableRowBegin &lt;${name2}&gt; TableRowEnd<br>\n" 
				+ "${TABLE_SECTION_END}text<br>\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 1-1 ${name1}<br>\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 2-1<br>\n${OPTIONAL_SECTION_END}<br>\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 2-2${dropped}<br>\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}Level 2-3${name2}<br>\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}Level 1-2<br>\n${OPTIONAL_SECTION_END}"
				+ "${name4}<br>\n"
				+ "EndTemplate<br>\n");
		tmp3.setSubjTemplate("Test Template");
		templateService.insert(tmp3);

		List<TemplateVariableVo> vars = variableService.getByTemplateId(varTmpltId);
		src1 = new MsgSourceVo();
		src1.setMsgSourceId("testMsgSource");
		src1.setDescription("Message Source");
		src1.setTemplateDataId(tmp3.getTemplateId());
		src1.setTemplateVariableId(vars.get(0).getTemplateId());
		src1.setFromAddrId(adr1.getEmailAddrId());
		src1.setExcludingIdToken(CodeType.NO_CODE.getValue());
		src1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		src1.setAllowOverride(CodeType.YES_CODE.getValue());
		src1.setSaveMsgStream(CodeType.YES_CODE.getValue());
		src1.setArchiveInd(CodeType.NO_CODE.getValue());
		src1.setUpdtTime(tms);
		src1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		sourceService.insert(src1);

		System.out.println("Template records inserted.");
	}

	private void loadGlobalVariables() {
		GlobalVariableDao gvService = SpringUtil.getAppContext().getBean(GlobalVariableDao.class);
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		for (GlobalVariableEnum variable : GlobalVariableEnum.values()) {
			GlobalVariableVo in = new GlobalVariableVo();
			in.setVariableName(variable.name());
			in.setUpdtTime(updtTime);
			in.setVariableValue(variable.getDefaultValue());
			in.setVariableFormat(variable.getVariableFormat());
			in.setVariableType(variable.getVariableType().getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(variable.getAllowOverride().getValue());
			in.setRequired(CodeType.NO_CODE.getValue());
			gvService.insert(in);
		}

		GlobalVariableVo in = new GlobalVariableVo();
		in.setVariableName(XHeaderName.SENDER_ID.getValue());
		in.setUpdtTime(updtTime);
		in.setVariableValue(Constants.DEFAULT_SENDER_ID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(CodeType.NO_CODE.getValue());
		gvService.insert(in);
		
		System.out.println("Global variable records inserted.");
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			TemplateTables ct = new TemplateTables();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}