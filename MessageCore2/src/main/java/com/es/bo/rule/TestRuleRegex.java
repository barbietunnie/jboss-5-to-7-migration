package com.es.bo.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.es.data.preload.RuleElementEnum;

public class TestRuleRegex {
		static final String LF = System.getProperty("line.separator", "\n");
		public static void main(String[] args) {
			try {
				TestRuleRegex test = new TestRuleRegex();
				test.challenge_response();
				test.postmaster();
				test.outOfOffice();
				test.auto_reply();
				test.deliveryFailure();
				test.spamblocker();
				test.virusBlocker();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		void challenge_response() {
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
			System.out.print("challenge_response body matching? " + matcher.find());
			matcher.reset(bodyIpermitMail);
			System.out.print(", " + matcher.find());
			matcher.reset(bodyDevnull);
			System.out.print(", " + matcher.find());
			matcher.reset(bodyVanquish);
			System.out.print(", " + matcher.find());
			matcher.reset(bodyQurb);
			System.out.println(", " + matcher.find());
			
			String headerRegex = RuleElementEnum.CHALLENGE_RESPONSE_1.getTargetText();
			Pattern headerPattern = Pattern.compile(headerRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			matcher = headerPattern.matcher("challenge_resp@ipermitmail.com");
			System.out.print("challenge_response header matching? " + matcher.find());
			matcher.reset("spamhippo@cr.com");
			System.out.println(", " + matcher.find());
			
			String subjRegex = RuleElementEnum.CHALLENGE_RESPONSE_2.getTargetText();
			Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
			matcher = subjPattern.matcher("Your mail to peter@gradwell.com requires confirmation");
			System.out.print("challenge_response subject matching? " + matcher.find());
			matcher.reset("CR found as [Qurb 100]");
			System.out.println(", " + matcher.find());
			
			String fromRegex = RuleElementEnum.CHALLENGE_RESPONSE_3.getTargetText();
			Pattern fromPattern = Pattern.compile(fromRegex, Pattern.CASE_INSENSITIVE);
			matcher = fromPattern.matcher("Confirmation from Kenneth E. Slaughter " + "<confirm-tech=openbsd.org@spamguard.vanquish.com>");
			System.out.println("challenge_response from matching? " + matcher.find());
		}
		
		void postmaster() {
			String postmasterFrom = RuleElementEnum.HARD_BOUNCE_1.getTargetText();
			Pattern postPattern = Pattern.compile(postmasterFrom, Pattern.CASE_INSENSITIVE);
			Matcher m = postPattern.matcher("postmaster@abc.com");
			System.out.print("postmater matching: " + m.find());
			m.reset("administrator2@abc.com");
			System.out.print(", " + m.find());

			String mailerdaemonFrom = RuleElementEnum.HARD_BOUNCE_2.getTargetText();
			Pattern daemPattern = Pattern.compile(mailerdaemonFrom, Pattern.CASE_INSENSITIVE);
			m = daemPattern.matcher("mailer-daemon@abc.com");
			System.out.println(", " + m.find());
		}
		
		void outOfOffice() {
			String subjRegex = RuleElementEnum.OUF_OF_OFFICE_AUTO_REPLY_1.getTargetText();
			Pattern pattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
			Matcher m = pattern.matcher("Re: (away from the office)");
			System.out.print("Out of office matching: " + m.find());
			m.reset("out of the office auto reply");
			System.out.println(", " + m.find());
		}
		
		void auto_reply() {
			String fromRegex = RuleElementEnum.AUTO_REPLY_2.getTargetText();
			Pattern fromPattern = Pattern.compile(fromRegex);
			Matcher m = fromPattern.matcher("autoresponder@abc.com");
			System.out.print("Auto-reply matching: " + m.find());
			m.reset("autoresponse-ooo@abc.com");
			System.out.print(", " + m.find());
			
			String subjRegex = RuleElementEnum.AUTO_REPLY_1.getTargetText();
			Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
			m = subjPattern.matcher("Re: Exception Autoreply: from SAXK");
			System.out.print(", " + m.find());
			m.reset("Auto-Respond E-Mail from aaa");
			System.out.println(", " + m.find());
			
			String bodyRegex = RuleElementEnum.AUTO_REPLY_3.getTargetText();
			Pattern bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = bodyPattern.matcher("This is an autoresponder. I'll never see your message SAXK");
			System.out.print("Auto-reply matching: " + m.find());
			m.reset("I.ll be away from the office until next monday.");
			System.out.print(", " + m.find());
			m.reset("I.ll be on vacation until next monday.");
			System.out.print(", " + m.find());
			m.reset("I.m away until next monday and am unable to read your message.");
			System.out.print(", " + m.find());
			m.reset("I am currently out of the office.");
			System.out.println(", " + m.find());
		}
		
		void deliveryFailure() {
			String subjRegex = RuleElementEnum.HardBounce_Subj_Match_1.getTargetText();
			Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
			Matcher m = subjPattern.matcher("Returned mail: Error During Delivery");
			System.out.print("Delivery Failure matching: " + m.find());
			m.reset("Returned mail: User Unknown");
			System.out.print(", " + m.find());
			m.reset("Mail could not be delivered");
			System.out.print(", " + m.find());
			m.reset("Undelivered Mail Returned to Sender");
			System.out.print(", " + m.find());
			m.reset("Email Addressing Error (aaa)");
			System.out.print(", " + m.find());
			m.reset("Returned mail: Delivery Error...");
			System.out.println(", " + m.find());
			
			String bodyRegex = RuleElementEnum.HardBounce_Body_Match_1.getTargetText();
			Pattern bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = bodyPattern.matcher("You've reached a non-working address. Please check");
			System.out.print("Delivery Failure matching: " + m.find());
			m.reset("Delivery Status: 5.4.1 Please check");
			System.out.println(", " + m.find());
		}
		
		void spamblocker() {
			String fromRegex = RuleElementEnum.SPAM_BLOCK_3.getTargetText();
			Pattern fromPattern = Pattern.compile(fromRegex, Pattern.CASE_INSENSITIVE);
			Matcher m = fromPattern.matcher("surfcontrol_agent@abc.com");
			System.out.print("Spam blocker matching: " + m.find());

			String returnPathRegex = RuleElementEnum.SPAM_BLOCK_4.getTargetText();
			Pattern pathPattern = Pattern.compile(returnPathRegex, Pattern.CASE_INSENSITIVE);
			m = pathPattern.matcher("pleaseforward@abc.com");
			System.out.println(", " + m.find());
			
			String subjRegex = RuleElementEnum.SPAM_BLOCK_1.getTargetText();
			Pattern subjPattern = Pattern.compile(subjRegex, Pattern.CASE_INSENSITIVE);
			m = subjPattern.matcher("[MailServer Notification]Attachment Blocking Notification");
			System.out.print("Spam blocker matching: " + m.find());
			m.reset("GWAVA Sender Notification .RBL block2");
			System.out.println(", " + m.find());
		}
		
		void virusBlocker() {
			String virusFrom = "(?:virus|scanner|devnull)\\S*\\@";
			Pattern fromPattern = Pattern.compile(virusFrom, Pattern.CASE_INSENSITIVE);
			Matcher m = fromPattern.matcher("virus_blocker@abc.com");
			System.out.print("Virus blocker matching " + m.find());
			
			String body1 = RuleElementEnum.VirusBlock_Body_Match_1.getTargetText();
			Pattern body1Pattern = Pattern.compile(body1, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = body1Pattern.matcher("There host The attachment was quarantined rejected found in ");
			System.out.print(", " + m.find());
			
			String body2 = RuleElementEnum.VirusBlock_Body_Match_2.getTargetText();
			Pattern body2Pattern = Pattern.compile(body2, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = body2Pattern.matcher("Incident Information: virus ");
			System.out.print(", " + m.find());
			
			String subj1Regex = RuleElementEnum.VIRUS_BLOCK_1.getTargetText();
			Pattern subj1Pattern = Pattern.compile(subj1Regex, Pattern.CASE_INSENSITIVE);
			m = subj1Pattern.matcher("McAfee GroupShield Alert message you sent");
			System.out.print(", " + m.find());
			
			String subj2Regex = RuleElementEnum.VIRUS_BLOCK_2.getTargetText();
			Pattern subj2Pattern = Pattern.compile(subj2Regex, Pattern.CASE_INSENSITIVE);
			m = subj2Pattern.matcher("EMAIL REJECTED");
			System.out.print(", " + m.find());
		}
}
