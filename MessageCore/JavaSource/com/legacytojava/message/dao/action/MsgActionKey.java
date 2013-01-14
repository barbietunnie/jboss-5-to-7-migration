package com.legacytojava.message.dao.action;

public class MsgActionKey {
	public String ruleName = "";
	public int actionSeq = -1;
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public int getActionSeq() {
		return actionSeq;
	}
	public void setActionSeq(int actionSeq) {
		this.actionSeq = actionSeq;
	}
}
