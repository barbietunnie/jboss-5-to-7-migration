package jpa.test.message;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.message.MessageBodyBuilder;
import jpa.message.MsgHeader;
import jpa.message.util.EmailIdParser;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBodyBuilderTest {

	@BeforeClass
	public static void MessageBodyBuilderPrepare() {
	}

	@Test
	public void testMessageBodyBuilder() {
		String LF = System.getProperty("line.separator", "\n");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		int bodyId = 123456;
		int xheaderId = 345678;
		String emailIdStr = parser.wrapupEmailId(bodyId);
		String emailIdXhdr = parser.wrapupEmailId4XHdr(xheaderId);

		// embed email_id for HTML email
		MessageBean msgBean = new MessageBean();
		msgBean.setContentType("text/html");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("<HTML>This is the original message." + Constants.MSG_DELIMITER_BEGIN
				+ emailIdStr + Constants.MSG_DELIMITER_END + "</HTML>");
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setMsgId(Integer.valueOf(999999));
		msgBean.setBody(MessageBodyBuilder.getBody(msgBean));
		System.out.println(">>>>>>>>>>>>>>>>HTML Message:" + LF + msgBean);

		String msgId = parser.parseMsg(msgBean.getBody());
		System.out.println("Email_Id from Body: " + msgId);
		assertTrue((""+bodyId).equals(msgId));

		// embed email_id for plain text email
		msgBean = new MessageBean();
		msgBean.setContentType("text/plain");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("This is the original message.\n" + Constants.MSG_DELIMITER_BEGIN
				+ emailIdStr + Constants.MSG_DELIMITER_END);
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setMsgId(Integer.valueOf(999999));
		msgBean.setBody(MessageBodyBuilder.getBody(msgBean));
		MsgHeader hdr = new MsgHeader();
		hdr.setName(parser.getEmailIdXHdrName());
		hdr.setValue(emailIdXhdr);
		List<MsgHeader> hdrs = new ArrayList<MsgHeader>();
		hdrs.add(hdr);
		msgBean.setHeaders(hdrs);
		System.out.println(">>>>>>>>>>>>>>>>TEXT Message:" + LF +msgBean);

		// parse email_id
		msgId = parser.parseMsg(msgBean.getBody());
		System.out.println("Email_Id from Body: " + msgId);
		assertTrue((""+bodyId).equals(msgId));
		msgId = parser.parseHeaders(msgBean.getHeaders());
		System.out.println("Email_Id from X-Header: " + msgId);
		assertTrue((""+xheaderId).equals(msgId));
	}
}
