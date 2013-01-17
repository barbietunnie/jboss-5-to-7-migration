package com.legacytojava.jbatch;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.common.EmailSender;
import com.legacytojava.jbatch.common.SimpleEmailVo;
import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.client.ClientDao;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;

/**
 * EventAlert class. It handles all event alerts from batch application.
 */
public class EventAlert implements java.io.Serializable {
	private static final long serialVersionUID = -1532509663419472555L;
	static final Logger logger = Logger.getLogger(EventAlert.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final String LF = System.getProperty("line.separator", "\n");
	final Hashtable<Integer, Set<String>> eventsSent = new Hashtable<Integer, Set<String>>();
	final Properties eventProps;
	static boolean silent = false;
	static boolean debug = true;
	
	private EmailSender emailSender;
	private String disabled;
	
	private ClientDao clientDao = null;

	/**
	 * create a EventAlert instance with event properties
	 * 
	 * @param _eventProps -
	 *            event properties
	 */
	public EventAlert(Properties _eventProps) {
		logger.info("Entering Constructor...");
		eventProps = _eventProps;
	}

	/**
	 * issue excep alert
	 * 
	 * @param server -
	 *            server number
	 * @param appMsg -
	 *            application provided message
	 */
	public void issueInfoAlert(int server, String appMsg) {
		String eventKey = "info_event";
		issueAlert(server, eventKey, appMsg);
	}

	/**
	 * issue clear alert
	 * 
	 * @param server -
	 *            the server number this event tied to
	 */
	void issueClearAlert(int server) {
		String appMsg = getAppName() + ": Exception cleared, jbatch restarted.";
		java.util.Enumeration<Integer> keys = eventsSent.keys();
		// loop through the hash table to issue clearing alerts for the server
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (server == ((Integer) key).intValue()) {
				Set<String> alerts = eventsSent.get(key);
				Iterator<?> it = alerts.iterator();
				while (it.hasNext()) {
					String eventKey = (String) it.next();
					issueAlert(JbMain.CLEAR_ALERT, eventKey, appMsg);
				}
				eventsSent.remove(key);
			}
		}
	}

	/**
	 * issue excep alert
	 * 
	 * @param server -
	 *            server number
	 */
	void issueExcepAlert(int server) {
		String eventKey = "excep_event";
		String appMsg = getAppName() + ": Exception caught, please check error log";
		issueAlert(server, eventKey, appMsg);
	}

	/**
	 * issue excep alert
	 * 
	 * @param server -
	 *            server number
	 * @param appMsg -
	 *            application provided message
	 */
	public void issueExcepAlert(int server, String appMsg) {
		String eventKey = "excep_event";
		issueAlert(server, eventKey, appMsg);
	}

	/**
	 * issue excep alert
	 * 
	 * @param server -
	 *            server number
	 * @param excep -
	 *            Exception received by application
	 */
	public void issueExcepAlert(int server, Exception excep) {
		issueExcepAlert(server, null, excep);
	}

	/**
	 * issue excep alert, derive event key from exception type
	 * 
	 * @param server -
	 *            server number
	 * @param appMsg -
	 *            application provided message
	 * @param excep -
	 *            Exception received by application
	 */
	public void issueExcepAlert(int server, String appMsg, Exception excep) {
		String eventKey = null;

		if (excep instanceof javax.jms.JMSException)
			eventKey = "jms_excep_event";
		else if (excep instanceof SmtpException)
			eventKey = "smtp_excep_event";
		else if (excep instanceof javax.mail.MessagingException)
			eventKey = "jmail_excep_event";
		else if (excep instanceof java.sql.SQLException)
			eventKey = "sql_excep_event";
		else
			eventKey = "excep_event";

		if (appMsg == null || appMsg.trim().length() == 0) {
			appMsg = getAppName() + ": Exception caught, please check error log";
		}

		issueAlert(server, eventKey, appMsg);
	}

	/**
	 * issue default fatal alert
	 * 
	 * @param server -
	 *            server number
	 */
	void issueFatalAlert(int server) {
		String eventKey = "fatal_event";
		String appMsg = getAppName() + ": Fatal Exception caught, please check error log";
		issueAlert(server, eventKey, appMsg);
	}

	/**
	 * issue default fatal alert with provided message text
	 * 
	 * @param server -
	 *            server number
	 * @param appMsg -
	 *            application provided message
	 */
	void issueFatalAlert(int server, String appMsg) {
		String eventKey = "fatal_event";
		issueAlert(server, eventKey, appMsg);
	}
	
	/**
	 * issue default fatal alert
	 * 
	 * @param server -
	 *            server number
	 * @param excep -
	 *            Exception received by application
	 */
	void issueFatalAlert(int server, Exception excep) {
		issueFatalAlert(server, null, excep);
	}
	
	/**
	 * issue fatal alert, derive event key from exception type
	 * 
	 * @param server -
	 *            server number
	 * @param appMsg -
	 *            application provided message
	 * @param excep -
	 *            Exception received by application
	 */
	public void issueFatalAlert(int server, String appMsg, Exception excep) {
		String eventKey = null;

		if (excep instanceof javax.jms.JMSException)
			eventKey = "jms_fatal_event";
		else if (excep instanceof SmtpException)
			eventKey = "smtp_fatal_event";
		else if (excep instanceof javax.mail.MessagingException)
			eventKey = "jmail_fatal_event";
		else if (excep instanceof java.sql.SQLException)
			eventKey = "sql_fatal_event";
		else
			eventKey = "fatal_event";

		if (appMsg == null || appMsg.trim().length() == 0) {
			appMsg = getAppName() + ": Fatal Exception caught, please check error log";
		}

		issueAlert(server, eventKey, appMsg);
	}

