package jpa.message;

import java.io.Serializable;

import jpa.model.MailInbox;

public class MessageContext implements Serializable {
	private static final long serialVersionUID = -8429972515707390401L;

	private javax.mail.Message[] messages;
	private MailInbox mailInbox;
	private MessageBean messageBean;
	private byte[] messageStream;
	
	public MessageContext() {}
	
	public MessageContext(javax.mail.Message[] messages, MailInbox mailInbox) {
		this.messages = messages;
		this.mailInbox = mailInbox;
	}

	public MessageContext(MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public MessageContext(byte[] messageStream) {
		this.messageStream = messageStream;
	}

	public javax.mail.Message[] getMessages() {
		return messages;
	}

	public void setMessages(javax.mail.Message[] messages) {
		this.messages = messages;
	}

	public MailInbox getMailInbox() {
		return mailInbox;
	}

	public void setMailInbox(MailInbox mailInbox) {
		this.mailInbox = mailInbox;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public byte[] getMessageStream() {
		return messageStream;
	}
}
