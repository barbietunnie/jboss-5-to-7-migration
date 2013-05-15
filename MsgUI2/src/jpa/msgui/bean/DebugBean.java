package jpa.msgui.bean;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name="debugBean")
@ApplicationScoped
public class DebugBean implements java.io.Serializable {
	private static final long serialVersionUID = 6115363411392224312L;
	private boolean showMessages = true;

	public boolean isShowMessages() {
		return showMessages;
	}
	public void setShowMessages(boolean debug) {
		this.showMessages = debug;
	}
}
