package jpa.test.task;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.service.EmailAddressService;
import jpa.service.task.AssignRuleName;

import org.apache.commons.lang3.StringUtils;
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
public class AssignRuleNameTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(AssignRuleNameTest.class);
	
	@Resource
	private AssignRuleName task;
	@Resource
	private EmailAddressService emailService;

	@BeforeClass
	public static void RuleMatchPrepare() {
	}

	@Test
	public void testAssignRuleName() throws Exception {
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
			mBean.setTo(InternetAddress.parse("watched_maibox@domain.com", false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");
		mBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(RuleNameEnum.HARD_BOUNCE.getValue());
		task.process(ctx);
	}
}
