package com.legacytojava.message.vo;


public class ReloadFlagsVo extends BaseVo implements java.io.Serializable {	
	private static final long serialVersionUID = 2746790651251857142L;
	private int clients = 0;
	private int rules = 0;
	private int actions = 0;
	private int templates = 0;
	private int schedules = 0;
	
	public int getClients() {
		return clients;
	}
	public void setClients(int clients) {
		this.clients = clients;
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
