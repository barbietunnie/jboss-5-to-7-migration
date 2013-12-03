package com.es.vo.comm;


public class ReloadFlagsVo extends BaseVoWithRowId implements java.io.Serializable {	
	private static final long serialVersionUID = 2746790651251857142L;
	private int senders = 0;
	private int rules = 0;
	private int actions = 0;
	private int templates = 0;
	private int schedules = 0;
	
	public int getSenders() {
		return senders;
	}
	public void setSenders(int senders) {
		this.senders = senders;
	}
	public int getRules() {
		return rules;
	}
	public void setRules(int rules) {
		this.rules = rules;
	}
	public int getActions() {
		return actions;
	}
	public void setActions(int actions) {
		this.actions = actions;
	}
	public int getTemplates() {
		return templates;
	}
	public void setTemplates(int templates) {
		this.templates = templates;
	}
	public int getSchedules() {
		return schedules;
	}
	public void setSchedules(int schedules) {
		this.schedules = schedules;
	}
}
