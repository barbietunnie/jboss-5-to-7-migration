package com.legacytojava.message.dao.idtokens;

import static com.legacytojava.message.constant.Constants.DEFAULT_CLIENTID;
import static com.legacytojava.message.constant.Constants.VERP_BOUNCE_EMAILID_XHEADER;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.constant.EmailIDToken;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.IdTokensVo;

/**
 * search the input string for possible embedded message id.
 */
public final class EmailIdParser implements Serializable {
	private static final long serialVersionUID = 8659745554700580366L;
	static final Logger logger = Logger.getLogger(EmailIdParser.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final IdTokensVo idTokensVo;
	
	// all patterns are defined with groups like: (A(B)C)
	private final Pattern bodyPattern;
	private final Pattern bodyHeaderPattern;
	private final Pattern headerPattern;
	
	private static EmailIdParser emailIdParser = null;

	private EmailIdParser(IdTokensVo vo) {
		if (vo != null) { // not used
			this.idTokensVo = vo;
		}
		else { // use default values from Constants class
			this.idTokensVo = new IdTokensVo();
			this.idTokensVo.setClientId(DEFAULT_CLIENTID);
			this.idTokensVo.setBodyBeginToken(EmailIDToken.BODY_BEGIN);
			this.idTokensVo.setBodyEndToken(EmailIDToken.BODY_END);
			this.idTokensVo.setXhdrBeginToken(EmailIDToken.XHDR_BEGIN);
			this.idTokensVo.setXhdrEndToken(EmailIDToken.XHDR_END);
			this.idTokensVo.setXHeaderName(EmailIDToken.XHEADER_NAME);
			this.idTokensVo.setMaxLength(EmailIDToken.MAXIMUM_LENGTH);
		}
		/*
		 * Some Email servers may insert CR/LF and tabs into Email_Id section
		 * (MS exchange server for one). Further research revealed that MS
		 * exchange server inserted "\r\n\t" into the Email_ID string, and it
		 * caused the parser to fail. The work around is to match spaces with
		 * one or more white spaces(\\s+).
		 */
		// body regular expression
		String bodyBegin = StringUtil.replaceAll(getClientId() + " " + getBodyBegin(), " ", "\\s+");
		bodyBegin = StringUtil.replaceAll(bodyBegin, ".", "\\.");
		String bodyEnd = StringUtil.replaceAll(getBodyEnd(), " ", "\\s+");
		bodyEnd = StringUtil.replaceAll(bodyEnd, ".", "\\.");
		String bodyRegex = "(" + bodyBegin + "(\\d{1," + idTokensVo.getMaxLength() + "})"
				+ bodyEnd + ")";
		bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		// end of body
		// body regular expression for x-header
		String bodyHdrBegin = StringUtil.replaceAll(getBodyXhdrBegin(), " ", "\\s+");
		bodyHdrBegin = StringUtil.replaceAll(bodyHdrBegin, ".", "\\.");
		String bodyHdrEnd = StringUtil.replaceAll(getXhdrEnd(), " ", "\\s+");
		bodyHdrEnd = StringUtil.replaceAll(bodyHdrEnd, ".", "\\.");
		String bodyHdrRegex = "(" + bodyHdrBegin + "(\\d{1," + idTokensVo.getMaxLength()
				+ "})" + bodyHdrEnd + ")";
		bodyHeaderPattern = Pattern.compile(bodyHdrRegex, Pattern.DOTALL);
		// end of body for x-header
		// x-header regular expression
		String headerBegin = StringUtil.replaceAll(getXhdrBegin(), " ", "\\s+");
		headerBegin = StringUtil.replaceAll(headerBegin, ".", "\\.");
		String headerEnd = StringUtil.replaceAll(getXhdrEnd(), " ", "\\s+");
		headerEnd = StringUtil.replaceAll(headerEnd, ".", "\\.");
		String headerRegex = "\\s*(" + headerBegin + "(\\d{1," + idTokensVo.getMaxLength() + "})"
				+ headerEnd + ")\\s*";
		headerPattern = Pattern.compile(headerRegex, Pattern.DOTALL);
		// end of x-header
		if (isDebugEnabled) {
			logger.debug("Regex for body 1: " + bodyRegex);
			logger.debug("Regex for body 2: " + bodyHdrRegex);
			logger.debug("Regex for header: " + headerRegex);
		}
	}
	
	public static EmailIdParser getDefaultParser() {
		if (emailIdParser == null) {
			emailIdParser = new EmailIdParser(null);
		}
		return emailIdParser;
	}

/*
	public static EmailIdParser getParser(IdTokensVo tokensVo) {
		EmailIdParser parser = new EmailIdParser(tokensVo);
		return parser;
	}
*/
	/**
	 * Search message text for embedded email id section, and return the
	 * Email_Id code.
	 * 
	 * @param msgStr -
	 *            message text
	 * @return Email_Id code or null if not found
	 * @throws NumberFormatException
	 *             if the Email_Id format is invalid
	 */
	public String parseMsg(String msgStr) throws NumberFormatException {
		if (msgStr == null || msgStr.length() == 0) {
			return null;
		}
		// Search Email Id from body. example: Email Id: 10.1234567890.0
		String keyStr = getKey(msgStr, bodyPattern);
		if (keyStr == null) {
			// Search Email Id from X-Header string: X-Email_Id: 10.300262286930.0
			// as X-Headers could be included in message body from a bounced email.
			keyStr = getKey(msgStr, bodyHeaderPattern);			
		}
		if (keyStr != null) {
			if (isDebugEnabled)
				logger.debug("parseMsg() - Encoded Email_Id found from body: " + keyStr);
			return MsgIdCipher.decode(keyStr) + "";
		}
		return null;
	}
	
	/**
	 * find Email_Id section from X-Headers and extract the embedded email id
	 * code.
	 * 
	 * @param headers -
	 *            MsgHeader list
	 * @return email id code
	 * @throws NumberFormatException
	 *             if the Email_Id format is invalid
	 */
	public String parseHeaders(List<MsgHeader> headers) throws NumberFormatException {
		for (int i = 0; headers != null && i < headers.size(); i++) {
			MsgHeader header = headers.get(i);
			String keyStr = null;
			if (VERP_BOUNCE_EMAILID_XHEADER.equals(header.getName())) {
				keyStr = parseXHeader(header.getValue());
			}
			else if (getEmailIdXHdrName().equals(header.getName())) {
				keyStr = parseXHeader(header.getValue());
			}
			if (keyStr != null) {
				if (isDebugEnabled) {
					logger.debug("parseHeaders() - Encoded Email_Id found from X-Header: "
							+ keyStr);
				}
				return MsgIdCipher.decode(keyStr) + "";
			}
		}
		return null;
	}

	/**
	 * extract the embedded email id from X-Header
	 * 
	 * @param xhdrStr
	 *            X-Header text
	 * @return Email_Id
	 * @throws NumberFormatException
	 *             if the Email_Id format is invalid
	 */
	public String parseXHeader(String xhdrStr) throws NumberFormatException {
		if (xhdrStr == null || xhdrStr.length() == 0) {
			return null;
		}
		// look for Email_Id from X-Header. example: 10.1234567890.0
		String keyStr = getKey(xhdrStr, headerPattern);
		return keyStr;
	}

	/**
	 * search message body for an embedded EmailId section
	 * 
	 * @param body -
	 *            message body
	 * @return true if an EmailId is found
	 */
	public boolean isEmailIdExist(String body) {
		if (body == null || body.trim().length() == 0) {
			return false;
		}
		// search embedded EmailId. example: Email Id: 10.1234567890.0
		Matcher matcher = bodyPattern.matcher(body);
		return matcher.find();
	}
	
	/**
	 * Replace the embedded email id with a new one. If a X-Header Email_Id
	 * section also found in the message text, remove it first.
	 * 
	 * @param msgStr
	 *            message text
	 * @param msgId
	 *            new message id
	 * @return message text with a new email_id
	 */
	public String replaceEmailId(String msgStr, long msgId) {
		if (msgStr == null || msgStr.trim().length() == 0) {
			return msgStr;
		}
		// first remove body X-Header: X-Email_Id: 10.3002622869.0
		String msgStr1 = removeKey(msgStr, bodyHeaderPattern);
		
		// second replace embedded: Email Id: 10.1234567890.0
		String msgStr2 = replaceKey(msgStr1, bodyPattern, msgId);
		
		return msgStr2;
	}

	/*
	 * retrieve email id code (still encoded) from message text using provided
	 * Pattern.
	 */
	private String getKey(String msg, Pattern pattern) {
		String key = null;
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			key = matcher.group(matcher.groupCount());
		}
		return key;
	}

