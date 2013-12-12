package com.legacytojava.message.table;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.context.ApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.MsgDataType;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.TableColumnName;
import com.legacytojava.message.dao.action.MsgActionDao;
import com.legacytojava.message.dao.action.MsgActionDetailDao;
import com.legacytojava.message.dao.action.MsgDataTypeDao;
import com.legacytojava.message.vo.action.MsgActionDetailVo;
import com.legacytojava.message.vo.action.MsgActionVo;
import com.legacytojava.message.vo.action.MsgDataTypeVo;

public class LoadActionTables {
	String LF = System.getProperty("line.separator", "\n");
	public static void main(String[] args) {
		LoadActionTables loadActionTables = new LoadActionTables();
		try {
			loadActionTables.loadData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void loadData() {
		loadData(null);
	}
	
	public void loadData(ApplicationContext factory)  {
		if (factory == null) {
			factory = SpringUtil.getDaoAppContext();
		}
		MsgDataTypeDao msgDataTypeDao = (MsgDataTypeDao) factory.getBean("msgDataTypeDao");
		MsgActionDao msgActionDao = (MsgActionDao) factory.getBean("msgActionDao");
		MsgActionDetailDao msgActionDetailDao = (MsgActionDetailDao) factory
				.getBean("msgActionDetailDao");
		load(msgDataTypeDao);
		load(msgActionDetailDao);
		load(msgActionDao);
	}
	
	void load(MsgDataTypeDao msgDataTypeDao) {
		String jndiProperties = 
			"java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" + LF +
			"java.naming.provider.url=jnp:////localhost:2099" + LF +
			"java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces";
		
		MsgDataTypeVo vo = null;
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.FROM_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.TO_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.CC_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.BCC_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.FINAL_RCPT_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.ORIG_RCPT_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + EmailAddressType.FORWARD_ADDR, "MessageBean");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.SECURITY_DEPT_ADDR, "clientDao");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.CUSTOMER_CARE_ADDR, "clientDao");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.RMA_DEPT_ADDR, "clientDao");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.VIRUS_CONTROL_ADDR, "clientDao");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.SPAM_CONTROL_ADDR, "clientDao");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("EMAIL_ADDRESS", "$" + TableColumnName.CHALLENGE_HANDLER_ADDR, "clientDao");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("QUEUE_NAME", "$RMA_REQUEST_INPUT", "rmaRequestInputJmsTemplate");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo("QUEUE_NAME", "$CUSTOMER_CARE_INPUT", "customerCareInputJmsTemplate");
		msgDataTypeDao.insert(vo);
		vo = new MsgDataTypeVo(MsgDataType.TEMPLATE_ID, "SubscribeByEmailReply", jndiProperties);
		msgDataTypeDao.insert(vo);
		
		// insert rule names
		for (RuleNameType name : RuleNameType.values()) {
			if (RuleNameType.GENERIC.equals(name)) {
				continue; // skip GENERIC
			}
			String ruleName = name.toString();
			vo = new MsgDataTypeVo("RULE_NAME", ruleName, null);
			msgDataTypeDao.insert(vo);
		}
		
		System.out.println("load() completed.");
	}
	
	void load(MsgActionDetailDao msgActionDetailDao) {
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		MsgActionDetailVo act = null;
		act = new MsgActionDetailVo("ACTIVATE","activete email address","activateBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("BOUNCE++","increase bounce count","bounceBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("CLOSE","close the message","closeBo",null,null,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("CSR_REPLY","send off the reply from csr","csrReplyBo",null,null,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("AUTO_REPLY","reply to the message automatically","autoReplyBo",null,MsgDataType.TEMPLATE_ID,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("MARK_DLVR_ERR","mark delivery error","deliveryErrorBo",null,null,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("DROP","drop the message","dropBo","com.legacytojava.message.bo.DropBoImpl",null,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("FORWARD","forward the message","forwardBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("TO_CSR","redirect to message queue","toCsrBo",null,"QUEUE_NAME",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("SAVE","save the message","saveBo",null,null,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("SENDMAIL","simply send the mail off","sendMailBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("SUSPEND","suspend email address","suspendBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("UNSUBSCRIBE","remove from the mailing list","unsubscribeBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("SUBSCRIBE","subscribe to the mailing list","subscribeBo",null,"EMAIL_ADDRESS",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("BROADCAST","broadcast to a mailing list","broadcastBo",null,null,updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("ASSIGN_RULENAME","set a rule mame and re-queue","assignRuleNameBo",null,"RULE_NAME",updtTime,"testuser");
		msgActionDetailDao.insert(act);
		act = new MsgActionDetailVo("OPEN","open the message","openBo",null,null,updtTime,"testuser");
		msgActionDetailDao.insert(act);

		System.out.println("load() completed.");
	}
	
	void load(MsgActionDao msgActionDao) {
		Timestamp now = new Timestamp(new Date().getTime());
		MsgActionVo act = null;
		// for build-in rules
		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.toString(),2,now,null,"SUSPEND","A","$"+EmailAddressType.FINAL_RCPT_ADDR+","+"$"+EmailAddressType.ORIG_RCPT_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.toString(),3,now,null,"MARK_DLVR_ERR","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.HARD_BOUNCE.toString(),4,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SOFT_BOUNCE.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SOFT_BOUNCE.toString(),2,now,null,"BOUNCE++","A","$"+EmailAddressType.FINAL_RCPT_ADDR+","+"$"+EmailAddressType.ORIG_RCPT_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SOFT_BOUNCE.toString(),3,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MAILBOX_FULL.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MAILBOX_FULL.toString(),2,now,null,"BOUNCE++","A","$"+EmailAddressType.FINAL_RCPT_ADDR+","+"$"+EmailAddressType.ORIG_RCPT_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MAILBOX_FULL.toString(),3,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MSGSIZE_TOO_BIG.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MSGSIZE_TOO_BIG.toString(),2,now,null,"TO_CSR","A","$CUSTOMER_CARE_INPUT");
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MAIL_BLOCK.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MAIL_BLOCK.toString(),2,now,null,"FORWARD","A","$" + TableColumnName.SPAM_CONTROL_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SPAM_BLOCK.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SPAM_BLOCK.toString(),2,now,null,"FORWARD","A","$"+TableColumnName.SPAM_CONTROL_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.VIRUS_BLOCK.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.VIRUS_BLOCK.toString(),2,now,null,"FORWARD","A","$"+TableColumnName.VIRUS_CONTROL_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.CHALLENGE_RESPONSE.toString(),1,now,null,"FORWARD","A","$"+TableColumnName.CHALLENGE_HANDLER_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.AUTO_REPLY.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.AUTO_REPLY.toString(),2,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.CC_USER.toString(),1,now,null,"DROP","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MDN_RECEIPT.toString(),1,now,null,"ACTIVATE","A","$"+EmailAddressType.FINAL_RCPT_ADDR+","+"$"+EmailAddressType.ORIG_RCPT_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.MDN_RECEIPT.toString(),2,now,null,"DROP","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.CSR_REPLY.toString(),1,now,null,"CSR_REPLY","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SEND_MAIL.toString(),1,now,null,"SENDMAIL","A","$"+EmailAddressType.TO_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.RMA_REQUEST.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.RMA_REQUEST.toString(),2,now,null,"ACTIVATE","A","$"+EmailAddressType.FROM_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.RMA_REQUEST.toString(),3,now,null,"TO_CSR","A","$RMA_REQUEST_INPUT");
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.UNSUBSCRIBE.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.UNSUBSCRIBE.toString(),2,now,null,"UNSUBSCRIBE","A","$"+EmailAddressType.FROM_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.UNSUBSCRIBE.toString(),3,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),2,now,null,"SUBSCRIBE","A","$"+EmailAddressType.FROM_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),3,now,null,"ACTIVATE","A","$"+EmailAddressType.FROM_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),4,now,null,"AUTO_REPLY","A","SubscribeByEmailReply");
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.SUBSCRIBE.toString(),5,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.BROADCAST.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.BROADCAST.toString(),2,now,null,"BROADCAST","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.BROADCAST.toString(),3,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.GENERIC.toString(),1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.GENERIC.toString(),2,now,null,"ACTIVATE","A","$"+EmailAddressType.FROM_ADDR);
		msgActionDao.insert(act);
		act = new MsgActionVo(RuleNameType.GENERIC.toString(),3,now,null,"TO_CSR","A","$CUSTOMER_CARE_INPUT");
		msgActionDao.insert(act);
		//act = new MsgActionVo(Constants.RULENAME.UNIDENTIFIED.toString(),1,now,"JBatchCorp","SAVE","A",null);
		//msgActionDao.insert(act);
		//act = new MsgActionVo(Constants.RULENAME.UNIDENTIFIED.toString(),2,now,"JBatchCorp","FORWARD","A","$"+Constants.CUSTOMER_CARE_ADDR);
		//msgActionDao.insert(act);
		
		// for custom rules
		act = new MsgActionVo("Unattended_Mailbox",1,now,null,"DROP","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("OutOfOffice_AutoReply",1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("OutOfOffice_AutoReply",2,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("Contact_Us",1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("XHeader_SpamScore",1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("XHeader_SpamScore",2,now,null,"CLOSE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("Executable_Attachment",1,now,null,"DROP","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("HardBouce_WatchedMailbox",1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("HardBouce_WatchedMailbox",2,now,null,"OPEN","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("HardBounce_NoFinalRcpt",1,now,null,"SAVE","A",null);
		msgActionDao.insert(act);
		act = new MsgActionVo("HardBounce_NoFinalRcpt",2,now,null,"OPEN","A",null);
		msgActionDao.insert(act);

		System.out.println("load() completed.");
	}
}