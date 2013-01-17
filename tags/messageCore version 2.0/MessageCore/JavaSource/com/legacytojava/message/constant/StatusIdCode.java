package com.legacytojava.message.constant;

public class StatusIdCode {

	//
	// define general statusId
	//
	public final static String ACTIVE = "A";
	public final static String INACTIVE = "I";
	public final static String SUSPENDED = "S"; // for Email Address
	public final static int BOUNCE_SUSPEND_THRESHOLD = 5;
	// suspend email address after 5 times of consecutive bounces

}
