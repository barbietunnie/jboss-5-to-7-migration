package com.es.data.loader;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.address.EmailTemplateDao;
import com.es.dao.address.EmailVariableDao;
import com.es.dao.address.MailingListDao;
import com.es.dao.address.SubscriptionDao;
import com.es.dao.sender.SenderUtil;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.MailingListDeliveryType;
import com.es.data.constant.StatusId;
import com.es.data.preload.EmailTemplateEnum;
import com.es.data.preload.EmailVariableEnum;
import com.es.data.preload.MailingListEnum;
import com.es.data.preload.SubscriberEnum;
import com.es.data.preload.SubscriberEnum.Subscriber;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.address.EmailTemplateVo;
import com.es.vo.address.EmailVariableVo;
import com.es.vo.address.MailingListVo;
import com.es.vo.address.SchedulesBlob;
import com.es.vo.address.SubscriptionVo;
import com.es.vo.comm.SenderVo;

public class EmailAddrTable extends AbstractTableBase {

	public void createTables() throws DataAccessException {
		createEmailTable();
		createMailingListTable();
		createSubscriptionTable();
		createEmailVariableTable();
		createEmailTemplateTable();
		createFindByAddressSP();
		createUnsubCommentsTable();
	}

	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE Unsub_Comment");
			System.out.println("Dropped Unsub_Comment Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE Email_Template");
			System.out.println("Dropped Email_Template Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE Email_Variable");
			System.out.println("Dropped Email_Variable Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE SUBSCRIPTION");
			System.out.println("Dropped SUBSCRIPTION Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MAILING_LIST");
			System.out.println("Dropped MAILING_LIST Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE EMAIL_ADDRESS");
			System.out.println("Dropped EMAIL_ADDRESS Table...");
		}
		catch (DataAccessException e) {
		}
	}
	
	void createEmailTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE EMAIL_ADDRESS ( "
					+ "EmailAddrId bigint AUTO_INCREMENT NOT NULL PRIMARY KEY, "
					+ "EmailAddr varchar(255) NOT NULL, "
					+ "OrigEmailAddr varchar(255) NOT NULL, "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " // A - active, S - suspended, I - Inactive
					+ "StatusChangeTime datetime, "
					+ "StatusChangeUserId varchar(10), "
					+ "BounceCount decimal(3) NOT NULL DEFAULT 0, "
					+ "LastBounceTime datetime, "
					+ "LastSentTime datetime, "
					+ "LastRcptTime datetime, "
					+ "AcceptHtml char(1) not null default '" + CodeType.YES_CODE.getValue() + "', "
					+ "UpdtTime datetime NOT NULL, "
					+ "UpdtUserId char(10) NOT NULL, "
					+ "UNIQUE INDEX (EmailAddr) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created EMAIL_ADDRESS Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMailingListTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MAILING_LIST ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "ListId varchar(8) NOT NULL, "
					+ "DisplayName varchar(50), "
					+ "AcctUserName varchar(100) NOT NULL, " 
						// left part of email address, right part from Sender_Data table's DomainName
					+ "Description varchar(500), "
					+ "SenderId varchar(16) NOT NULL, "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " 
						// A - active, I - Inactive
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
					+ "IsSendText char(1), "
					+ "CreateTime datetime NOT NULL, "
					+ "ListMasterEmailAddr varchar(255), "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (SenderId) REFERENCES Sender_Data (SenderId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (AcctUserName), "
					+ "UNIQUE INDEX (ListId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created MAILING_LIST Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSubscriptionTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE SUBSCRIPTION ( "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8) NOT NULL, "
					+ "Subscribed char(1) NOT NULL, " 
						// Y - subscribed, N - not subscribed, P - Pending Confirmation
					+ "CreateTime datetime NOT NULL, "
					+ "SentCount int NOT NULL DEFAULT 0, "
					+ "LastSentTime datetime, "
					+ "OpenCount int NOT NULL DEFAULT 0, "
					+ "LastOpenTime datetime, "
					+ "ClickCount int NOT NULL DEFAULT 0, "
					+ "LastClickTime datetime, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (ListId) REFERENCES MAILING_LIST (ListId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "UNIQUE INDEX (EmailAddrId,ListId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created SUBSCRIPTION Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createEmailVariableTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE Email_Variable ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "VariableName varchar(26) NOT NULL, "
					+ "VariableType char(1) NOT NULL, " 
						// S - system, C - customer (individual)
					+ "TableName varchar(50), " // document only
					+ "ColumnName varchar(50), " // document only
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " 
						// A - active, I - Inactive
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
					+ "DefaultValue varchar(255), "
					+ "VariableQuery varchar(255), " // 1) provides TO emailAddId as query criteria
													// 2) returns a single field called "ResultStr"
					+ "VariableProc varchar(100), " // when Query is null or returns no result
					+ "PRIMARY KEY (RowId), "
					+ "UNIQUE INDEX (VariableName) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created Email_Variable Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createEmailTemplateTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE Email_Template ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "TemplateId varchar(26) NOT NULL, "
					+ "ListId varchar(8) NOT NULL, "
					+ "Subject varchar(255), "
					+ "BodyText mediumtext, "
					+ "IsHtml char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', " // Y or N
					+ "ListType varchar(12) NOT NULL, " // Traditional/Personalized
					+ "DeliveryOption varchar(4) NOT NULL DEFAULT '" + MailingListDeliveryType.ALL_ON_LIST.getValue() + "', " // when ListType is Personalized
						// ALL - all on list, CUST - only email addresses with customer record
					+ "SelectCriteria varchar(100), " 
						// additional selection criteria - to be implemented
					+ "EmbedEmailId char(1) NOT NULL DEFAULT '', " // Y, N, or <Blank> - use system default
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', "
					+ "Schedules blob, " // store a java object
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (ListId) REFERENCES MAILING_LIST (ListId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "UNIQUE INDEX (TemplateId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created Email_Template Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createUnsubCommentsTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE Unsub_Comment ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8), "
					+ "Comments varchar(500) NOT NULL, "
					+ "AddTime datetime NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (EmailAddrId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created Unsub_Comment Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}


/* MySQL Stored Procedure:
DELIMITER $$

DROP PROCEDURE IF EXISTS `message`.`FindByAddress` $$
CREATE DEFINER=`email`@`%` PROCEDURE `FindByAddress`(
  IN iEmailAddr VARCHAR(255),
  OUT oEmailAddrId LONG,
  OUT oEmailAddr VARCHAR(255),
  OUT oOrigEmailAddr VARCHAR(255),
  OUT oStatusId CHAR(1),
  OUT oStatusChangeTime DATETIME,
  OUT oStatusChangeUserId VARCHAR(10),
  OUT oBounceCount DECIMAL(3,0),
  OUT oLastBounceTime DATETIME,
  OUT oLastSentTime DATETIME,
  OUT oLastRcptTime DATETIME,
  OUT oAcceptHtml CHAR(1),
  OUT oUpdtTime DATETIME,
  OUT oUpdtUserId VARCHAR(10)
 )
 MODIFIES SQL DATA
BEGIN
  declare pEmailAddrId long default 0;
  declare currTime DATETIME;
  declare pEmailAddr varchar(255) default null;
  select EmailAddrId, EmailAddr, OrigEmailAddr, StatusId, StatusChangeTime, StatusChangeUserId,
          BounceCount, LastBounceTime, LastSentTime, LastRcptTime, AcceptHtml,
          UpdtTime, UpdtUserId
    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId
    from EmailAddr where EmailAddr=TRIM(iEmailAddr);
  select now() into currTime;
  if oEmailAddr is NULL then
    insert into EmailAddr (EmailAddr, OrigEmailAddr, StatusChangeTime,
                          StatusChangeUserId, UpdtTime, UpdtUserId)
      values (iEmailAddr, iEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');
    select last_insert_id() into oEmailAddrId;
    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;
    select 'A' into oStatusId;
    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;
    select 0 into oBounceCount;
    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;
    select 'Y' into oAcceptHtml;
    select currTime, 'StoredProc' into oUpdtTime, oUpdtUserId;
  end if;
  select oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId;
END $$

DELIMITER ;
*/
	
	void createFindByAddressSP() throws DataAccessException {
		try {
			getJdbcTemplate().execute(
				"DROP PROCEDURE IF EXISTS `message`.`FindByAddress`"
			);
			getJdbcTemplate().execute(
				"CREATE PROCEDURE `FindByAddress`(" + LF +
				"  IN iEmailAddr VARCHAR(255)," + LF +
				"  IN iOrigEmailAddr VARCHAR(255)," + LF +
				"  OUT oEmailAddrId LONG," + LF +
				"  OUT oEmailAddr VARCHAR(255)," + LF +
				"  OUT oOrigEmailAddr VARCHAR(255)," + LF +
				"  OUT oStatusId CHAR(1)," + LF +
				"  OUT oStatusChangeTime DATETIME," + LF +
				"  OUT oStatusChangeUserId VARCHAR(10)," + LF +
				"  OUT oBounceCount DECIMAL(3,0)," + LF +
				"  OUT oLastBounceTime DATETIME," + LF +
				"  OUT oLastSentTime DATETIME," + LF +
				"  OUT oLastRcptTime DATETIME," + LF +
				"  OUT oAcceptHtml CHAR(1)," + LF +
				"  OUT oUpdtTime DATETIME," + LF +
				"  OUT oUpdtUserId VARCHAR(10)" + LF +
				" )" + LF +
				" MODIFIES SQL DATA" + LF +
				"BEGIN" + LF +
				"  declare pEmailAddrId long default 0;" + LF +
				"  declare currTime DATETIME;" + LF +
				"  declare pEmailAddr varchar(255) default null;" + LF +
				"  select EmailAddrId, EmailAddr, OrigEmailAddr, StatusId, StatusChangeTime, StatusChangeUserId," + LF +
				"          BounceCount, LastBounceTime, LastSentTime, LastRcptTime, AcceptHtml," + LF +
				"          UpdtTime, UpdtUserId" + LF +
				"    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId," + LF +
				"          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml," + LF +
				"          oUpdtTime, oUpdtUserId" + LF +
				"    from Email_Address where EmailAddr=TRIM(iEmailAddr);" + LF +
				"  select now() into currTime;" + LF +
				"  if oEmailAddr is NULL then" + LF +
				"    insert into Email_Address (EmailAddr, OrigEmailAddr, StatusChangeTime," + LF +
				"                          StatusChangeUserId, UpdtTime, UpdtUserId)" + LF +
				"      values (iEmailAddr, iOrigEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');" + LF +
				"    select last_insert_id() into oEmailAddrId;" + LF +
				"    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;" + LF +
				"    select 'A' into oStatusId;" + LF +
				"    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;" + LF +
				"    select 0 into oBounceCount;" + LF +
				"    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;" + LF +
				"    select 'Y' into oAcceptHtml;" + LF +
				"    select currTime, 'StoredProc' into oUpdtTime, oUpdtUserId;" + LF +
				"  end if;" + LF +
				"END "
			);
			System.out.println("Created FindByAddress Stored Procedure...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadTestData() throws DataAccessException {
		loadEmailAddrs();
		loadMailingLists();
		loadProdMailingLists();
		loadEmailVariables();
		loadEmailTemplates();
		loadProdEmailTemplates();
		loadSubscribers();
	}

	public void loadReleaseData() throws DataAccessException {
		loadEmailAddrs();
		loadMailingLists();
		loadEmailVariables();
		loadEmailTemplates();
		loadSubscribers();
	}
	
	private void loadEmailAddrs() throws DataAccessException {
		EmailAddressDao service = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		int count = 0;
		for (SubscriberEnum.Subscriber sub : SubscriberEnum.Subscriber.values()) {
			EmailAddressVo data = new EmailAddressVo();
			data.setOrigEmailAddr(sub.getAddress());
			data.setEmailAddr(data.getOrigEmailAddr());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
			data.setStatusChangeUserId("testuser" + (++count));
			data.setBounceCount(0);
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}

	}

	private void loadMailingLists() throws DataAccessException {
		SenderVo sender = SenderUtil.getDefaultSenderVo();
		String domain = sender.getDomainName();
		
		MailingListDao mlistService = SpringUtil.getAppContext().getBean(MailingListDao.class);

		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		for (MailingListEnum mlist : MailingListEnum.values()) {
			if (mlist.isProd()) continue;
			MailingListVo in = new MailingListVo();
			in.setSenderId(sender.getSenderId());
			in.setListId(mlist.name());
			in.setDisplayName(mlist.getDisplayName());
			in.setAcctUserName(mlist.getAcctName());
			in.setDescription(mlist.getDescription());
			in.setStatusId(mlist.getStatusId().getValue());
			in.setIsBuiltIn(mlist.isBuiltin()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			if (mlist.equals(MailingListEnum.SMPLLST1) || mlist.equals(MailingListEnum.SMPLLST2)) {
				in.setIsSendText(CodeType.YES_CODE.getValue());
			}
			in.setCreateTime(createTime);
			in.setUpdtUserId(Constants.DEFAULT_USER_ID);
			in.setListMasterEmailAddr("sitemaster@"+domain);
			mlistService.insert(in);
		}
		
		System.out.println("Mailing list records inserted.");
	}
	
	private void loadProdMailingLists() throws DataAccessException {
		SenderVo sender = SenderUtil.getDefaultSenderVo();
		String domain = sender.getDomainName();

		MailingListDao mlistService = SpringUtil.getAppContext().getBean(MailingListDao.class);
		
		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		
		for (MailingListEnum mlist : MailingListEnum.values()) {
			if (!mlist.isProd()) continue;
			MailingListVo in = new MailingListVo();
			in.setSenderId(sender.getSenderId());
			in.setListId(mlist.name());
			in.setDisplayName(mlist.getDisplayName());
			in.setAcctUserName(mlist.getAcctName());
			in.setDescription(mlist.getDescription());
			in.setStatusId(mlist.getStatusId().getValue());
			in.setIsBuiltIn(mlist.isBuiltin()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			in.setCreateTime(createTime);
			in.setUpdtUserId(Constants.DEFAULT_USER_ID);
			// TODO get domain name from properties file
			in.setListMasterEmailAddr("sitemaster@"+domain);
			mlistService.insert(in);
		}

		System.out.println("Mailing List records for Prod inserted.");
	}
	
	private void loadSubscribers() {
		java.sql.Timestamp createTime = new java.sql.Timestamp(System.currentTimeMillis());
		
		MailingListDao mlistService = SpringUtil.getAppContext().getBean(MailingListDao.class);
		EmailAddressDao emailService = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		SubscriptionDao subService = SpringUtil.getAppContext().getBean(SubscriptionDao.class);
		
		for (SubscriberEnum sublst : SubscriberEnum.values()) {
			MailingListVo mlist = mlistService.getByListId(sublst.getMailingList().name());
			for (Subscriber subscriber : sublst.getSubscribers()) {
				SubscriptionVo sub = new SubscriptionVo();
				sub.setListId(mlist.getListId());
				sub.setSubscribed(sublst.isSubscribed()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
				sub.setStatusId(StatusId.ACTIVE.getValue());
				sub.setCreateTime(createTime);
				EmailAddressVo email = emailService.findSertAddress(subscriber.getAddress());
				sub.setEmailAddrId(email.getEmailAddrId());
				if (Subscriber.Subscriber2.equals(subscriber)) {
					sub.setClickCount(1);
					sub.setOpenCount(2);
					sub.setSentCount(3);
				}
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				subService.insert(sub);
			}
		}
		System.out.println("Subscription records for Prod inserted.");
	}

	private void loadEmailVariables() {
		EmailVariableDao service = SpringUtil.getAppContext().getBean(EmailVariableDao.class);
		for (EmailVariableEnum variable : EmailVariableEnum.values()) {
			EmailVariableVo data = new EmailVariableVo();
			data.setVariableName(variable.name());
			data.setVariableType(variable.getVariableType().getValue());
			data.setTableName(variable.getTableName());
			data.setColumnName(variable.getColumnName());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setIsBuiltIn(variable.isBuiltin()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			data.setDefaultValue(variable.getDefaultValue());
			data.setVariableQuery(variable.getVariableQuery());
			data.setVariableProc(variable.getVariableProcName());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		System.out.println("Email variable records inserted.");
	}
	
	private void loadEmailTemplates() {
		SenderVo sender = SenderUtil.getDefaultSenderVo();
		MailingListDao mlistService = SpringUtil.getAppContext().getBean(MailingListDao.class);
		EmailTemplateDao service = getEmailTemplateDao();
		for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
			if (tmp.getMailingList().isProd()) continue;
			MailingListVo mlist = mlistService.getByListId(tmp.getMailingList().name());
			EmailTemplateVo data = new EmailTemplateVo();
			data.setSenderId(sender.getSenderId());
			data.setListId(mlist.getListId());
			data.setTemplateId(tmp.name());
			data.setSubject(tmp.getSubject());
			data.setBodyText(tmp.getBodyText());
			data.setIsHtml(tmp.isHtml());
			data.setListType(tmp.getListType().getValue());
			data.setDeliveryOption(tmp.getDeliveryType().getValue());
			data.setIsBuiltIn(tmp.isBuiltin()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			String embedEmailId = " "; // use system default when null
			if (tmp.getIsEmbedEmailId()!=null) {
				embedEmailId = tmp.getIsEmbedEmailId()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue();
			}
			data.setEmbedEmailId(embedEmailId);
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			SchedulesBlob blob1 = new SchedulesBlob();
			data.setSchedulesBlob(blob1);
			service.insert(data);
		}

		System.out.println("Email Template records inserted.");
	}
	
	void loadProdEmailTemplates() {
		SenderVo sender = SenderUtil.getDefaultSenderVo();
		MailingListDao mlistService = SpringUtil.getAppContext().getBean(MailingListDao.class);
		EmailTemplateDao service = getEmailTemplateDao();
		for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
			if (!tmp.getMailingList().isProd()) continue;
			MailingListVo mlist = mlistService.getByListId(tmp.getMailingList().name());
			EmailTemplateVo data = new EmailTemplateVo();
			data.setSenderId(sender.getSenderId());
			data.setListId(mlist.getListId());
			data.setTemplateId(tmp.name());
			data.setSubject(tmp.getSubject());
			data.setBodyText(tmp.getBodyText());
			data.setIsHtml(tmp.isHtml());
			data.setListType(tmp.getListType().getValue());
			data.setDeliveryOption(tmp.getDeliveryType().getValue());
			data.setIsBuiltIn(tmp.isBuiltin()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue());
			String embedEmailId = " "; // use system default when null
			if (tmp.getIsEmbedEmailId()!=null) {
				embedEmailId = tmp.getIsEmbedEmailId()?CodeType.YES_CODE.getValue():CodeType.NO_CODE.getValue();
			}
			data.setEmbedEmailId(embedEmailId);
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			SchedulesBlob blob1 = new SchedulesBlob();
			data.setSchedulesBlob(blob1);
			service.insert(data);
		}

		System.out.println("Email Template records for Prod inserted.");
	}


	void selectEmailTemplate() throws DataAccessException {
		try	{
			String sql =
				"select * from Email_Template where TemplateId = 'test template'";
			List<EmailTemplateVo> rs = getJdbcTemplate().query(sql, 
					new BeanPropertyRowMapper<EmailTemplateVo>(EmailTemplateVo.class));
			for (EmailTemplateVo vo : rs) {
				String id = vo.getListId();
				SchedulesBlob blob = vo.getSchedulesBlob();
					System.out.println("ListId: " + id + ", blob: " + blob);
			}
		} 
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}
	
	/**
	 * to trigger the insert of template id's to MsgDataType table. 
	 */
	public void updateTemplates() {
		int rowsUpdated = 0;
		List<EmailTemplateVo> list = getEmailTemplateDao().getAll();
		for (EmailTemplateVo tmpltVo : list) {
			tmpltVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
			rowsUpdated += getEmailTemplateDao().update(tmpltVo);
		}
		System.out.println("Updated EmailTemplate records: " + rowsUpdated);
	}

	private EmailTemplateDao emailTemplateDao = null;
	private EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = SpringUtil.getAppContext().getBean(EmailTemplateDao.class);
		}
		return emailTemplateDao;
	}
	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			EmailAddrTable ct = new EmailAddrTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}