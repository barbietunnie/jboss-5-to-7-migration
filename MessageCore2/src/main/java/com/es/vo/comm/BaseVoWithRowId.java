package com.es.vo.comm;

public class BaseVoWithRowId extends BaseVo {
	private static final long serialVersionUID = 6044654314841160008L;
	protected int rowId = -1;

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

}
