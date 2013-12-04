package com.es.msgbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.es.vo.comm.MailBoxVo;

public class MessageContext implements Serializable {
	private static final long serialVersionUID = -8429972515707390401L;

	private javax.mail.Message[] messages;
	private MailBoxVo mailBoxVo;
	private MessageBean messageBean;
	private byte[] messageStream;
	private String taskArguments;
	private List<Long> msgIdList;
	private List<Long> emailAddrIdList;
	
	public MessageContext() {}
	
	public MessageContext(javax.mail.Message[] messages, MailBoxVo mailBoxVo) {
		this.messages = messages;
		this.mailBoxVo = mailBoxVo;
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

	public MailBoxVo getMailBoxVo() {
		return mailBoxVo;
	}

	public void setMailBoxVo(MailBoxVo mailBoxVo) {
		this.mailBoxVo = mailBoxVo;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public void setMessageBean(MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public byte[] getMessageStream() {
		return messageStream;
	}

	public String getTaskArguments() {
		return taskArguments;
	}

	public void setMessageStream(byte[] messageStream) {
		this.messageStream = messageStream;
	}

	public void setTaskArguments(String taskArguments) {
		this.taskArguments = taskArguments;
	}

	public List<Long> getMsgIdList() {
		if (msgIdList == null) {
			msgIdList = new ArrayList<Long>();
		}
		return msgIdList;
	}
	public List<Long> getEmailAddrIdList() {
		if (emailAddrIdList == null) {
			emailAddrIdList = new ArrayList<Long>();
		}
		return emailAddrIdList;
	}
}
