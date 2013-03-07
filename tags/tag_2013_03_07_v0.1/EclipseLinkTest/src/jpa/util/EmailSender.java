package jpa.util;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author Jack Wang
 *
 * This is a common email sender that is used to send out email notifications.
 * 
 * This class requires an email.properties file in META-INF folder of your class path.
 * 
 * Sample contents of email.properties:
#
# %%%CHANGE ME%%%: Change the SenderId to your project name
#
SenderId=MyApp
#
HostName=emailsphere.com
HostIP=localhost
EmailDomain=espheredemo.com
#
# Default recipients for unhandled error.
#
RecipientId.DEV=developers
RecipientId.TEST=quality.control
RecipientId.UAT=user.acceptance
RecipientId.PROD=prod.support
#
RecipientIdForFatalError.DEV=developers
RecipientIdForFatalError.TEST=developers,quality.control
RecipientIdForFatalError.UAT=developers
RecipientIdForFatalError.PROD=developers,prod.support
#
RecipientIdForDevelopers.DEV=developers
RecipientIdForDevelopers.TEST=developers
RecipientIdForDevelopers.UAT=developers
RecipientIdForDevelopers.PROD=developers
#
# disable email notification: yes/no
disable=no
#
 */
public class EmailSender {
	private static Logger logger = Logger.getLogger(EmailSender.class);
	static boolean isDebugEnabled = logger.isDebugEnabled();

	private static String fileName = "META-INF/email.properties";
	private static Properties emailProps = null;
	private static String hostName = null;
	
	public enum EmailList {
		ToUnhandled,
		ToFatalError,
		ToDevelopers
	}
	
	private EmailSender() {
	}

	private static void getEmailProperties() throws EmailSenderException {
		if (emailProps == null) {
			logger.info("email properties file name: " + fileName);
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			URL url = loader.getResource(fileName);
			if (url == null) {
				throw new EmailSenderException("Could not find " + fileName + " file.");
			}
			logger.info("loading email properties file from: " + url.getPath());
			emailProps = new Properties();
			try {
				InputStream is = url.openStream();
				emailProps.load(is);
			}
			catch (IOException e) {
				throw new EmailSenderException("IOException caught", e);
			}
			logger.info(fileName + ": " + emailProps);
			hostNames.add(emailProps.getProperty("HostName"));
			String hostIP = emailProps.getProperty("HostIP");
			if (StringUtils.isNotBlank(hostIP)) {
				hostNames.add(hostIP);
			}
			hostName = (String) hostNames.get(0);
		}
	}

	private static Session getMailSession() {
		String mailJndi = "mail/Mailer"; //"java:comp/env/mail/Mailer";
		Session mailSession = null;
		//get JNDI configuration mail session from the was server
		try {
			InitialContext ctx = new InitialContext();
   			mailSession = (Session) ctx.lookup(mailJndi);
		}
		catch (NamingException ne) {
			logger.warn("Could not find JNDI entry: " + mailJndi + ". " + ne.getMessage());
			//If there is no JNDI configuration, 
			//then get mail session through convention way
			Properties props = System.getProperties();
			props.put("mail.smtp.host", hostName);
			mailSession = Session.getInstance(props, null);
		}
		return mailSession;
	}

	/**
	 * Send an email notification when unhandled error was raised.
	 * Email subject line is constructed from email.properties and input
	 * parameter "region", <SenderId> Application - Error, <region>
	 * for example: Emailsphere Application - Error, TEST
	 * 
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - attachment, optional.
	 * @param region
	 *            - the region the listener is running in
	 * @return true if email is sent successfully
	 * @throws EmailSenderException
	 */
	public static boolean sendEmailUnhandled(String body, String attachment, String region)
		throws EmailSenderException {
		String recipientId = null;
		String ccAddress = null;
		return sendEmail(null, body, attachment, EmailList.ToUnhandled, region,
				recipientId, ccAddress);
	}

