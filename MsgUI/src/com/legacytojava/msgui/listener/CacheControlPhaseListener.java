package com.legacytojava.msgui.listener;

import java.util.HashSet;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

import com.legacytojava.msgui.util.FacesUtil;

public class CacheControlPhaseListener implements PhaseListener {
	private static final long serialVersionUID = -2166859627692568459L;

	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}

	public void afterPhase(PhaseEvent event) {
	}

	private final Set<String> Exclusions = new HashSet<String>();
	public void beforePhase(PhaseEvent event) {
		String viewId = FacesUtil.getCurrentViewId();
		if (Exclusions.isEmpty()) {
			Exclusions.add("/login.jsp");
			Exclusions.add("/mailingListCompose.jsp");
			Exclusions.add("/emailTemplateEdit.jsp");
			//Exclusions.add("/msgInboxList.jsp");
		}
		//System.out.println("ViewId: " + viewId);
		if (Exclusions.contains(viewId)) {
			// exclude certain pages.
			return;
		}
		FacesContext facesContext = event.getFacesContext();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext()
				.getResponse();
		response.addHeader("Pragma", "no-cache");
		//response.addHeader("Expires", "-1");
		response.addHeader("Cache-Control", "no-cache");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "must-revalidate");
	}
}