package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.data.preload.EmailTemplateEnum;
import jpa.data.preload.EmailVariableEnum;
import jpa.data.preload.MailingListEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.MsgHeader;
import jpa.message.util.EmailIdParser;
import jpa.model.message.MessageHeader;
import jpa.model.message.MessageInbox;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;
import jpa.service.task.BroadcastToList;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;
import jpa.variable.Renderer;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class BroadcastToListTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(BroadcastToListTest.class);
	
	@Resource
	private BroadcastToList task;
	@Resource
	private EmailAddressService emailService;
	@Resource
	private MessageInboxService inboxService;

	@BeforeClass
	public static void RuleMatchPrepare() {
	}

	@Test
	public void testBroadcastToList() throws Exception {
		MessageBean mBean = new MessageBean();
		mBean.setSubject(EmailTemplateEnum.SampleNewsletter2.getSubject());
		mBean.setValue(EmailTemplateEnum.SampleNewsletter2.getBodyText());
		mBean.setMailboxUser("testUser");
		mBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
		MessageInbox _minbox = inboxService.getLastRecord();
		mBean.setMsgId(_minbox.getRowId());
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(MailingListEnum.SMPLLST1.name());
		task.process(ctx);
		
		assertFalse(ctx.getRowIds().isEmpty());
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		for (Integer rowid : ctx.getRowIds()) {
			MessageInbox minbox = inboxService.getAllDataByPrimaryKey(rowid);
			String emailId_body = parser.parseMsg(minbox.getMsgBody());
			mBean.getHeaders().clear();
			for (MessageHeader mhdr : minbox.getMessageHeaderList()) {
				MsgHeader hdr = new MsgHeader();
				hdr.setName(mhdr.getHeaderName());
				hdr.setValue(mhdr.getHeaderValue());
				mBean.getHeaders().add(hdr);
			}
			String emailId_xhdr = parser.parseHeaders(mBean.getHeaders());
			System.out.println("Email_Id from body: " + emailId_body + ", from XHdr: " + emailId_xhdr);
			assertTrue(emailId_body.equals(emailId_xhdr));
			assertTrue(emailId_body.equals(minbox.getRowId()+""));
			
			String to_sent = minbox.getToAddress().getAddress();
			RenderVariableVo vo = new RenderVariableVo(
					EmailVariableEnum.SubscriberAddress.name(),
					to_sent,
					VariableType.ADDRESS);
			Map<String, RenderVariableVo> vars = new HashMap<String, RenderVariableVo>();
			vars.put(vo.getVariableName(), vo);
			String subj = Renderer.getInstance().render(
					EmailTemplateEnum.SampleNewsletter2.getSubject(), vars,
					new HashMap<String, ErrorVariableVo>());
			System.out.println("Subject rendered: " + subj);
			System.out.println("Subject msginbox: " + minbox.getMsgSubject());
			assertTrue(subj.equals(minbox.getMsgSubject()));
		}
	}
}
