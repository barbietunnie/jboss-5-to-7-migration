package com.es.data.loader;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.core.util.StringUtil;
import com.es.dao.action.RuleActionDao;
import com.es.dao.action.RuleActionDetailDao;
import com.es.dao.action.RuleDataTypeDao;
import com.es.dao.rule.RuleLogicDao;
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddrType;
import com.es.data.constant.StatusId;
import com.es.data.constant.TableColumnName;
import com.es.data.constant.VariableName;
import com.es.data.preload.EmailTemplateEnum;
import com.es.data.preload.MailingListEnum;
import com.es.data.preload.QueueNameEnum;
import com.es.data.preload.RuleActionDetailEnum;
import com.es.data.preload.RuleActionEnum;
import com.es.data.preload.RuleDataTypeEnum;
import com.es.data.preload.RuleNameEnum;
import com.es.vo.action.RuleActionDetailVo;
import com.es.vo.action.RuleActionVo;
import com.es.vo.action.RuleDataTypeVo;
import com.es.vo.comm.SenderDataVo;
import com.es.vo.rule.RuleLogicVo;

/**
 * Dependency: RuleBean - this program runs RuleTable first before create its
 * own tables.
 */
public class RuleActionTables extends AbstractTableBase {
	
	public void dropTables() {
		try	{
			getJdbcTemplate().execute("DROP TABLE RULE_ACTION");
			System.out.println("Dropped RULE_ACTION Table...");
		} catch (Exception e) {}
		try	{
			getJdbcTemplate().execute("DROP TABLE RULE_ACTION_DETAIL");
			System.out.println("Dropped RULE_ACTIONDETAIL Table...");
		} catch (Exception e) {}
		try	{
			getJdbcTemplate().execute("DROP TABLE RULE_DATA_TYPE");
			System.out.println("Dropped RULE_DATA_TYPE Table...");
		} catch (Exception e) {}
	}
	
	public void createTables() throws DataAccessException {
		createActionDataTypeTable();
		createActionDetailTable();
		createActionTable();
	}
	
	public void loadTestData() throws DataAccessException {
		loadRuleDataTypeAndValues();
		loadRuleActionDetails();
		loadRuleActions();
	}
	
