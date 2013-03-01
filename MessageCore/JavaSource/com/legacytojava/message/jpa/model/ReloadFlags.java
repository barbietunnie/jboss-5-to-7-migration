package com.legacytojava.message.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ReloadFlags")
/*
 * XXX !!!!! Rollback will not work since this table is defined with MyISAM engine
 */
public class ReloadFlags implements java.io.Serializable {	
	private static final long serialVersionUID = -5657762883755527124L;

	@Id
	private int rowId = -1;
	private int clients = 0;
	private int rules = 0;
	private int actions = 0;
	private int templates = 0;
	private int schedules = 0;
	
	public ReloadFlags() {
		// must have a no-argument constructor
	}

	public int getRowId() {
		return rowId;
	}

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
