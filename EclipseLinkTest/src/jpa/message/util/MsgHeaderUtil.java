package jpa.message.util;

import java.util.ArrayList;
import java.util.List;

import jpa.message.MsgHeader;
import jpa.model.message.MessageHeader;

public class MsgHeaderUtil {

	public static List<MsgHeader> messageHeaderList2MsgHeaderList(List<MessageHeader> messageHeaderList) {
		List<MsgHeader> list = new ArrayList<MsgHeader>();
		for (MessageHeader header : messageHeaderList) {
			MsgHeader msgHeader = new MsgHeader();
			msgHeader.setName(header.getHeaderName());
			msgHeader.setValue(header.getHeaderValue());
			list.add(msgHeader);
		}
		return list;
	}
}