	void createActionDataTypeTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RULE_DATA_TYPE ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"DataType varchar(16) NOT NULL, " +
			"DataTypeValue varchar(100) NOT NULL, " +
			"MiscProperties varchar(255), " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (DataType, DataTypeValue) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULE_DATA_TYPE Table...");
		} 
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionDetailTable() throws DataAccessException {
		try	{
			getJdbcTemplate().execute("CREATE TABLE RULE_ACTION_DETAIL ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ActionId varchar(16) NOT NULL, " +
			"Description varchar(100), " +
			"ProcessBeanId varchar(50) NOT NULL, " +
			"ProcessClassName varchar(100), " +
			"DataType varchar(16), " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"INDEX (DataType), " +
			"FOREIGN KEY (DataType) REFERENCES RULE_DATA_TYPE (DataType) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (ActionId), " +
			"PRIMARY KEY (RowId) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULE_ACTION_DETAIL Table...");
		} 
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RULE_ACTION ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"ActionSeq int NOT NULL, " +
			"StartTime datetime NOT NULL, " +
			"SenderId varchar(16), " + 
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " +
			"ActionId varchar(16) NOT NULL, " +
			"DataTypeValues text, " + // maximum size of 65,535, to accommodate template text
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RULE_LOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(RuleName), " +
			"FOREIGN KEY (ActionId) REFERENCES RULE_ACTION_DETAIL (ActionId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (RuleName, ActionSeq, StartTime, SenderId) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULE_ACTION Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	private void loadRuleDataTypeAndValues()  {
		String jndiProperties = 
				"java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" + LF +
				"java.naming.provider.url=jnp:////localhost:2099" + LF +
				"java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces";
		RuleDataTypeDao typeService = SpringUtil.getAppContext().getBean(RuleDataTypeDao.class);
		for (RuleDataTypeEnum type : RuleDataTypeEnum.values()) {
			RuleDataTypeVo tp = null;
			if (RuleDataTypeEnum.EMAIL_ADDRESS.equals(type)) {
				// insert email address values
				for (EmailAddrType addrType : EmailAddrType.values()) {
					tp = new RuleDataTypeVo(RuleDataTypeEnum.EMAIL_ADDRESS.name(), "$" + addrType.getValue(), "MessageBean");
					typeService.insert(tp);
				}
				// insert column names storing email address
				for (TableColumnName addrColumn : TableColumnName.values()) {
					tp = new RuleDataTypeVo(RuleDataTypeEnum.EMAIL_ADDRESS.name(), "$" + addrColumn.getValue(), "senderDataService");
					typeService.insert(tp);
				}
			}
			else if (RuleDataTypeEnum.QUEUE_NAME.equals(type)) {
				for (QueueNameEnum queue : QueueNameEnum.values()) {
					tp = new RuleDataTypeVo(RuleDataTypeEnum.QUEUE_NAME.name(), "$" + queue.name(), queue.getJmstemplate());
					typeService.insert(tp);
				}
			}
			else if (RuleDataTypeEnum.TEMPLATE_ID.equals(type)) {
				for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
					if (EmailTemplateEnum.SubscribeByEmailReply.equals(tmp)) {
						tp = new RuleDataTypeVo(RuleDataTypeEnum.TEMPLATE_ID.name(), tmp.name(), jndiProperties);
					}
					else {
						tp = new RuleDataTypeVo(RuleDataTypeEnum.TEMPLATE_ID.name(), tmp.name(), null);
					}
					if (typeService.getByTypeValuePair(RuleDataTypeEnum.TEMPLATE_ID.name(), tmp.name())==null) {
						typeService.insert(tp);
					}
				}
			}
			else if (RuleDataTypeEnum.RULE_NAME.equals(type)) {
				for (RuleNameEnum name : RuleNameEnum.values()) {
					if (RuleNameEnum.GENERIC.equals(name)) {
						continue; // skip GENERIC
					}
					tp = new RuleDataTypeVo(RuleDataTypeEnum.RULE_NAME.name(), name.getValue(), null);
					typeService.insert(tp);
				}
			}
			else if (RuleDataTypeEnum.MAILING_LIST.equals(type)) {
				// TODO
				for (MailingListEnum list : MailingListEnum.values()) {
					tp = new RuleDataTypeVo(RuleDataTypeEnum.MAILING_LIST.name(), "$" + list.name(), list.getAcctName());
					typeService.insert(tp);
				}
			}
			else if (RuleDataTypeEnum.EMAIL_PROPERTY.equals(type)) {
				for (VariableName var : VariableName.values()) {
					tp = new RuleDataTypeVo(RuleDataTypeEnum.EMAIL_PROPERTY.name(), "$" + var.getValue(), null);
					typeService.insert(tp);
				}
			}
			else {
				tp = new RuleDataTypeVo(type.name(), type.getDescription(), null);
				typeService.insert(tp);
			}
		}

		System.out.println("Rule data type records inserted.");
	}
	
	private void loadRuleActionDetails() {
		RuleDataTypeDao typeService = SpringUtil.getAppContext().getBean(RuleDataTypeDao.class);
		RuleActionDetailDao detailService = SpringUtil.getAppContext().getBean(RuleActionDetailDao.class);
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		for (RuleActionDetailEnum ruleAction : RuleActionDetailEnum.values()) {
			RuleDataTypeVo tp1 = null;
			String dataType = null;
			if (ruleAction.getDataType()!=null) {
				List<RuleDataTypeVo> tps = typeService.getByDataType(ruleAction.getDataType().name());
				if (!tps.isEmpty()) {
					tp1 = tps.get(0);
					dataType = tp1.getDataType();
				}
			}
			RuleActionDetailVo act = new RuleActionDetailVo(ruleAction.name(),
					ruleAction.getDescription(), ruleAction.getServiceName(),
					ruleAction.getClassName(), dataType, updtTime, Constants.DEFAULT_USER_ID);
			detailService.insert(act);
		}
		
		System.out.println("Rule action detail records inserted.");
	}
	
	private void loadRuleActions() {
		RuleLogicDao logicService = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		RuleActionDetailDao detailService = SpringUtil.getAppContext().getBean(RuleActionDetailDao.class);
		RuleActionDao actionService = SpringUtil.getAppContext().getBean(RuleActionDao.class);
		SenderDataDao senderService = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (RuleActionEnum act : RuleActionEnum.values()) {
			RuleLogicVo logic = logicService.getByRuleName(act.getRuleName().getValue());
			RuleActionDetailVo detail = detailService.getByActionId(act.getActionDetail().name());
			RuleActionVo action = new RuleActionVo(logic.getRuleName(),
					act.getSequence(), now, null, detail.getActionId(),
					StatusId.ACTIVE.getValue(), act.getFieldValues());
			actionService.insert(action);
		}

		try {
			SenderDataVo sender = senderService.getBySenderId("JBatchCorp");
			System.out.println("JbatchCorp Sender found: " + StringUtil.prettyPrint(sender));
		}
		catch (DataAccessException e) {}
		System.out.println("Rule action records inserted.");
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			RuleActionTables ct = new RuleActionTables();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}