	/**
	 * issue alert using provided event key and message text. The recipient's
	 * address is retrieved from event Properties using event key, if it
	 * returned no value, the Security Email address from Client table is then
	 * used. If the recipient's email address has a local domain name like
	 * "localhost", it is replaced with a real domain name from Client table.
	 * 
	 * 
	 * @param server -
	 *            server number
	 * @param eventKey -
	 *            event key
	 * @param appMsg -
	 *            application provided message
	 */
	public void issueAlert(int server, String eventKey, String appMsg) {
		if (silent) {
			logger.warn("Silent Flag is on - msg=" + appMsg + ", eventKey=" + eventKey);
			return;
		}
		if (StringUtil.isEmpty(appMsg)) {
			appMsg = "Error: Message Text was no provided by Application.";
		}
		String origToAddrs = eventProps.getProperty(eventKey);// from event Properties
		String hostName = HostUtil.getHostName(); // just in case
		ClientVo clientVo = ClientUtil.getDefaultClientVo();
		if (StringUtil.isEmpty(origToAddrs)) {
			origToAddrs = clientVo.getSecurityEmail();
		}
		hostName = clientVo.getDomainName();
		origToAddrs = (origToAddrs == null ? "" : origToAddrs); // just for safety
		StringTokenizer st = new StringTokenizer(origToAddrs, ",");
		String toAddr = st.nextToken();
		// replace email address domain with domain name from Client table
		toAddr = replaceDomainName(toAddr, hostName);
		while (st.hasMoreTokens()) {
			String _toAddr = st.nextToken();
			toAddr += "," + replaceDomainName(_toAddr, hostName);
		}
		String from = replaceDomainName("event.alert@localhost", hostName);
		try {
			SimpleEmailVo vo = new SimpleEmailVo();
			vo.setFromAddr(from);
			vo.setToAddr(toAddr);
			vo.setMsgSubject("A " + eventKey + " occured");
			vo.setMsgBody(new Date()+ LF + appMsg);
			if ("yes".equalsIgnoreCase(disabled)) {
				logger.warn("Alert is disabled and not sent. It was intended to : " + toAddr
						+ " - msg=" + appMsg + ", key=" + eventKey);
			}
			else {
				emailSender.send(vo);
				logger.info("Alert sent to " + toAddr + " - msg=" + appMsg + ", key=" + eventKey);
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during issueAlert()", e);
		}
		// save the alert into the hash-table keyed by server
		// the hash-table is used later on for clearing the alert
		if (server != JbMain.CLEAR_ALERT) {
			if (eventsSent.get(Integer.valueOf(server)) == null) {
				Set<String> alerts = Collections.synchronizedSet(new HashSet<String>());
				// Set alerts = new HashSet();
				alerts.add(eventKey);
				eventsSent.put(Integer.valueOf(server), alerts);
			}
			else {
				Set<String> alerts = eventsSent.get(Integer.valueOf(server));
				alerts.add(eventKey);
			}
		}
	}

	private static String replaceDomainName(String addr, String hostName) {
		if (StringUtil.isEmpty(addr)) return addr;
		// replace if domain name is blank or is "localhost"
		int atPos;
		if ((atPos = addr.lastIndexOf("@")) < 0) { // local address (without the right part)
			int rightPos;
			if ((rightPos = addr.lastIndexOf(">")) > 0) { // address looks like <user>
				addr = addr.substring(0, rightPos) + "@" + hostName + ">";
			}
			else {
				addr += "@" + hostName;
			}
		}
		else { // remote address (with the right part)
			String domainName = EmailAddrUtil.getEmailDomainName(addr);
			if ("localhost".equalsIgnoreCase(domainName) || domainName == null) {
				if (addr.lastIndexOf(">") > 0) { // address looks like <user@localhost>
					addr = addr.substring(0, atPos) + "@" + hostName + ">";
				}
				else {
					addr = addr.substring(0, atPos) + "@" + hostName;
				}
			}
		}
		return addr;
	}
	
	/**
	 * retrieve application name from application properties
	 */
	private String getAppName() {
		return JbMain.appConf.getProperty("app_name", "jbatch");
	}

	public EmailSender getEmailSender() {
		return emailSender;
	}

	public void setEmailSender(EmailSender emailSender) {
		this.emailSender = emailSender;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao) JbMain.getBatchAppContext().getBean("clientDao");
		}
		return clientDao;
	}

	public void setClientDao(ClientDao clientDao) {
		this.clientDao = clientDao;
	}
	
	public static void main(String[] args) {
		System.out.println(EmailAddrUtil.getDisplayName("  abc@abc.com"));
		System.out.println(EmailAddrUtil.getDisplayName("\"my name\" <abc@abc.com>"));
		System.out.println(EmailAddrUtil.getEmailDomainName("alert@localhost"));
		System.out.println(EmailAddrUtil.getEmailDomainName("\"my name\" <abc@abc.com>"));
		
		System.out.println(replaceDomainName("alert@localhost", Constants.VENDER_DOMAIN_NAME));
	}
}