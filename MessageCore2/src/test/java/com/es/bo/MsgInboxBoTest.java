package com.es.bo;

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

import com.es.bo.inbox.MessageParserBo;
import com.es.bo.inbox.MsgInboxBo;
import com.es.bo.outbox.MsgOutboxBo;
import com.es.dao.outbox.MsgRenderedDao;
import com.es.data.preload.RuleNameEnum;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MsgHeader;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.outbox.MsgRenderedVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
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
	private MessageParserBo parser;
	@Resource
	private MsgRenderedDao msgRenderedDao;
	@BeforeClass
	public static void  MsgInboxBoPrepare() {
	}
	@Test
	public void testMsgInboxBo() throws Exception {
		long renderId = 1L;
		try {
			MsgRenderedVo renderedVo = msgRenderedDao.getByPrimaryKey(renderId);
			if (renderedVo == null) {
				renderId = msgRenderedDao.getLastRecord().getRenderId();
			}
			MessageBean messageBean = msgOutboxBo.getMessageByPK(renderId);
			assertNotNull(messageBean);
			if (isDebugEnabled) {
				logger.debug("MessageBean returned:" + LF + messageBean);
			}
			parser.parse(messageBean);
			
			// build MsgHeader
			MsgHeader header = new MsgHeader();
			header.setName(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
			header.setValue(EmailIdParser.getDefaultParser().createEmailId4XHdr(renderId));
			List<MsgHeader> headers = new ArrayList<MsgHeader>();
			headers.add(header);
			messageBean.setHeaders(headers);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean After:" + LF + messageBean);
			}
			if (messageBean.getRuleName()==null) {
				messageBean.setRuleName(RuleNameEnum.GENERIC.getValue());
			}
			renderId = msgInboxBo.saveMessage(messageBean);
			logger.info("msgInboxBo.saveMessage - MsgId returned: " + renderId);
			MsgInboxVo vo = msgInboxBo.getAllDataByMsgId(renderId);
			assertNotNull(vo);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
