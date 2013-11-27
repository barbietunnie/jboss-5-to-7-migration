package com.es.vo.outbox;

import java.io.Serializable;

import com.es.vo.comm.BaseVo;

public class RenderAttachmentVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -5823830395412308000L;
	private long renderId = -1L;
	private int attchmntSeq = -1;
	private String attchmntName = null;
	private String attchmntType = null;
	private String attchmntDisp = null;
	private byte[] attchmntValue = null;
	
	public String getAttchmntDisp() {
		return attchmntDisp;
	}
	public void setAttchmntDisp(String attchmntDisp) {
		this.attchmntDisp = attchmntDisp;
	}
	public String getAttchmntName() {
		return this.attchmntName;
	}
	public void setAttchmntName(String attchmntName) {
		this.attchmntName = attchmntName;
	}
	public int getAttchmntSeq() {
		return this.attchmntSeq;
	}
	public void setAttchmntSeq(int attchmntSeq) {
		this.attchmntSeq = attchmntSeq;
	}
	public String getAttchmntType() {
		return this.attchmntType;
	}
	public void setAttchmntType(String attchmntType) {
		this.attchmntType = attchmntType;
	}
	public byte[] getAttchmntValue() {
		return this.attchmntValue;
	}
	public void setAttchmntValue(byte[] attchmntValue) {
		this.attchmntValue = attchmntValue;
	}
	public long getRenderId() {
		return renderId;
	}
	public void setRenderId(long renderId) {
		this.renderId = renderId;
	}
}