	/**
	 * A send email method that sends email notifications to a specified
	 * recipient.
	 * 
	 * @param subject
	 *            - message subject.
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - message attachment, optional.
	 * @param region
	 *            - the region the application is running in.
	 * @param recipientId
	 *            - use this recipient id if it is valued.
	 * @return true if email is sent successfully
	 * @throws EmailSenderException
	 */
	public static boolean sendEmail(
		String subject,
		String body,
		String attachment,
		String region,
		String recipientId,
		String ccAddress)
		throws EmailSenderException {
		return sendEmail(subject, body, attachment, EmailList.ToUnhandled,
				region, recipientId, ccAddress);
	}

	/**
	 * A send mail method that sends email notifications for both unhandled and
	 * fatal errors.
	 * 
	 * @param subject
	 *            - message subject, ignored if recipient is
	 *            EmailList.ToUnhandled.
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - message attachment, optional.
	 * @param emailList
	 *            - message recipients.
	 * @param region
	 *            - the region the listener is running in.
	 * @return true if email is sent successfully
	 * @throws EmailSenderException
	 */
	public static boolean sendEmail(
		String subject,
		String body,
		String attachment,
		EmailList emailList,
		String region,
		String ccAddress)
		throws EmailSenderException {
		String recipientId = null;
		return sendEmail(subject, body, attachment, emailList, region,
				recipientId, ccAddress);
	}

	private static List<String> hostNames = new ArrayList<String>();
	private static int currHostIdx = 0;
	
	/**
	 * A send mail method that sends email notifications when an unhandled error
	 * or a fatal error was raised from application.
	 * 
	 * @param subject
	 *            - message subject, ignored if recipient is
	 *            EmailList.ToUnhandled.
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - message attachment, optional.
	 * @param emailList
	 *            - message recipients.
	 * @param region
	 *            - the region the listener is running in.
	 * @param recipientId
	 *            - use this recipient id if it is valued.
	 * @param ccAddress
	 *            - carbon copy to this address if it is valued.
	 * @return true if email is sent successfully
	 * @throws EmailSenderException
	 */
	public static boolean sendEmail(
		String subject,
		String body,
		String attachment,
		EmailList emailList,
		String region,
		String recipientId,
		String ccAddress)
		throws EmailSenderException {
		// read email.properties
		getEmailProperties();
		if ("yes".equalsIgnoreCase(emailProps.getProperty("disable"))) {
			logger.info("sendEmail() - Email notification disabled in " + fileName);
			return false;
		}
		try {
			sendEmail(hostName, subject, body, attachment, emailList, region,
					recipientId, ccAddress);
			return true;
		}
		catch (AddressException e) {
			logger.error("Invalid Email address found: ", e);
			return false;
		}
		catch (MessagingException e1) {
			if ((e1.toString().indexOf("Could not connect to SMTP host") >= 0 
					|| e1.toString().indexOf("Unknown SMTP host") >= 0)
					&& (++currHostIdx < hostNames.size())) {
				logger.error("Failed to send email via " + hostName + ", " + e1);
				hostName = (String) hostNames.get(currHostIdx);
				logger.error("Try next SMTP server " + hostName + " ...");
				return sendEmail(subject, body, attachment, emailList, region,
						recipientId, ccAddress);
			}
			else {
				throw new EmailSenderException("Failed to send email via " + hostName, e1);
			}
		}
	}