	String displayAllKeys(String msg, Pattern pattern) {
		String key = null;
		Matcher matcher = pattern.matcher(msg);
		int index = 0;
		while (matcher.find(index)) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				logger.info(i + " - " + matcher.group(i));
			}
			key = matcher.group(matcher.groupCount());
			index = matcher.end();
		}
		return key;
	}
	
	/*
	 * remove Email_Id section, for example: X-Email_Id: 10.3002622869.0
	 */
	private String removeKey(String msg, Pattern pattern) {
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			int group = matcher.groupCount() >= 1 ? 1 : matcher.groupCount();
			msg = msg.substring(0, matcher.start(group)) + msg.substring(matcher.end(group));
		}
		return msg;
	}

	/*
	 * Find an Email_Id section in the message text, and replace the Email_Id
	 * code with a new one.
	 */
	private String replaceKey(String msg, Pattern pattern, long msgId) {
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			//msg = matcher.replaceFirst(MsgIdCipher.encode(msgId)); // not working with group
			msg = msg.substring(0, matcher.start(matcher.groupCount())) + MsgIdCipher.encode(msgId)
					+ msg.substring(matcher.end(matcher.groupCount()));
		}
		return msg;
	}

	/**
	 * create an email id section for message body, using the provided msgId as
	 * the source of Email_Id code.
	 * 
	 * @param msgId
	 *            email_id to be wrapped
	 * @return wrapped email_id
	 */
	public String wrapupEmailId(Long msgId) throws NumberFormatException {
		if (msgId == null) {
			return null;
		}
		else {
			return getClientId() + " " + getBodyBegin() + MsgIdCipher.encode(msgId) + getBodyEnd();
		}
	}
	
	/**
	 * create an email id section for X-Header, using the provided msgId as the
	 * source of Email_Id code.
	 * 
	 * @param msgId
	 *            email_id to be wrapped
	 * @return wrapped email_id
	 */
	public String wrapupEmailId4XHdr(Long msgId) throws NumberFormatException {
		if (msgId == null) {
			return null;
		}
		else {
			return getXhdrBegin() + MsgIdCipher.encode(msgId) + getXhdrEnd();
		}
	}

	/**
	 * return the X-Header name for Message_Id
	 * 
	 * @return X-Header name
	 */
	public String getEmailIdXHdrName() {
		return idTokensVo.getXHeaderName();
	}

	private String getClientId() {
		return idTokensVo.getClientId();
	}
	
	private String getBodyBegin() {
		return idTokensVo.getBodyBeginToken();
	}
	
	private String getBodyEnd() {
		return idTokensVo.getBodyEndToken();
	}
	
	private String getBodyXhdrBegin() {
		return idTokensVo.getXHeaderName() + ": " + idTokensVo.getXhdrBeginToken();
	}
	
	private String getXhdrBegin() {
		return idTokensVo.getXhdrBeginToken();
	}
	
	private String getXhdrEnd() {
		return idTokensVo.getXhdrEndToken();
	}
	
	public static void main(String[] args) {
		long msgId = 12345L;
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String msgStr = "this is my message text.\n" + parser.wrapupEmailId(msgId) + "\n...the rest";
		System.out.println("Original Msg Text: " + msgStr);
		String id = parser.parseMsg(msgStr);
		System.out.println("Email Id restored: " + id);
		String msgStr2 = parser.replaceEmailId(msgStr, msgId);
		System.out.println("Msg Text after replace: " + msgStr2);
		System.out.println("Email Id restored: " + parser.parseMsg(msgStr2));
		System.out.println("Msg: " + parser.replaceKey(msgStr, parser.bodyPattern, 9876543));
		System.out.println("Msg: " + parser.removeKey(msgStr, parser.bodyPattern));
		
		Matcher matcher = parser.bodyPattern.matcher("aaaaab \nSystem Email \r\tId: \n 10.123456.0 ... the rest");
		if (matcher.find()) {
			String matched = matcher.group(matcher.groupCount());
			System.out.println("Matched String: " + matched + ", groups: " + matcher.groupCount());
		}
		else {
			System.out.println("Pattern not matched.");
		}
	}
}
