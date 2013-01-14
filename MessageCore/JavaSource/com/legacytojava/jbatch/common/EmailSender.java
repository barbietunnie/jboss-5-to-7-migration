package com.legacytojava.jbatch.common;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.legacytojava.message.constant.MailProtocol;
import com.legacytojava.message.constant.XHeaderName;
import com.legacytojava.message.util.StringUtil;

/**
 * A simple email sender that sends email off via SMTP server.
 */
public class EmailSender implements java.io.Serializable {
	private static final long serialVersionUID = -1906190523204288240L;
	protected static final Logger logger = Logger.getLogger(EmailSender.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final Properties smtpProps;
	final String smtphost;
	final String smtpport;
	final String mailer = "EmailSender";
	final String protocol;
	final String host;
	final String user;
	final String password;
	
	private boolean debug = false;
	String LF = System.getProperty("line.separator","\n");
	
    /**
	 * Constructor, with SMTP server properties as its parameter.
	 * 
	 * @param smtpProps -
	 *            SMTP properties
	 */
	public EmailSender(Properties smtpProps) {
		this.smtpProps = smtpProps;
		smtphost = this.smtpProps.getProperty("smtphost", "localhost");
		smtpport = (String) this.smtpProps.getProperty("smtpport", "25");
		protocol = this.smtpProps.getProperty("protocol", MailProtocol.POP3);
		user = this.smtpProps.getProperty("user");
		password = this.smtpProps.getProperty("password");
		host = this.smtpProps.getProperty("host", smtphost);
	}
	
	/**
	 * This method accepts constructs a message from input fields and sends it
	 * off via SMTP server.
	 * 
	 * @param from -
	 *            from address
	 * @param to -
	 *            to address
	 * @param subject -
	 *            message subject
	 * @param body -
	 *            message body
	 * @throws MessagingException
	 */
	public void send(String from, String to, String subject, String body) throws MessagingException {
		if (StringUtil.isEmpty(to)) {
			throw new MessagingException("Input TO address is blank.");
		}
		if (StringUtil.isEmpty(subject)) {
			throw new MessagingException("Input Subject is blank.");
		}
		SimpleEmailVo vo = new SimpleEmailVo();
		vo.setFromAddr(from);
		vo.setToAddr(to);
		vo.setMsgSubject(subject);
		vo.setMsgBody(body);
		send(vo);
	}
	
	/**
	 * This method constructs a message from the SimpleEmailVo and sends it off
	 * via SMTP server.
	 * 
	 * @param m -
	 *            SimpleEmailVo contains email properties.
	 * @throws MessagingException
	 */
	public void send(SimpleEmailVo m) throws MessagingException {
		if (isDebugEnabled)
			logger.debug("Entering send method");

		Properties props = System.getProperties();
		if (smtphost != null) {
			props.put("mail.smtp.host", smtphost);
		}
		// Get a Session object
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);
		// construct a MimeMessage
		Message msg = new MimeMessage(session);
		Address[] addrs = InternetAddress.parse(m.getFromAddr(), false);
		if (addrs != null && addrs.length > 0) {
			msg.setFrom(addrs[0]);
		}
		else {
			msg.setFrom();
		}
		msg.setRecipients(RecipientType.TO, InternetAddress.parse(m.getToAddr(), false));
		if (m.getCcAddr() != null) {
			msg.setRecipients(RecipientType.CC, InternetAddress.parse(m.getCcAddr(), false));
		}
		if (m.getBccAddr() != null) {
			msg.setRecipients(RecipientType.BCC, InternetAddress.parse(m.getBccAddr(), false));
		}
		msg.setSubject(m.getMsgSubject());
		msg.setText(m.getMsgBody());
		if (m.getXMailer() != null) {
			msg.setHeader(XHeaderName.XHEADER_MAILER, m.getXMailer());
		}
		msg.setSentDate(new Date());
		// could also use Session.getTransport() and Transport.connect()
		// send the thing off
		Transport.send(msg);
		if (isDebugEnabled) {
			logger.debug("Mail from " + m.getFromAddr() + " - " + m.getMsgSubject()
					+ " was sent to: " + m.getToAddr());
		}
		// IMAP only
		String url = null;
		// Keep a copy, if requested. Works with IMAP only.
		keepACopy(url, session, msg, m);
	}
	
	void keepACopy(String url, Session session, Message msg, SimpleEmailVo m) throws MessagingException {
		if (protocol.equalsIgnoreCase(MailProtocol.IMAP) && m.getSaveToFolder() != null) {
			// Get a Store object
			Store store = null;
			if (url != null) {
				URLName urln = new URLName(url);
				store = session.getStore(urln);
				store.connect();
			}
			else {
				if (protocol != null) {
					store = session.getStore(protocol);
				}
				else {
					store = session.getStore();
				}
				// Connect
				if (host != null) {
					store.connect(host, user, password);
				}
				else if (user != null && password != null) {
					store.connect(user, password);
				}
				else {
					store.connect();
				}
			}
			// Get record Folder.  Create if it does not exist.
			Folder folder = store.getFolder(m.getSaveToFolder());
			if (folder == null) {
				logger.error("Can't get record folder.");
			}
			if (!folder.exists()) {
				folder.create(Folder.HOLDS_MESSAGES);
			}
			Message[] msgs = new Message[1];
			msgs[0] = msg;
			folder.appendMessages(msgs);
			if (isDebugEnabled) {
				logger.debug("Mail from: " + m.getFromAddr() + " - " + m.getMsgSubject() + ", to: "
						+ m.getToAddr() + " was recorded successfully.");
			}
		}
	}
}
