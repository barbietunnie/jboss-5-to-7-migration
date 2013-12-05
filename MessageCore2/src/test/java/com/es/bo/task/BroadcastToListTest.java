package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.render.ErrorVariable;
import com.es.bo.render.RenderVariable;
import com.es.bo.render.Renderer;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgHeaderDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.Constants;
import com.es.data.constant.VariableType;
import com.es.data.preload.EmailTemplateEnum;
import com.es.data.preload.EmailVariableEnum;
import com.es.data.preload.MailingListEnum;
import com.es.data.preload.RuleNameEnum;
import com.es.msg.util.EmailIdParser;
import com.es.msg.util.MsgHeaderVoUtil;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgHeaderVo;
import com.es.vo.inbox.MsgInboxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class BroadcastToListTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(BroadcastToListTest.class);
	
	@Resource
	private BroadcastToList task;
	@Resource
	private EmailAddressDao emailDao;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private MsgHeaderDao headerDao;

	@BeforeClass
	public static void BroadcastPrepare() {
	}

	@Test
	public void testBroadcastToList() throws Exception {
		EmailTemplateEnum testNewsLetter = EmailTemplateEnum.SampleNewsletter2;
		MessageBean mBean = new MessageBean();
		mBean.setSubject(testNewsLetter.getSubject());
		mBean.setValue(testNewsLetter.getBodyText());
		mBean.setMailboxUser("testUser");
		mBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
		MsgInboxVo _minbox = inboxDao.getLastRecord();
		mBean.setMsgId(_minbox.getMsgId());
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(MailingListEnum.SMPLLST1.name());
		task.process(ctx);
		
		// now verify results
		System.out.println("Verifying Results ##################################################################");
		assertFalse(ctx.getMsgIdList().isEmpty());
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		for (Long msgId : ctx.getMsgIdList()) {
			MsgInboxVo minbox = inboxDao.getByPrimaryKey(msgId);
			String emailId_body = parser.parseMsg(minbox.getMsgBody());
			List<MsgHeaderVo> headers = headerDao.getByMsgId(msgId);
			mBean.getHeaders().clear();
			mBean.getHeaders().addAll(MsgHeaderVoUtil.toMsgHeaderList(headers));
			String emailId_xhdr = parser.parseHeaders(mBean.getHeaders());
			System.out.println("Email_Id from body: " + emailId_body + ", from XHdr: " + emailId_xhdr);
			assertNotNull(emailId_xhdr);
			if (emailId_body != null) { // in case of text message
				assertTrue(emailId_body.equals(emailId_xhdr));
			}
			assertTrue(emailId_xhdr.equals(minbox.getMsgId()+""));
			
			String to_sent = minbox.getToAddress();
			RenderVariable vo = new RenderVariable(
					EmailVariableEnum.SubscriberAddress.name(),
					to_sent,
					VariableType.ADDRESS);
			Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
			vars.put(vo.getVariableName(), vo);
			String subj = Renderer.getInstance().render(
					testNewsLetter.getSubject(), vars,
					new HashMap<String, ErrorVariable>());
			System.out.println("Subject rendered: " + subj);
			System.out.println("Subject msginbox: " + minbox.getMsgSubject());
			assertTrue(subj.equals(minbox.getMsgSubject()));
			
			if (EmailTemplateEnum.SampleNewsletter2.equals(testNewsLetter)) {
				assertTrue(minbox.getMsgBody().indexOf(to_sent)>0);
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			assertTrue(minbox.getMsgBody().indexOf(sdf.format(new java.util.Date()))>0);
		}
	}
}
