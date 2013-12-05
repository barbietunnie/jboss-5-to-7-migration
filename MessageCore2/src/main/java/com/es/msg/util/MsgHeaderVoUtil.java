package com.es.msg.util;

import java.util.ArrayList;
import java.util.List;

import com.es.msgbean.MsgHeader;
import com.es.vo.inbox.MsgHeaderVo;

public class MsgHeaderVoUtil {

	public static List<MsgHeader> toMsgHeaderList(List<MsgHeaderVo> voList) {
		List<MsgHeader> list = new ArrayList<MsgHeader>();
		for (MsgHeaderVo vo : voList) {
			MsgHeader msgHeader = new MsgHeader();
			msgHeader.setName(vo.getHeaderName());
			msgHeader.setValue(vo.getHeaderValue());
			list.add(msgHeader);
		}
		return list;
	}

}
