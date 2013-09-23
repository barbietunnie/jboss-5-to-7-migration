package jpa.msgui.vo;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.util.StringUtil;


public final class PagingSubscriberData extends PagingVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 2702501767172625606L;
	private String senderId = null;
	private String ssnNumber = null;
	private String dayPhone = null;
	private String firstName = null;
	private String lastName = null;
	private String emailAddr = null;
	
	public static void main(String[] args) {
		PagingSubscriberData vo = new PagingSubscriberData();
		vo.printMethodNames();
		PagingSubscriberData vo2 = new PagingSubscriberData();
		vo2.setSenderId(Constants.DEFAULT_SENDER_ID);
		vo.setSsnNumber(" 123-45-6789 ");
		vo2.setStatusId(StatusId.ACTIVE.getValue());
		StringUtil.stripAll(vo);
		System.out.println(vo.toString());
		System.out.println(vo.equalsToSearch(vo2));
		System.out.println(vo.listChanges());
	}

	public void resetPageContext() {
		super.resetPageContext();
		senderId = null;
		ssnNumber = null;
		dayPhone = null;
		firstName = null;
		lastName = null;
		emailAddr = null;
	}
	
	protected void setSearchableFields() {
		super.setSearchableFields();
		searchFields.add("senderId");
		searchFields.add("ssnNumber");
		searchFields.add("dayPhone");
		searchFields.add("firstName");
		searchFields.add("lastName");
		searchFields.add("emailAddr");
	}

	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String clientId) {
		this.senderId = clientId;
	}
	public String getSsnNumber() {
		return ssnNumber;
	}
	public void setSsnNumber(String ssnNumber) {
		this.ssnNumber = ssnNumber;
	}
	public String getDayPhone() {
		return dayPhone;
	}
	public void setDayPhone(String dayPhone) {
		this.dayPhone = dayPhone;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
}
