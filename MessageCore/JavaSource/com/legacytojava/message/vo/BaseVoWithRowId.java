package com.legacytojava.message.vo;

import javax.xml.bind.annotation.XmlElement;

public class BaseVoWithRowId extends BaseVo {
	private static final long serialVersionUID = -3339330324256273692L;

	@XmlElement
	protected int rowId = -1;

	public final int getRowId() {
		return rowId;
	}
	public final void setRowId(int rowId) {
		this.rowId = rowId;
	}

}