	private static void sendEmail(
		String hostName,
		String _subject,
		String body,
		String attachment,
		EmailList emailList,
		String _region,
		String _recipientId,
		String _ccAddress)
		throws AddressException, MessagingException {
		// get "region" from properties file, for backward compatibility
		String region = emailProps.getProperty("Region");
		if (StringUtils.isNotBlank(_region)) {
			region = _region;
		}
		String senderId = emailProps.getProperty("SenderId");
		// get recipient from properties file
		String recipientId = emailProps.getProperty("RecipientId." + region);
		if (EmailList.ToFatalError.equals(emailList)) {
			recipientId = emailProps.getProperty("RecipientIdForFatalError." + region);
		}
		else if (EmailList.ToDevelopers.equals(emailList)) {
			recipientId = emailProps.getProperty("RecipientIdForDevelopers." + region);
		}
		if (StringUtils.isNotBlank(_recipientId)) {
			// send to the recipient from input parameter
			recipientId = _recipientId;
		}
		if (StringUtils.isBlank(recipientId)) {
			logger.warn("sendEmail() - Email recipient is not provided, quit.");
			return;
		}
		
		String emailDomain = emailProps.getProperty("EmailDomain","emailsphere.com");
		
		logger.info("EMail host name: " + hostName + ", Region: " + region);

		// create some properties and get the default Session
		//Properties props = System.getProperties();
		//props.put("mail.smtp.host", hostName);
		//Session session = Session.getInstance(props, null);
		Session session = getMailSession();

		// create a message
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(appendDomain(senderId, emailDomain)));
		String recipientAddrs = appendDomain(recipientId, emailDomain);
		InternetAddress[] address = InternetAddress.parse(recipientAddrs, false);
		msg.setRecipients(Message.RecipientType.TO, address);
		if (StringUtils.isNotBlank(_ccAddress)) {
			InternetAddress[] cc = InternetAddress.parse(appendDomain(_ccAddress, emailDomain), false);
			msg.setRecipients(Message.RecipientType.CC, cc);
		}
		if (StringUtils.isNotBlank(_subject)) {
			String subj = StringUtils.replaceOnce(_subject, "{0}", region);
			msg.setSubject(subj);
		}
		else {
			msg.setSubject(senderId + " Application - Error, " + region);
		}
		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		String bodyStr = StringUtils.replaceOnce(body, "{0}", region);
		mbp1.setText(bodyStr);

		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);

		if (StringUtils.isNotBlank(attachment)) {
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			mbp2.setText(attachment, "us-ascii");
			mp.addBodyPart(mbp2);
		}

		// add the Multipart to the message
		msg.setContent(mp);

		// set the Date: header
		msg.setSentDate(new Date());

		// send the message
		Transport.send(msg);

		logger.info("Email notification sent to: " + recipientAddrs);
	}

	public static void send(String from, String to, String subject, String body)
			throws MessagingException, EmailSenderException {
		if (StringUtil.isEmpty(to)) {
			throw new MessagingException("Input TO address is blank.");
		}
		if (StringUtil.isEmpty(subject)) {
			throw new MessagingException("Input Subject is blank.");
		}
		getEmailProperties();
		// Get a Session object
		Session session = getMailSession();
		// construct a MimeMessage
		Message msg = new MimeMessage(session);
		Address[] addrs = InternetAddress.parse(from, false);
		if (addrs != null && addrs.length > 0) {
			msg.setFrom(addrs[0]);
		}
		else {
			msg.setFrom();
		}
		msg.setRecipients(RecipientType.TO, InternetAddress.parse(to, false));
		msg.setSubject(subject);
		msg.setText(body);
		msg.setSentDate(new Date());
		// could also use Session.getTransport() and Transport.connect()
		// send the thing off
		Transport.send(msg);
		if (isDebugEnabled) {
			logger.debug("Mail from " + from + " - " + subject
					+ " was sent to: " + to);
		}
	}

	private static String appendDomain(String addrs, String emailDomain) {
		StringTokenizer st = new StringTokenizer(addrs, ",");
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.indexOf("@") < 0) {
				token += "@" + emailDomain;
			}
			if (sb.length() > 0) sb.append(",");
			sb.append(token);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		fileName = "META-INF/email_sample.properties";
		try {
			EmailSender.sendEmail("Test from EmailSender",
					"EmailSender...\ntest message", "attachment text",
					EmailList.ToUnhandled, null, "jack.k.wang");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
