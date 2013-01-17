package com.legacytojava.message.bo.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanBuilder;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.bo.inbox.MessageParser;
import com.legacytojava.message.bo.inbox.MsgInboxBo;
import com.legacytojava.message.bo.outbox.MsgOutboxBo;
import com.legacytojava.message.bo.outbox.MsgOutboxBoTest;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.idtokens.EmailIdParser;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.dao.inbox.MsgStreamDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.message.vo.inbox.MsgInboxWebVo;
import com.legacytojava.message.vo.outbox.MsgStreamVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-bo_jms-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class BoTestBase {
	protected static final Logger logger = Logger.getLogger(BoTestBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	protected final static String LF = System.getProperty("line.separator","\n");
	protected final long renderId = 1L;

	protected static AbstractApplicationContext factory;
	@Resource
	protected MsgInboxBo msgInboxBo;
	@Resource
	protected MsgOutboxBo msgOutboxBo;
	@Resource
	protected MsgStreamDao msgStreamDao;
	@Resource
	protected MessageParser parser;
	@Resource
	protected MsgInboxDao msgInboxDao;
	@Resource
	protected EmailAddrDao emailAddrDao;
	@BeforeClass
	public static void prepare() {
		factory = SpringUtil.getAppContext();
	}
	@Before
	public void checkMsgrendered() throws AddressException, DataValidationException, ParseException {
		if (msgOutboxBo.getMessageByPK(renderId)==null) {
			Result result = JUnitCore.runClasses(MsgOutboxBoTest.class);
			for (Failure failure : result.getFailures()) {
				logger.error(failure.toString());
			}
		}
	}
	protected Message createMimeMessage(byte[] mailStream) throws MessagingException {
		Message msg = MessageBeanUtil.createMimeMessage(mailStream);
		return msg;
	}

	protected MessageBean createMessageBean(Message msg) throws MessagingException {
		try {
			MessageBean msgBean = MessageBeanBuilder.processPart(msg, null);
			return msgBean;
		}
		catch (IOException e) {
			throw new MessagingException(e.toString());
		}
	}
	
	protected MessageBean buildMessageBeanFromMsgStream() throws Exception {
		MsgStreamVo msgStreamVo = msgStreamDao.getLastRecord();
		if (msgStreamVo == null) {
			addMsgToInbox();
		}
		msgStreamVo = msgStreamDao.getLastRecord();
		assertNotNull(msgStreamVo);
		Message msg = createMimeMessage(msgStreamVo.getMsgStream());
		MessageBean messageBean = createMessageBean(msg);
		messageBean.setMsgId(msgStreamVo.getMsgId()); // to be converted as MsgRefId
		Address[] fromAddr = {new InternetAddress("botest.from@test.com")};
		messageBean.setFrom(fromAddr);
		Address[] toAddr = {new InternetAddress("botest.to@test.com")};
		messageBean.setTo(toAddr);
		messageBean.setFinalRcpt("botest.finalrcpt@test.com");
		return messageBean;
	}

	protected MsgInboxVo selectMsgInboxByMsgId(long msgId) {
		MsgInboxVo vo = (MsgInboxVo)msgInboxDao.getByPrimaryKey(msgId);
		if (vo!=null) {
			System.out.println("MsgInboxDao - selectMsgInboxByMsgId: "+LF+vo);
		}
		return vo;
	}

	protected List<MsgInboxWebVo> selectMsgInboxByMsgRefId(long msgRefId) {
		List<MsgInboxWebVo> list = (List<MsgInboxWebVo>)msgInboxDao.getByMsgRefId(msgRefId);
		for (MsgInboxWebVo vo : list) {
			logger.info("MsgInboxDao - selectMsgInboxByMsgRefId: "+LF+vo);
		}
		return list;
	}

	protected EmailAddrVo selectEmailAddrByAddress(String emailAddr) {
		EmailAddrVo addrVo = emailAddrDao.findByAddress(emailAddr);
		logger.info("EmailAddrDao - selectEmailAddrByAddress: "+LF+addrVo);
		return addrVo;
	}

	protected MessageBean addMsgToInbox() throws Exception {
		long msgRefId = 1L;
		try {
			MessageBean messageBean = msgOutboxBo.getMessageByPK(renderId);
			assertNotNull(messageBean);
			if (isDebugEnabled) {
				logger.debug("MessageBean returned:" + LF + messageBean);
			}
			parser.parse(messageBean);
			
			// build MsgHeader
			MsgHeader header = new MsgHeader();
			header.setName(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
			header.setValue(EmailIdParser.getDefaultParser().wrapupEmailId4XHdr(msgRefId));
			List<MsgHeader> headers = new ArrayList<MsgHeader>();
			headers.add(header);
			messageBean.setHeaders(headers);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean After:" + LF + messageBean);
			}
			if (messageBean.getRuleName()==null) {
				messageBean.setRuleName(RuleNameType.GENERIC.name());
			}
			long msgId = msgInboxBo.saveMessage(messageBean);
			logger.info("msgInboxBo.saveMessage - MsgId returned: " + msgId);
			MsgInboxVo vo = msgInboxBo.getAllDataByMsgId(msgId);
			assertNotNull(vo);
			MessageBean msgBean = MessageBeanBuilder.createMessageBean(vo);
			return msgBean;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/*
	 * Define assignable rule names
	 */
	protected final static RuleNameType[] RuleNames = {
			RuleNameType.HARD_BOUNCE,
			RuleNameType.SOFT_BOUNCE,
			RuleNameType.MAILBOX_FULL,
			RuleNameType.MAIL_BLOCK,
			RuleNameType.SPAM_BLOCK,
			RuleNameType.VIRUS_BLOCK,
			RuleNameType.CSR_REPLY,
			RuleNameType.RMA_REQUEST

	};
}
