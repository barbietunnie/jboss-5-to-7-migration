package com.legacytojava.message.bo.inbox;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.bo.outbox.MsgOutboxBo;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.dao.idtokens.EmailIdParser;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-jmsqueue_rmt-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MsgInboxBoTest {
	static final Logger logger = Logger.getLogger(MsgInboxBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgInboxBo msgInboxBo;
	@Resource
	private MsgOutboxBo msgOutboxBo;
	@Resource
	private MessageParser parser;
	@BeforeClass
	public static void  MsgInboxBoPrepare() {
	}
	@Test
	public void testMsgInboxBo() throws Exception {
		long msgId = 1L;
		try {
			MessageBean messageBean = msgOutboxBo.getMessageByPK(msgId);
			assertNotNull(messageBean);
			if (isDebugEnabled) {
				logger.debug("MessageBean returned:" + LF + messageBean);
			}
			parser.parse(messageBean);
			
			// build MsgHeader
			MsgHeader header = new MsgHeader();
			header.setName(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
			header.setValue(EmailIdParser.getDefaultParser().wrapupEmailId4XHdr(msgId));
			List<MsgHeader> headers = new ArrayList<MsgHeader>();
			headers.add(header);
			messageBean.setHeaders(headers);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean After:" + LF + messageBean);
			}
			if (messageBean.getRuleName()==null) {
				messageBean.setRuleName(RuleNameType.GENERIC.toString());
			}
			msgId = msgInboxBo.saveMessage(messageBean);
			logger.info("msgInboxBo.saveMessage - MsgId returned: " + msgId);
			MsgInboxVo vo = msgInboxBo.getAllDataByMsgId(msgId);
			assertNotNull(vo);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
