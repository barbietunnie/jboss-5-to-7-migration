package com.legacytojava.message.ejb.rule;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.inbox.MessageParser;
import com.legacytojava.message.bo.inbox.MsgInboxBo;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

/**
 * Session Bean implementation class MsgInbox
 */
@Stateless(mappedName = "ejb/MsgInbox")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(MsgInboxRemote.class)
@Local(MsgInboxLocal.class)
public class MsgInbox implements MsgInboxRemote, MsgInboxLocal {
	protected static final Logger logger = Logger.getLogger(MsgInbox.class);
	@Resource
	SessionContext context;
	private MsgInboxBo msgInboxBo;
	private MessageParser parser;
    /**
     * Default constructor. 
     */
    public MsgInbox() {
		msgInboxBo = (MsgInboxBo) SpringUtil.getAppContext().getBean("msgInboxBo");
		parser = (MessageParser) SpringUtil.getAppContext().getBean("messageParser");
    }

	public long saveMessage(MessageBean messageBean) throws DataValidationException {
		long msgId = msgInboxBo.saveMessage(messageBean);
		return msgId;
	}

	public MsgInboxVo getMessageByPK(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxBo.getMessageByPK(msgId);
		return msgInboxVo;
	}

	public void parseMessage(MessageBean msgBean) throws DataValidationException {
		parser.parse(msgBean);
	}
}
