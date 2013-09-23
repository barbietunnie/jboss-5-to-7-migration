package jpa.msgui.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class PagingVo extends BasePagingVo implements Serializable {
	private static final long serialVersionUID = -3937664465278379794L;
	
	// define paging context
	public static final int PAGE_SIZE = 20;
	private Timestamp dateTimeFirst = null;
	private Timestamp dateTimeLast = null;
	private int idFirst = -1;
	private int idLast = -1;
	private String strIdFirst = null;
	private String strIdLast = null;
	public static enum PageAction {FIRST, NEXT, PREVIOUS, CURRENT, LAST};
	private PageAction pageAction = PageAction.CURRENT;
	private int pageSize = PAGE_SIZE;
	private int rowCount = -1;
	// end of paging
	
	private String searchString = null;

	public static void main(String[] args) {
		PagingVo vo = new PagingVo();
		vo.printMethodNames();
		System.out.println(vo.toString());
		PagingVo vo2 = new PagingVo();
		vo2.setStatusId("A");
		vo.setSearchString("abcd");
		System.out.println(vo.equalsToSearch(vo2));
		System.out.println(vo.listChanges());
	}
	
	public PagingVo() {
		init();
		setStatusId(null);
	}
	
	private void init() {
		setSearchableFields();
	}
	
	protected void setSearchableFields() {
		searchFields.add("searchString");
		searchFields.add("statusId");
	}
	
	public void resetPageContext() {
		dateTimeFirst = null;
		dateTimeLast = null;
		idFirst = -1;
		idLast = -1;
		strIdFirst = null;
		strIdLast = null;
		pageAction = PageAction.CURRENT;
		rowCount = -1;
	}
	
	public Timestamp getDateTimeFirst() {
		return dateTimeFirst;
	}

	public void setDateTimeFirst(Timestamp dateTimeFirst) {
		this.dateTimeFirst = dateTimeFirst;
	}

	public Timestamp getDateTimeLast() {
		return dateTimeLast;
	}

	public void setDateTimeLast(Timestamp dateTimeLast) {
		this.dateTimeLast = dateTimeLast;
	}

	public PageAction getPageAction() {
		return pageAction;
	}

	public void setPageAction(PageAction action) {
		this.pageAction = action;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getIdFirst() {
		return idFirst;
	}

	public void setIdFirst(int idFirst) {
		this.idFirst = idFirst;
	}

	public int getIdLast() {
		return idLast;
	}

	public void setIdLast(int idLast) {
		this.idLast = idLast;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	public String getStrIdFirst() {
		return strIdFirst;
	}

	public void setStrIdFirst(String strIdFirst) {
		this.strIdFirst = strIdFirst;
	}

	public String getStrIdLast() {
		return strIdLast;
	}

	public void setStrIdLast(String strIdLast) {
		this.strIdLast = strIdLast;
	}
}