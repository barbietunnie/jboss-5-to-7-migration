package com.es.bo;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.es.data.preload.RuleElementEnum;

public class RuleRegexEnumTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(RuleRegexEnumTest.class);
	
	public static void RuleRegexEnumPrepres() {
	}
	
	@Test
	public void testChallengeResponse() {
		String bodyEarthLink = "I apologize for this automatic reply to your email." + LF + LF +
		"To control spam, I now allow incoming messages only from senders I have approved beforehand." + LF + LF +
		"If you could like to be added to my list of approved senders, please fill out the short request form (see link" + LF +
		"below). Once I approve you, I will receive your original message in my inbox. You do not need to resend your" + LF +
		"message. I apologize for this one-time inconvenience." + LF + LF +
		"Click the link below to fill out the request.";
		
		String bodyIpermitMail = 
		"Please click the link below so that I can receive your message." + LF +
		"Your message is being held by my anti-spam system, iPermitMail." + LF +
		"http://www.ipermitmail.com/ipm/Messa...k=371054102204";
		
		String bodyDevnull = "Hello." + LF +
		"Your mail to peter@gradwell.com requires your confirmation." + LF +
		"To confirm that your mail is legitimate, simply click on this URL:" + LF +
		"https://www.gradwell.com/u/?c=1f7c824d2f59695d5b4dd5f0" + LF +
		"Once you have confirmed this mail then you will never have" + LF +
		"to confirm any more mails to peter@gradwell.com." + LF +
		"You must confirm your mail within one week or else it will be" + LF +
		"deleted with no further notice to you." + LF +
		"Thank you.";
		
		String bodyVanquish = 
		"The message that you sent to me (Kenneth E. Slaughter) has not yet been delivered:" + LF +
		"From: tech@openbsd.org" + LF +
		"Subject: Re: Re: Message" + LF +
		"Date: Tue, 27 Apr 2004 13:36:38 -0500" + LF +
		"I am now using Vanquish to avoid spam.  This automated message" + LF +
		"is an optional feature of that service, which I have enabled." + LF +
		"Please accept this one-time request to confirm that the above" + LF +
		"message actually came from you.  Your confirmation will release" + LF +
		"the message and allow all future messages from your address." + LF +
		"Click here to confirm:" + LF +
		"http://confirm.vanquish.com/?U=b+Etxkx0Uvgx5q2Hp4CETg" + LF +
		"Vanquish respects my privacy and yours.  Your confirmation" + LF +
		"gets your mail delivered to me now and in the future.  It" + LF +
		"does not serve any marketing purpose.  Learn how privacy is" + LF +
		"assured: www.vanquish.com/privacy";
		
		String bodyQurb = "This is an automated message." + LF +
		"I apologize for the inconvenience, but I need your help in fighting" + LF +
		"spam. I'm using a program called Qurb which automatically maintains" + LF +
		"a list of approved senders for me. Messages from approved senders go" + LF +
		"directly to my Inbox. Messages from addresses that Qurb hasn't seen" + LF +
		"before are quarantined until the address of the sender can be" + LF +
		"confirmed.";
		
		String bodyRegex = RuleElementEnum.ChalResp_Body_Match_1.getTargetText();
		Pattern bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = bodyPattern.matcher(bodyEarthLink);
		logger.info("challenge_response body matching? ");
		assertTrue("found", matcher.find());
		matcher.reset(bodyIpermitMail);
		assertTrue("found", matcher.find());
		matcher.reset(bodyDevnull);
		assertTrue("found", matcher.find());
		matcher.reset(bodyVanquish);
		assertTrue("found", matcher.find());
		matcher.reset(bodyQurb);
		assertTrue("found", matcher.find());
		
		String headerRegex = RuleElementEnum.CHALLENGE_RESPONSE_1.getTargetText();
		Pattern headerPattern = Pattern.compile(headerRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = headerPattern.matcher("challenge_resp@ipermitmail.com");
		logger.info("challenge_response header matching? ");
		assertTrue("found", matcher.find());
		matcher.reset("spamhippo@cr.com");
		assertTrue("found", matcher.find());
		
		String subjRegex = RuleElementEnum.CHALLENGE_RESPONSE_2.getTargetText();
		Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
		matcher = subjPattern.matcher("Your mail to peter@gradwell.com requires confirmation");
		logger.info("challenge_response subject matching? ");
		assertTrue("found", matcher.find());
		matcher.reset("CR found as [Qurb 100]");
		assertTrue("found", matcher.find());
		
		String fromRegex = RuleElementEnum.CHALLENGE_RESPONSE_3.getTargetText();
		Pattern fromPattern = Pattern.compile(fromRegex, Pattern.CASE_INSENSITIVE);
		matcher = fromPattern.matcher("Confirmation from Kenneth E. Slaughter " + "<confirm-tech=openbsd.org@spamguard.vanquish.com>");
		logger.info("challenge_response from matching? ");
		assertTrue("found", matcher.find());
	}
	
	@Test
	public void TestPostmasterHardBounce() {
		String postmasterFrom = RuleElementEnum.HARD_BOUNCE_1.getTargetText();
		Pattern postPattern = Pattern.compile(postmasterFrom, Pattern.CASE_INSENSITIVE);
		Matcher m = postPattern.matcher("postmaster@abc.com");
		logger.info("postmater matching? ");
		assertTrue("found", m.find());
		m.reset("administrator2@abc.com");
		assertTrue("found", m.find());

		String mailerdaemonFrom = RuleElementEnum.HARD_BOUNCE_2.getTargetText();
		Pattern daemPattern = Pattern.compile(mailerdaemonFrom, Pattern.CASE_INSENSITIVE);
		m = daemPattern.matcher("mailer-daemon@abc.com");
		assertTrue("found", m.find());
	}
	
	@Test
	public void TestOutOfOfficeAutoReply() {
		String subjRegex = RuleElementEnum.OUF_OF_OFFICE_AUTO_REPLY_1.getTargetText();
		Pattern pattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher("Re: (away from the office)");
		logger.info("Out of office matching? ");
		assertTrue("found", m.find());
		m.reset("out of the office auto reply");
		assertTrue("found", m.find());
	}
	
	@Test
	public void TestAutoReply() {
		String fromRegex = RuleElementEnum.AUTO_REPLY_2.getTargetText();
		Pattern fromPattern = Pattern.compile(fromRegex);
		Matcher m = fromPattern.matcher("autoresponder@abc.com");
		logger.info("Auto-reply matching? ");
		assertTrue("found", m.find());
		m.reset("autoresponse-ooo@abc.com");
		assertTrue("found", m.find());
		
		String subjRegex = RuleElementEnum.AUTO_REPLY_1.getTargetText();
		Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
		m = subjPattern.matcher("Re: Exception Autoreply: from SAXK");
		assertTrue("found", m.find());
		m.reset("Auto-Respond E-Mail from aaa");
		assertTrue("found", m.find());
		
		String bodyRegex = RuleElementEnum.AUTO_REPLY_3.getTargetText();
		Pattern bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = bodyPattern.matcher("This is an autoresponder. I'll never see your message SAXK");
		logger.info("Auto-reply matching? ");
		assertTrue("found", m.find());
		m.reset("I.ll be away from the office until next monday.");
		assertTrue("found", m.find());
		m.reset("I.ll be on vacation until next monday.");
		assertTrue("found", m.find());
		m.reset("I.m away until next monday and am unable to read your message.");
		assertTrue("found", m.find());
		m.reset("I am currently out of the office.");
		assertTrue("found", m.find());
	}
	
	@Test
	public void testDeliveryFailure() {
		String subjRegex = RuleElementEnum.HardBounce_Subj_Match_1.getTargetText();
		Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
		Matcher m = subjPattern.matcher("Returned mail: Error During Delivery");
		logger.info("Delivery Failure matching? ");
		assertTrue("found", m.find());
		m.reset("Returned mail: User Unknown");
		assertTrue("found", m.find());
		m.reset("Mail could not be delivered");
		assertTrue("found", m.find());
		m.reset("Undelivered Mail Returned to Sender");
		assertTrue("found", m.find());
		m.reset("Email Addressing Error (aaa)");
		assertTrue("found", m.find());
		m.reset("Returned mail: Delivery Error...");
		assertTrue("found", m.find());
		
		String bodyRegex = RuleElementEnum.HardBounce_Body_Match_1.getTargetText();
		Pattern bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = bodyPattern.matcher("You've reached a non-working address. Please check");
		logger.info("Delivery Failure matching? ");
		assertTrue("found", m.find());
		m.reset("Delivery Status: 5.4.1 Please check");
		assertTrue("found", m.find());
	}
	
	@Test
	public void testSpamblocker() {
		String fromRegex = RuleElementEnum.SPAM_BLOCK_3.getTargetText();
		Pattern fromPattern = Pattern.compile(fromRegex, Pattern.CASE_INSENSITIVE);
		Matcher m = fromPattern.matcher("surfcontrol_agent@abc.com");
		logger.info("Spam blocker matching? ");
		assertTrue("found", m.find());

		String returnPathRegex = RuleElementEnum.SPAM_BLOCK_4.getTargetText();
		Pattern pathPattern = Pattern.compile(returnPathRegex, Pattern.CASE_INSENSITIVE);
		m = pathPattern.matcher("pleaseforward@abc.com");
		assertTrue("found", m.find());
		
		String subjRegex = RuleElementEnum.SPAM_BLOCK_1.getTargetText();
		Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
		m = subjPattern.matcher("[MailServer Notification]Attachment Blocking Notification");
		logger.info("Spam blocker matching? ");
		assertTrue("found", m.find());
		m.reset("GWAVA Sender Notification .RBL block2");
		assertTrue("found", m.find());
	}
	
	@Test
	public void testVirusBlocker() {
		String virusFrom = "(?:virus|scanner|devnull)\\S*\\@";
		Pattern fromPattern = Pattern.compile(virusFrom, Pattern.CASE_INSENSITIVE);
		Matcher m = fromPattern.matcher("virus_blocker@abc.com");
		logger.info("Virus blocker matching? ");
		assertTrue("found", m.find());
		
		String body1 = RuleElementEnum.VirusBlock_Body_Match_1.getTargetText();
		Pattern body1Pattern = Pattern.compile(body1, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = body1Pattern.matcher("There host The attachment was quarantined rejected found in ");
		assertTrue("found", m.find());
		
		String body2 = RuleElementEnum.VirusBlock_Body_Match_2.getTargetText();
		Pattern body2Pattern = Pattern.compile(body2, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = body2Pattern.matcher("Incident Information: virus ");
		assertTrue("found", m.find());
		
		String subj1Regex = RuleElementEnum.VIRUS_BLOCK_1.getTargetText();
		Pattern subj1Pattern = Pattern.compile(subj1Regex, Pattern.CASE_INSENSITIVE);
		m = subj1Pattern.matcher("McAfee GroupShield Alert message you sent");
		assertTrue("found", m.find());
		
		String subj2Regex = RuleElementEnum.VIRUS_BLOCK_2.getTargetText();
		Pattern subj2Pattern = Pattern.compile(subj2Regex, Pattern.CASE_INSENSITIVE);
		m = subj2Pattern.matcher("EMAIL REJECTED");
		assertTrue("found", m.find());
	}
}
