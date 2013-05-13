package jpa.msgui.bean;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class DebugBean {
	private boolean showMessages = true;

	public boolean isShowMessages() {
		return showMessages;
	}
	public void setShowMessages(boolean debug) {
		this.showMessages = debug;
	}
}
