package com.legacytojava.message.vo.inbox;

import java.io.Serializable;

import com.legacytojava.message.vo.BaseVo;

public class AttachmentsVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -2967470914809239961L;
	private long msgId = -1;
	private int attchmntDepth = -1;
	private int attchmntSeq = -1;
	private String attchmntName = null;
	private String attchmntType = null;
	private String attchmntDisp = null;
	private byte[] attchmntValue = null;
	
	/** Define UI Components */
	private int attachmentSize = 0;
	
	public int getAttachmentSize() {
		if (attchmntValue == null) {
			return attachmentSize;
		}
		else {
			return attchmntValue.length;
		}
	}
	
	public String getSizeAsString() {
		int len = getAttachmentSize();
		if (len < 1024) {
			return 1024 + "";
		}
		else {
			return (int) Math.ceil((double)len / 1024.0) + "K";
		}
	}

	public void setAttachmentSize(int size) {
		attachmentSize = size;
	}
	/** End of UI Components */
	
	public int getAttchmntDepth() {
		return attchmntDepth;
	}
	public void setAttchmntDepth(int attchmntDepth) {
		this.attchmntDepth = attchmntDepth;
	}
	public String getAttchmntDisp() {
		return attchmntDisp;
	}
	public void setAttchmntDisp(String attchmntDisp) {
		this.attchmntDisp = attchmntDisp;
	}
	public String getAttchmntName() {
		return attchmntName;
	}
	public void setAttchmntName(String attchmntName) {
		this.attchmntName = attchmntName;
	}
	public int getAttchmntSeq() {
		return attchmntSeq;
	}
	public void setAttchmntSeq(int attchmntSeq) {
		this.attchmntSeq = attchmntSeq;
	}
	public String getAttchmntType() {
		return attchmntType;
	}
	public void setAttchmntType(String attchmntType) {
		this.attchmntType = attchmntType;
	}
	public byte[] getAttchmntValue() {
		return attchmntValue;
	}
	public void setAttchmntValue(byte[] attchmntValue) {
		this.attchmntValue = attchmntValue;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	
}