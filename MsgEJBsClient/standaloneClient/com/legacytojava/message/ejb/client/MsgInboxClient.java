package com.legacytojava.message.ejb.client;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.CreateException;
import javax.mail.internet.AddressException;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanBuilder;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.bo.inbox.MessageParser;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.dao.idtokens.EmailIdParser;
import com.legacytojava.message.ejb.rule.MsgInboxRemote;
import com.legacytojava.message.ejb.sendmail.SendMailRemote;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.LookupUtil;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

/**
 * this class tests both SendMail and MsgInbox EJB's
 */
public class MsgInboxClient {
	public static void main(String[] args){
		try {
			MsgInboxClient msgInboxClient = new MsgInboxClient();
			msgInboxClient.invokeEJBs();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static boolean GetFromDB = false;
	private void invokeEJBs() throws CreateException, AddressException,
			DataValidationException, ParseException, IOException {
		SendMailRemote sendMail = (SendMailRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/SendMail!com.legacytojava.message.ejb.sendmail.SendMailRemote");
		MsgInboxRemote msgInbox = (MsgInboxRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/MsgInbox!com.legacytojava.message.ejb.rule.MsgInboxRemote");
		
		MessageParser parser = (MessageParser)SpringUtil.getAppContext().getBean("messageParser");
		
		long msgId = 1L;
		MessageBean messageBean = null;
		if (GetFromDB) {
			messageBean = sendMail.getMessageByPK(msgId);
		}
		else {
			MsgInboxVo msgVo = msgInbox.getMessageByPK(msgId);
			messageBean = MessageBeanBuilder.createMessageBean(msgVo);
		}
		System.out.println("MessageBean returned: "+messageBean);
		
		parser.parse(messageBean);
		
		// build MsgHeader
		MsgHeader header = new MsgHeader();
		header.setName(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
		header.setValue(EmailIdParser.getDefaultParser().wrapupEmailId4XHdr(msgId));
		List<MsgHeader> headers = new ArrayList<MsgHeader>();
		headers.add(header);
		messageBean.setHeaders(headers);
		
		System.out.println("MessageBean After: " + messageBean);
		if (messageBean.getRuleName()==null)
			messageBean.setRuleName(RuleNameType.GENERIC.toString());
		msgId = msgInbox.saveMessage(messageBean);
		System.out.println("msgInboxBo.saveMessage - MsgId returned: " + msgId);
	}
}
