package jpa.data.preload;

import jpa.constant.MailProtocol;
import jpa.constant.StatusId;

public enum MailInboxEnum {
	SUPPORT("support","support","localhost",-1,MailProtocol.POP3, "Default Site Return Path",StatusId.ACTIVE,
			false,5,false,false,null,true,true,true);
	
	MailInboxEnum(String userId, String userPswd, String hostName, int port,
			MailProtocol protocol, String description, StatusId status,
			Boolean isInternalOnly, int readPerPass, boolean isUseSsl,
			Boolean isToPlainText, String toAddressDomain,
			Boolean isCheckDuplicate, Boolean isAlertDuplicate,
			Boolean isLogDuplicate) {
		
	}
}
