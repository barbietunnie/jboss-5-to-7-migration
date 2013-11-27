package com.es.vo.comm;

import java.io.Serializable;
import java.sql.Timestamp;

public class TimerServerVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = 826439429623556631L;
	private int timerInterval = -1;
	private String timerIntervalUnit = "";
	private int initialDelay = -1;
	private Timestamp startTime;
	
	public String getProcessorName() {
		// always use this default value
		return "timerProcessor";
	}

	public int getInitialDelay() {
		return initialDelay;
	}
	public void setInitialDelay(int initialDelay) {
		this.initialDelay = initialDelay;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public int getTimerInterval() {
		return timerInterval;
	}
	public void setTimerInterval(int timerInterval) {
		this.timerInterval = timerInterval;
	}
	public String getTimerIntervalUnit() {
		return timerIntervalUnit;
	}
	public void setTimerIntervalUnit(String timerIntervalUnit) {
		this.timerIntervalUnit = timerIntervalUnit;
	}
}